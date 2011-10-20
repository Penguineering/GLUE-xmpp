package de.ovgu.dke.glue.xmpp;

import java.net.URI;

import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportFactory;
import de.ovgu.dke.glue.xmpp.config.XMPPConfigurationLoader;

public class XMPPTransportFactory implements TransportFactory {
	private final XMPPConfigurationLoader configLoader;

	public XMPPTransportFactory(final XMPPConfigurationLoader configLoader) {
		this.configLoader = configLoader;
	}

	@Override
	public Transport createTransport(URI peer) throws TransportException {
		// TODO Auto-generated method stub
		return null;
	}

}
