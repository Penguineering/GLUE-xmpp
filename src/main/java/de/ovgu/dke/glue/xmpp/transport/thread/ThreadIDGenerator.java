package de.ovgu.dke.glue.xmpp.transport.thread;

import de.ovgu.dke.glue.api.transport.TransportException;

public interface ThreadIDGenerator {
	public String generate() throws TransportException;
}
