package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;

import org.apache.commons.configuration.ConfigurationException;

import de.ovgu.dke.glue.api.transport.PacketHandlerFactory;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportFactory;
import de.ovgu.dke.glue.api.transport.TransportRegistry;
import de.ovgu.dke.glue.xmpp.config.XMPPConfigurationLoader;
import de.ovgu.dke.glue.xmpp.config.XMPPPropertiesConfigurationLoader;

// FIXME: eher vom XMPPClient implementieren lassen?
public class XMPPTransportFactory implements TransportFactory {
	// TODO als Methode anbieten?
	public static final String DEFAULT_REGISTRY_KEY = "xmpp";

	private final XMPPClient client;

	public XMPPTransportFactory(final PacketHandlerFactory handlerFactory)
			throws TransportException {
		try {
			final XMPPConfigurationLoader confLoader = new XMPPPropertiesConfigurationLoader();
			this.client = new XMPPClient(confLoader.loadConfiguration(),
					handlerFactory);
			this.client.startup();
		} catch (ConfigurationException e) {
			throw new TransportException("Error loading the configuration: "
					+ e.getMessage(), e);
		} catch (TransportException e) {
			throw new TransportException("Error during client initialization: "
					+ e.getMessage(), e);
		}
	}

	public XMPPTransportFactory(final XMPPClient client) {
		this.client = client;
	}

	/**
	 * Register this transport factory as default
	 */
	public void registerAsDefault() {
		TransportRegistry.getInstance().registerTransportFactory(
				XMPPTransportFactory.DEFAULT_REGISTRY_KEY, this);
		TransportRegistry.getInstance().setDefaultTransportFactory(
				XMPPTransportFactory.DEFAULT_REGISTRY_KEY);
	}

	@Override
	public Transport createTransport(URI peer) throws TransportException {
		return client == null ? null : client.createTransport(peer);
	}

	public void dispose() {
		if (client != null)
			client.teardown();
	}

}
