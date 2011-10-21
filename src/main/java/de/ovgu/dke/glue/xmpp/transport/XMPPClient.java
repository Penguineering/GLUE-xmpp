package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import de.ovgu.dke.glue.xmpp.config.XMPPConfiguration;

/**
 * XMPP Client to receive and evaluate XMPP requests.
 * 
 * Uses <code>m_conn_lock</code> for internal thread synchronization on
 * connection access.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 */
public class XMPPClient implements PacketListener, ConnectionListener {
	static Log logger = LogFactory.getLog(XMPPClient.class);

	private final XMPPConfiguration xmppconfig;

	/**
	 * Used to lock the <code>m_handler</code> as it may be accessed through
	 * different threads.
	 */
	// private final Object handler_lock = new Object();

	private XMPPConnection connection = null;

	/**
	 * Used to lock the <code>connection</code> as it may be accessed through
	 * different threads.
	 */
	private final Object conn_lock = new Object();

	/**
	 * This presence is used when the client is ready to receive commands.
	 */
	static final Presence PRESENCE_ONLINE = new Presence(
			Presence.Type.available, "XMPP client up and running.", 10,
			Presence.Mode.available);

	/**
	 * This presence is used when the client is started and logged in, but the
	 * packet collector is not yet working.
	 */
	static final Presence PRESENCE_XA = new Presence(Presence.Type.available,
			"Currently not available", 0, Presence.Mode.xa);

	/**
	 * This presence is used when the client closes the connection.
	 */
	static final Presence PRESENCE_OFFLINE = new Presence(
			Presence.Type.unavailable);

	/**
	 * Create a new, configured but uninitialized client instance. The XMPP
	 * configuration is obtained from the plugin environment
	 * (conf/xmpp.properties).
	 * 
	 * @throws IllegalStateException
	 *             if the plugin environment could not be found
	 */
	public XMPPClient(final XMPPConfiguration config) {
		if (config == null)
			throw new NullPointerException("Configuration may not be null!");
		xmppconfig = config;
	}

	/**
	 * Get the configuration for this client.
	 * 
	 * @return the XMPP configuration used for this client instance.
	 */
	public XMPPConfiguration getXmppconfig() {
		return xmppconfig;
	}

	/**
	 * Start the client. Connects to the server, logs in and sets the status to
	 * extended away, as the client is not yet listening on the queue.
	 * 
	 * If initialized correctly, the client is able to receive and can act as a
	 * packet queue provider.
	 * 
	 * @throws IllegalStateException
	 *             if there is already a connection.
	 * @throws XMPPException
	 *             when an errors during XMPP communication occurs.
	 */
	public void startup() throws XMPPException {
		synchronized (conn_lock) {
			// TODO use XMPPException
			if (connection != null)
				throw new IllegalStateException("Connection already exists.");

			final ConnectionConfiguration conn_config = new ConnectionConfiguration(
					xmppconfig.getServer());
			conn_config.setCompressionEnabled(xmppconfig.isCompression());

			// adapt online priority to configuration
			PRESENCE_ONLINE.setPriority(xmppconfig.getPriority());

			// do not send an initial presence
			conn_config.setSendPresence(false);

			String resource = xmppconfig.getResource();
			// generate a resource name if none is given
			if (resource == null || resource.length() == 0)
				resource = "xmpp" + System.currentTimeMillis();

			// allow reconnection
			conn_config.setReconnectionAllowed(true);

			// create a new connection
			connection = new XMPPConnection(conn_config);

			// establish the connection
			connection.connect();

			// add this client as connection listener
			connection.addConnectionListener(this);
			connection.addPacketListener(this, null);

			// login
			connection.login(xmppconfig.getUser(), xmppconfig.getPass(),
					resource);

			// go online
			connection.sendPacket(PRESENCE_ONLINE);
		}

		logger.info("XMPP connection established.");
	}

	@Override
	public void processPacket(final Packet packet) {
		if (packet == null)
			return;

		// TODO evaluate the packet
		// getHandler().handleXMPPPacket(packet);
	}

	/**
	 * Tear down the XMPP client instance. Logs off, closes the connection and
	 * unties the handler.
	 */
	public void teardown() {
		synchronized (conn_lock) {
			if (connection != null) {
				// go offline and disconnect
				connection.removePacketListener(this);
				connection.disconnect(PRESENCE_OFFLINE);
				connection = null;

				// remove the handler
				// synchronized (handler_lock) {
				// m_handler = null;
				// }

				logger.info("XMPP connection has been closed.");
			}
		}
	}

	public boolean isConnected() {
		synchronized (conn_lock) {
			return connection != null && connection.isAuthenticated();
		}
	}

	public void enqueuePacket(final Packet packet) throws InterruptedException {
		synchronized (conn_lock) {
			if (!isConnected())
				throw new IllegalStateException(
						"Connection is not properly initialized!");

			connection.sendPacket(packet);
		}
	}

	@Override
	public void connectionClosed() {
	}

	@Override
	public void connectionClosedOnError(Exception e) {
	}

	@Override
	public void reconnectingIn(int seconds) {
	}

	@Override
	public void reconnectionFailed(Exception e) {
	}

	@Override
	public void reconnectionSuccessful() {
	}

	public URI getLocalURI(){
		final String jid;
		synchronized (conn_lock) {
			jid = connection.getUser();
		}
		return jid == null ? null : URI.create("xmpp:" + jid);
	}
}
