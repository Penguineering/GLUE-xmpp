package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;

import org.jivesoftware.smack.packet.Packet;

public class XMPPPacket extends AbstractPacket {
	public URI sender;
	public URI receiver;
	public String thread_id;
	public Packet xmpp_packet;

	public XMPPPacket(Object payload, Priority priority) {
		super(payload, priority);
	}

	@Override
	public Object getAttribute(String key) {
		if ("sender".equals(key))
			return sender;
		else if ("receiver".equals(key))
			return receiver;
		else if ("thread".equals(key))
			return thread_id;
		else if ("xmpp.packet".equals(key))
			return xmpp_packet;
		else
			return super.getAttribute(key);
	}
}
