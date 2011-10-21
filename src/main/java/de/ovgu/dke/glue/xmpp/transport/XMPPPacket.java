package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;


public class XMPPPacket extends AbstractPacket {
	public URI sender;
	public URI receiver;
	
	public XMPPPacket(Object payload, Priority priority) {
		super(payload, priority);
	}
}
