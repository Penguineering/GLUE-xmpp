package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;

import org.apache.commons.configuration.ConfigurationException;

import de.ovgu.dke.glue.api.reporting.ReportListener;
import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.api.transport.PacketHandlerFactory;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportFactory;
import de.ovgu.dke.glue.api.transport.TransportLifecycleListener;
import de.ovgu.dke.glue.xmpp.config.XMPPConfigurationLoader;
import de.ovgu.dke.glue.xmpp.config.XMPPPropertiesConfigurationLoader;

public class XMPPTransportFactory implements TransportFactory {
	public static final String DEFAULT_REGISTRY_KEY = "xmpp";

	private XMPPClient client;

	public XMPPTransportFactory() {
	}

	@Override
	public void init() throws TransportException {
		try {
			final XMPPConfigurationLoader confLoader = new XMPPPropertiesConfigurationLoader();
			this.client = new XMPPClient(confLoader.loadConfiguration());
			this.client.startup();
		} catch (ConfigurationException e) {
			throw new TransportException("Error loading the configuration: "
					+ e.getMessage(), e);
		} catch (TransportException e) {
			throw new TransportException("Error during client initialization: "
					+ e.getMessage(), e);
		}
	}

	@Override
	public void setDefaultPacketHandlerFactory(
			PacketHandlerFactory handlerFactory) throws TransportException {
		if (client == null)
			throw new TransportException("Client has not been initialized!");
		client.setDefaultPacketHandlerFactory(handlerFactory);
	}

	@Override
	public void setSerializationProvider(SerializationProvider provider)
			throws TransportException {
		client.setDefaultSerializationProvider(provider);
	}

	@Override
	public Transport createTransport(URI peer) throws TransportException {
		return client == null ? null : client.createTransport(peer);
	}

	@Override
	public void dispose() {
		if (client != null)
			client.teardown();
	}

	@Override
	public String getDefaultRegistryKey() {
		return DEFAULT_REGISTRY_KEY;
	}

	@Override
	public void addReportListener(ReportListener listener) {
		if (client != null)
			client.addReportListener(listener);
	}

	@Override
	public void removeReportListener(ReportListener listener) {
		if (client != null)
			client.removeReportListener(listener);
	}

	@Override
	public void addTransportLifecycleListener(
			TransportLifecycleListener listener) {
		if (client != null)
			client.addLifecycleListener(listener);
	}

	@Override
	public void removeTransportLifecycleListener(
			TransportLifecycleListener listener) {
		if (client != null)
			client.removeLifecycleListener(listener);
	}
}
