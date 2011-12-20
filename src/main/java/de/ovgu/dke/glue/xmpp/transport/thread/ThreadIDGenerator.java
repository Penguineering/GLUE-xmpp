package de.ovgu.dke.glue.xmpp.transport.thread;

import de.ovgu.dke.glue.api.transport.TransportException;

public interface ThreadIDGenerator {
	public String generateThreadID() throws TransportException;

	/**
	 * The thread ID for meta communication (such as capabilities).
	 * 
	 * @return
	 * @throws TransportException
	 */
	public String generateMetaThreadID() throws TransportException;
}
