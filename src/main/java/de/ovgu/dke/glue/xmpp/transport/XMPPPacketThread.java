package de.ovgu.dke.glue.xmpp.transport;

import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.TransportException;

public class XMPPPacketThread implements PacketThread {
	private final XMPPTransport transport;

	// the pair (owner; id) makes the process ID used to distinguisch different
	// message threads between a pair of peers
	private final String owner;
	private final int id;

	public XMPPPacketThread(XMPPTransport transport, int id) {
		this(transport, transport.getPeer().toString(), id);
	}

	public XMPPPacketThread(XMPPTransport transport, String owner, int id) {
		this.transport = transport;
		this.owner = owner;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getOwner() {
		return owner;
	}

	@Override
	public void dispose() {
		transport.disposeThread(this);
	}

	@Override
	public void send(Object payload, Packet.Priority priority)
			throws TransportException {
		try {
			transport.sendPacket(this,
					(XMPPPacket) createPacket(payload, priority));
		} catch (ClassCastException e) {
			throw new TransportException(
					"Error converting packet to XMPP packet, invalid implementation type!",
					e);
		}
	}

	public Packet createPacket(Object payload, Packet.Priority priority)
			throws TransportException {
		XMPPPacket pkt = new XMPPPacket(payload, priority);
		pkt.sender = transport.getClient().getLocalURI();
		pkt.receiver = transport.getPeer();

		return pkt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		if (id != other.id)
			return false;
		if (transport == null) {
			if (other.transport != null)
				return false;
		} else if (!transport.equals(other.transport))
			return false;
		return true;
	}
}
