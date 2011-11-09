package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;

import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.TransportException;

//TODO synchronization
public class XMPPPacketThread implements PacketThread {
	private final XMPPTransport transport;

	private final String id;
	
	private PacketHandler handler;

	/**
	 * this reflects the last known peer's JID, including a resource. This id
	 * changes depending in the last received message and is the target for
	 * subsequent replies.
	 */
	private URI effective_jid;

	public XMPPPacketThread(XMPPTransport transport, String id, PacketHandler handler)
			throws TransportException {
		this.transport = transport;
		this.id = id;
		
		this.handler = handler;

		this.effective_jid = transport.getPeer();
	}

	public String getId() {
		return id;
	}

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
	public void send(Object payload, Packet.Priority priority)
			throws TransportException {
		try {
			final XMPPPacket pkt = new XMPPPacket(payload, priority);
			pkt.sender = transport.getClient().getLocalURI();
			pkt.receiver = effective_jid;
			pkt.thread_id = this.getId();

			transport.sendPacket(this, pkt);
		} catch (ClassCastException e) {
			throw new TransportException(
					"Error converting packet to XMPP packet, invalid implementation type!",
					e);
		}
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
}
