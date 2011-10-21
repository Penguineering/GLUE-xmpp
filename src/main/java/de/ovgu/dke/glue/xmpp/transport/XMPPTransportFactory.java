package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;

import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportFactory;

// FIXME: eher vom XMPPClient implementieren lassen?
public class XMPPTransportFactory implements TransportFactory {
	private final XMPPClient client;

	public XMPPTransportFactory(final XMPPClient client) {
		this.client = client;
	}

	@Override
	public Transport createTransport(URI peer) throws TransportException {
		return client == null ? null : client.createTransport(peer);
	}

}
