package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;

import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.api.transport.Connection;
import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.transport.thread.XMPPPacketThread;

public class XMPPConn extends Connection {
	final XMPPTransport transport;

	/**
	 * This reflects the last known peer's JID, including a resource. The id
	 * changes depending on the last received message and is the target for
	 * subsequent replies.
	 */
	private URI effective_jid;

	public XMPPConn(final String connectionSchema, final XMPPTransport transport) {
		super(connectionSchema);
		this.transport = transport;

		this.effective_jid = transport.getPeer();
	}

	public URI getEffectiveJID() {
		synchronized (this) {
			return effective_jid;
		}
	}

	public void setEffectiveJID(URI jid) {
		synchronized (this) {
			this.effective_jid = jid;
		}
	}

	@Override
	public PacketThread createThread(PacketHandler handler)
			throws TransportException {
		return transport.createThread(this, handler);
	}

	@Override
	public Transport getTransport() {
		return transport;
	}

	@Override
	public URI getPeer() {
		return transport.getPeer();
	}

	public void disposeThread(PacketThread thread) {
		transport.disposeThread(thread);
	}

	@Override
	protected void sendSerializedPayload(PacketThread pt, Object payload,
			Packet.Priority priority) throws TransportException {
		final XMPPPacket pkt = createPacket((XMPPPacketThread) pt, payload,
				priority);

		transport.sendPacket((XMPPPacketThread) pt, pkt);
	}

	public XMPPPacket createPacket(XMPPPacketThread pt, Object payload,
			Packet.Priority priority) throws TransportException {
		return new XMPPPacket(payload, priority, transport.getClient()
				.getLocalURI(), getEffectiveJID(), pt.getId(),
				this.getConnectionSchema());
	}

	@Override
	public boolean checkCapabilities() throws TransportException {
		return transport.checkCapabilities(getConnectionSchema());
	}

	@Override
	public String getSerializationFormat() {
		// TODO later: make this according to client capabilities
		return SerializationProvider.STRING;
	}

}
