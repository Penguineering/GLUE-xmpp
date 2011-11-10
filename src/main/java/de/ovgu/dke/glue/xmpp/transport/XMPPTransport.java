package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.reporting.ReportListener;
import de.ovgu.dke.glue.api.reporting.ReportListenerSupport;
import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.transport.LifecycleListener;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.serialization.SmackMessageConverter;
import de.ovgu.dke.glue.xmpp.serialization.TextThreadSmackPacketConverter;
import de.ovgu.dke.glue.xmpp.serialization.XMPPThreadSmackPacketConverter;
import de.ovgu.dke.glue.xmpp.transport.thread.PacketThreadManager;

// follows http://xmpp.org/extensions/xep-0201.html for message threading
// TODO variables threading-verfahren korrekt umsetzen
public class XMPPTransport implements Transport {
	private final URI peer;
	private final XMPPClient client;

	private final ReportListenerSupport report_listeners;
	private final Collection<LifecycleListener> lifecycle_listeners;

	private PacketHandler defaultPacketHandler;

	private SmackMessageConverter converter;

	private final PacketThreadManager threads;

	public XMPPTransport(final URI peer, final XMPPClient client,
			PacketThreadManager threads) {
		this.peer = peer;
		this.client = client;

		this.report_listeners = new ReportListenerSupport(this);
		this.lifecycle_listeners = new LinkedList<LifecycleListener>();
		this.threads = threads;

		this.converter = null;
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
		return threads.createThread(this,
				handler == null ? defaultPacketHandler : handler);
	}

	void disposeThread(PacketThread thread) {
		if (thread != null)
			threads.removeThread(((XMPPPacketThread) thread).getId());
	}

	@Override
	public void setDefaultPackerHandler(PacketHandler handler) {
		this.defaultPacketHandler = handler;
	}

	public PacketHandler getDefaultPacketHandler() {
		return this.defaultPacketHandler;
	}

	void sendPacket(final XMPPPacketThread thread, final XMPPPacket packet)
			throws TransportException {
		// check thread
		final XMPPPacketThread lt = threads.retrieveThread(thread.getId());

		if (lt == null || thread.getTransport() != this)
			throw new TransportException("Packet thread " + thread.getId()
					+ " is not registered on this transport!");

		// create an XMPP message
		SmackMessageConverter conv = this.converter;
		if (conv == null) {
			// be on the safe side and encode thread IDs in the message body
			conv = new TextThreadSmackPacketConverter();
			// a converter will be stored by the client upon receipt of a
			// message
		}

		try {
			final Message msg = conv.toSmack(packet);
			client.enqueuePacket(msg);
		} catch (InterruptedException e) {
			throw new TransportException("Error sending XMPP packet: "
					+ e.getMessage(), e);
		} catch (SerializationException e) {
			throw new TransportException("Error converting XMPP packet: "
					+ e.getMessage(), e);
		}
	}

	public XMPPPacket processSmackMessage(Message msg)
			throws TransportException {
		try {
			// create XMPP packet from smack packet
			XMPPPacket pkt = null;

			// get the converter
			SmackMessageConverter conv = this.getConverter();
			// if there is no converter, we try different methods:
			// first the XMPP threading (XEP-0201),
			// then thread info in body
			if (conv == null) {
				// try the XMPP threading converter first
				conv = new XMPPThreadSmackPacketConverter();
				pkt = conv.fromSmack(msg);

				// use the text based threading converter if there was no thread
				// attached
				if (pkt.thread_id == null || pkt.thread_id.isEmpty()) {
					conv = new TextThreadSmackPacketConverter();
					pkt = conv.fromSmack(msg);
				}
			} else
				pkt = conv.fromSmack(msg);

			if (pkt.thread_id == null) {
				throw new TransportException("Packet thread ID for "
						+ pkt.thread_id + " could not be retrieved!");
			} else if (this.getConverter() == null) {
				// if we are fine here, store the converter
				this.setConverter(conv);
			}

			// return the packet
			return pkt;
		} catch (SerializationException e) {
			throw new TransportException("Error converting Smack message: "
					+ e.getMessage(), e);
		}

	}

	protected SmackMessageConverter getConverter() {
		return converter;
	}

	protected void setConverter(SmackMessageConverter converter) {
		this.converter = converter;
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
