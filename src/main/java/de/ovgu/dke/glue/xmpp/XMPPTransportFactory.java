package de.ovgu.dke.glue.xmpp;

import java.net.URI;

import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportFactory;
import de.ovgu.dke.glue.xmpp.config.XMPPConfiguration;
import de.ovgu.dke.glue.xmpp.config.XMPPConfigurationLoader;

public class XMPPTransportFactory implements TransportFactory {
	private final XMPPConfigurationLoader configLoader;

	private XMPPConfiguration config;

	public XMPPTransportFactory(final XMPPConfigurationLoader configLoader) {
		this.configLoader = configLoader;
		this.config = null;
	}

	protected void init() throws Exception {
		config = configLoader.loadConfiguration();
	}

	@Override
	public Transport createTransport(URI peer) throws TransportException {
		try {
		if (this.config == null)
				init();
		
		// TODO Auto-generated method stub
		return null;
		} catch (Exception e) {
			throw new TransportException(e);
		}
	}

}
