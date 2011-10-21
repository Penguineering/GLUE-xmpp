package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.reporting.ReportListener;
import de.ovgu.dke.glue.api.reporting.ReportListenerSupport;
import de.ovgu.dke.glue.api.transport.LifecycleListener;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;

public class XMPPTransport implements Transport {
	private final URI peer;
	private final XMPPClient client;

	private final ReportListenerSupport report_listeners;
	private final Collection<LifecycleListener> lifecycle_listeners;

	private final Set<PacketThread> threads;

	private PacketHandler defaultPacketHandler;

	public XMPPTransport(final URI peer, final XMPPClient client) {
		this.peer = peer;
		this.client = client;

		this.report_listeners = new ReportListenerSupport();
		this.lifecycle_listeners = new LinkedList<LifecycleListener>();

		this.threads = new HashSet<PacketThread>();
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
		final int id = -1;

		XMPPPacketThread pt = new XMPPPacketThread(this, id);

		// register packet thread
		synchronized (threads) {
			threads.add(pt);
		}

		return pt;
	}

	void disposeThread(PacketThread thread) {
		if (thread != null)
			synchronized (threads) {
				threads.remove(thread);
			}
	}

	@Override
	public void setDefaultPackerHandler(PacketHandler handler) {
		this.defaultPacketHandler = handler;
	}

	void sendPacket(XMPPPacketThread thread, XMPPPacket packet)
			throws TransportException {
		synchronized (threads) {
			if (!threads.contains(thread))
				throw new TransportException("Packet thread " + thread.getId()
						+ " is not registered for " + peer
						+ " on this transport!");

			// create an XMPP message
			Message msg = createXMPPMessage(packet);

			try {
				client.enqueuePacket(msg);
			} catch (InterruptedException e) {
				throw new TransportException("Error sending XMPP packet: "
						+ e.getMessage(), e);
			}
		}
	}

	protected Message createXMPPMessage(final XMPPPacket packet)
			throws TransportException {
		Message msg = new Message(uri2jid(packet.receiver));
		if (packet.getPayload() != null)
			msg.setBody(packet.getPayload().toString());

		return msg;
	}

	private static String uri2jid(URI peer) throws TransportException {
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
