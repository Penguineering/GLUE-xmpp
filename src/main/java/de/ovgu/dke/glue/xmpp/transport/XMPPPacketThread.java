package de.ovgu.dke.glue.xmpp.transport;

import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.TransportException;

public class XMPPPacketThread implements PacketThread {
	private final XMPPTransport transport;
	private final int id;

	public XMPPPacketThread(XMPPTransport transport, int id) {
		this.transport = transport;
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	@Override
	public void dispose() {
		transport.disposeThread(this);
	}

	@Override
	public void send(Packet packet) throws TransportException {
		try {
			 transport.sendPacket(this, (XMPPPacket) packet);
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
