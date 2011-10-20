package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportFactory;

public class XMPPTransportFactory implements TransportFactory {
	private final XMPPClient client;

	private final ConcurrentMap<URI, XMPPTransport> transports;

	public XMPPTransportFactory(final XMPPClient client) {
		this.client = client;

		this.transports = new ConcurrentHashMap<URI, XMPPTransport>();
	}

	@Override
	public Transport createTransport(URI peer) throws TransportException {
		try {
			XMPPTransport transport = transports.get(peer);

			if (transport == null) {
				transport = new XMPPTransport(peer, client);
				transports.put(peer, transport);
			}

			return transport;
		} catch (Exception e) {
			throw new TransportException(e);
		}
	}

}
