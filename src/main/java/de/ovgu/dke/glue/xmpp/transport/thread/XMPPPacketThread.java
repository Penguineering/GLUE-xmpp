package de.ovgu.dke.glue.xmpp.transport.thread;

import java.net.URI;

import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacket;
import de.ovgu.dke.glue.xmpp.transport.XMPPTransport;

//TODO synchronization
public class XMPPPacketThread extends PacketThread {
	private final XMPPTransport transport;

	private final String id;

	private PacketHandler handler;

	/**
	 * This reflects the last known peer's JID, including a resource. The id
	 * changes depending on the last received message and is the target for
	 * subsequent replies.
	 */
	private URI effective_jid;

	public XMPPPacketThread(XMPPTransport transport, String id,
			PacketHandler handler) throws TransportException {
		this.transport = transport;
		this.id = id;

		this.handler = handler;

		this.effective_jid = transport.getPeer();
	}

	public String getId() {
		return id;
	}

	@Override
	public XMPPTransport getTransport() {
		return transport;
	}

	public PacketHandler getHandler() {
		return handler;
	}

	public void setHandler(PacketHandler handler) {
		this.handler = handler;
	}

	public URI getEffectiveJID() {
		return effective_jid;
	}

	public void setEffectiveJID(URI jid) {
		this.effective_jid = jid;
	}

	@Override
	public void dispose() {
		transport.disposeThread(this);
	}

	@Override
	protected void sendSerializedPayload(Object payload,
			Packet.Priority priority) throws TransportException {
		final XMPPPacket pkt = createPacket(payload, priority);

		transport.sendPacket(this, pkt);
	}

	public XMPPPacket createPacket(Object payload, Packet.Priority priority)
			throws TransportException {
		return new XMPPPacket(payload, priority, transport.getClient()
				.getLocalURI(), effective_jid, this.getId());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((transport == null) ? 0 : transport.hashCode());
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
		XMPPPacketThread other = (XMPPPacketThread) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (transport == null) {
			if (other.transport != null)
				return false;
		} else if (!transport.equals(other.transport))
			return false;
		return true;
	}

	@Override
	public URI getPeer() {
		return effective_jid;
	}
}
