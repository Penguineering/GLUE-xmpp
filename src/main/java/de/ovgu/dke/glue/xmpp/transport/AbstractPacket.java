package de.ovgu.dke.glue.xmpp.transport;

import de.ovgu.dke.glue.api.transport.Packet;

public abstract class AbstractPacket implements Packet {
	private final Object payload;
	private final Packet.Priority priority;

	public AbstractPacket(final Object payload, final Packet.Priority priority) {
		this.payload = payload;
		this.priority = priority;
	}

	@Override
	public Object getPayload() {
		return payload;
	}

	@Override
	public Priority getPriority() {
		return priority;
	}

	@Override
	public Object getAttribute(String key) {
		return null;
	}

}
