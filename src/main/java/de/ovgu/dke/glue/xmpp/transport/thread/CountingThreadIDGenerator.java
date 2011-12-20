package de.ovgu.dke.glue.xmpp.transport.thread;

import java.net.URI;

import de.ovgu.dke.glue.api.transport.TransportException;

public class CountingThreadIDGenerator implements ThreadIDGenerator {
	final URI local_peer;
	private Integer last_id = 0;

	public CountingThreadIDGenerator(URI localPeer) {
		this.local_peer = localPeer;
	}

	@Override
	public String generateThreadID() throws TransportException {
		synchronized (last_id) {
			last_id++;
			final String id = local_peer.toASCIIString() + ":"
					+ Integer.toString(last_id);

			return id;
		}
	}

	@Override
	public String generateMetaThreadID() throws TransportException {
		final String id = local_peer.toASCIIString() + ":meta";

		return id;
	}
}
