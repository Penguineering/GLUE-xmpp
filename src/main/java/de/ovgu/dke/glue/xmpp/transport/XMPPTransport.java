package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.reporting.ReportListener;
import de.ovgu.dke.glue.api.reporting.ReportListenerSupport;
import de.ovgu.dke.glue.api.transport.LifecycleListener;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.transport.thread.ThreadIDGenerator;

// TODO peer muss mit und ggf. ohne ressource matchen
// packet thread proxy verwenden, um resource matching umzusetzen

// follows http://xmpp.org/extensions/xep-0201.html for message threading
public class XMPPTransport implements Transport {
	private final URI peer;
	private final XMPPClient client;

	private final ReportListenerSupport report_listeners;
	private final Collection<LifecycleListener> lifecycle_listeners;

	private final Map<String, XMPPPacketThread> threads;

	private PacketHandler defaultPacketHandler;

	/**
	 * The thread ID generator instance is shared among all transports of a client
	 */
	private final ThreadIDGenerator id_generator;

	public XMPPTransport(final URI peer, final XMPPClient client, ThreadIDGenerator generator) {
		this.peer = peer;
		this.client = client;

		this.report_listeners = new ReportListenerSupport(this);
		this.lifecycle_listeners = new LinkedList<LifecycleListener>();

		this.threads = new ConcurrentHashMap<String, XMPPPacketThread>();

		this.id_generator = generator; 
	}

	public final URI getPeer() {
		return peer;
	}

	protected XMPPClient getClient() {
		return client;
	}

	@Override
	public void addReportListener(ReportListener listener) {
		report_listeners.addReportListener(listener);
	}

	@Override
	public void removeReportListener(ReportListener listener) {
		report_listeners.removeReportListener(listener);
	}

	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		synchronized (lifecycle_listeners) {
			lifecycle_listeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		synchronized (lifecycle_listeners) {
			lifecycle_listeners.remove(listener);
		}
	}

	protected void fireLifecycleListeners(Transport.Status oldStatus,
			Transport.Status newStatus) {
		synchronized (lifecycle_listeners) {
			for (final LifecycleListener listener : lifecycle_listeners)
				listener.onStatusChange(this, oldStatus, newStatus);
		}
	}

	@Override
	public PacketThread createThread(PacketHandler handler)
			throws TransportException {
		// TODO generate id
		final String id = id_generator.generate();
		XMPPPacketThread pt = new XMPPPacketThread(this, id);

		// register packet thread
		threads.put(pt.getId(), pt);

		return pt;
	}

	void disposeThread(PacketThread thread) {
		if (thread != null)
			threads.remove(thread);
	}

	@Override
	public void setDefaultPackerHandler(PacketHandler handler) {
		this.defaultPacketHandler = handler;
	}

	void sendPacket(final XMPPPacketThread thread, final XMPPPacket packet)
			throws TransportException {
		// check thread
		final XMPPPacketThread lt = threads.get(thread.getId());

		if (lt == null || thread.getTransport() != this)
			throw new TransportException("Packet thread " + thread.getId()
					+ " is not registered on this transport!");

		// create an XMPP message
		Message msg = createXMPPMessage(thread, packet);

		try {
			client.enqueuePacket(msg);
		} catch (InterruptedException e) {
			throw new TransportException("Error sending XMPP packet: "
					+ e.getMessage(), e);
		}
	}

	protected Message createXMPPMessage(final XMPPPacketThread thread,
			final XMPPPacket packet) throws TransportException {
		Message msg = new Message(uri2jid(packet.receiver));
		msg.setType(Message.Type.chat);
		msg.setThread(thread.getId());

		if (packet.getPayload() != null)
			msg.setBody(packet.getPayload().toString());

		return msg;
	}

	protected static String uri2jid(URI peer) throws TransportException {
		if (!peer.toString().startsWith("xmpp:"))
			throw new TransportException(
					"Target peer does not use the xmpp protocol: " + peer);

		return peer.toString().substring(5);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((peer == null) ? 0 : peer.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XMPPTransport other = (XMPPTransport) obj;
		if (peer == null) {
			if (other.peer != null)
				return false;
		} else if (!peer.equals(other.peer))
			return false;
		return true;
	}
}
