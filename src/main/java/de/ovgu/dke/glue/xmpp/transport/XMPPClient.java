package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import de.ovgu.dke.glue.api.transport.PacketHandlerFactory;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportFactory;
import de.ovgu.dke.glue.xmpp.config.XMPPConfiguration;
import de.ovgu.dke.glue.xmpp.transport.thread.CountingThreadIDGenerator;
import de.ovgu.dke.glue.xmpp.transport.thread.PacketThreadManager;

/**
 * XMPP Client to receive and evaluate XMPP requests.
 * 
 * Uses <code>m_conn_lock</code> for internal thread synchronization on
 * connection access.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 */
public class XMPPClient implements PacketListener, ConnectionListener,
		TransportFactory {
	static Log logger = LogFactory.getLog(XMPPClient.class);

	private final XMPPConfiguration xmppconfig;

	private final ConcurrentMap<URI, XMPPTransport> transports;

	private final PacketHandlerFactory handler_factory;

	private PacketThreadManager threads;

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
	 * Create a new, configured but uninitialized client instance.
	 * 
	 * @param config
	 *            The XMPP configuration
	 * @param handlerFactory
	 *            default packet handler factory.
	 * @throws NullPointerException
	 *             if the <code>config</code> parameter is <code>null</code>.
	 */
	public XMPPClient(final XMPPConfiguration config,
			final PacketHandlerFactory handlerFactory) {
		if (config == null)
			throw new NullPointerException("Configuration may not be null!");
		this.xmppconfig = config;

		this.handler_factory = handlerFactory;

		this.transports = new ConcurrentHashMap<URI, XMPPTransport>();
		this.threads = null;
	}

	/**
	 * Get the configuration for this client.
	 * 
	 * @return the XMPP configuration used for this client instance.
	 */
	public XMPPConfiguration getXmppconfig() {
		return xmppconfig;
	}

	@Override
	public Transport createTransport(URI peer) throws TransportException {
		try {
			XMPPTransport transport = transports.get(peer);

			if (transport == null) {
				transport = new XMPPTransport(peer, this, threads);
				transport.setDefaultPackerHandler(handler_factory
						.createPacketHandler());
				transports.put(peer, transport);
			}

			return transport;
		} catch (Exception e) {
			throw new TransportException(e);
		}
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
	 * @throws TransportException
	 *             when an errors during XMPP communication occurs.
	 */
	public void startup() throws TransportException {
		try {
			synchronized (conn_lock) {
				if (connection != null)
					throw new TransportException("Connection already exists.");

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

				// create the thread manager for this connection
				this.threads = new PacketThreadManager(
						new CountingThreadIDGenerator(this.getLocalURI()));

				// go online
				connection.sendPacket(PRESENCE_ONLINE);
			}

			logger.info("XMPP connection established.");
		} catch (XMPPException e) {
			throw new TransportException(
					"Error during initialization of XMPP connection: "
							+ e.getMessage(), e);
		}
	}

	@Override
	// TODO wohin mit den Fehlermeldungen?
	public void processPacket(final Packet packet) {
		if ((packet == null) || !(packet instanceof Message))
			return;
		final Message msg = (Message) packet;

		try {
			// get the transport for this sender
			final XMPPTransport transport = findTransport(msg.getFrom());

			// create XMPP packet from smack packet
			XMPPPacket pkt = transport.processSmackMessage(msg);

			// TODO check ID consistency

			XMPPPacketThread pt = threads.retrieveThread(pkt.thread_id);

			if (pt == null) {
				logger.debug("Creating new packet thread with ID "
						+ pkt.thread_id);
				pt = (XMPPPacketThread) threads.addThread(transport,
						pkt.thread_id, transport.getDefaultPacketHandler());
			}

			if (pt != null) {
				// adapt the threads effective JID
				pt.setEffectiveJID(URI.create("xmpp:" + pkt.sender));

				// TODO handle message in thread
				logger.debug(pt.getTransport().getPeer());
				logger.debug("Payload:\n" + pkt.getPayload());

				// TODO call via thread
				if (pt.getHandler() != null)
					pt.getHandler().handle(pt, pkt);
				else
					logger.error("No packet handler defined for thread "
							+ pt.getId());
			}
		} catch (TransportException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tries to find a transport that matches the given JID, if necessary by
	 * removing the Jabber resource. If none could be found, create a new
	 * transport.
	 * 
	 * @param from
	 * @return
	 */
	protected XMPPTransport findTransport(final String from) {
		final URI peer = URI.create(from);
		XMPPTransport transport = transports.get(peer);

		// if not found, first look for transport without resource
		// TODO move thread to new transport?
		if (transport == null) {
			int idx = from.lastIndexOf('/');
			if (idx > 0) {
				final URI shortFrom = URI.create("xmpp:"
						+ from.substring(0, idx));
				transport = transports.get(shortFrom);
			}
		}
		// if still not found: create transport
		if (transport == null) {
			try {
				transport = (XMPPTransport) createTransport(peer);
			} catch (TransportException e) {
				// TODO reporting benutzen
				logger.error("Could not create transport: " + e.getMessage(), e);
			}
		}

		return transport;
	}

	/**
	 * Tear down the XMPP client instance. Logs off, closes the connection and
	 * unties the handler.
	 */
	public void teardown() {
		synchronized (conn_lock) {
			if (connection != null) {
				// dispose the threads
				for (String id : this.threads.getThreadIDs()) {
					final PacketThread pt = this.threads.retrieveThread(id);
					if (pt != null)
						pt.dispose();
				}
				this.threads = null;

				// close the transports
				this.transports.clear();

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

	public URI getLocalURI() {
		final String jid;
		synchronized (conn_lock) {
			jid = connection.getUser();
		}
		return jid == null ? null : URI.create("xmpp:" + jid);
	}
}
