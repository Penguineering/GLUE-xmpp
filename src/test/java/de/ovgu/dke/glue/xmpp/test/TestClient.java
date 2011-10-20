package de.ovgu.dke.glue.xmpp.test;

import java.net.URI;
import java.net.URISyntaxException;

import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportRegistry;
import de.ovgu.dke.glue.xmpp.XMPPTransportFactory;
import de.ovgu.dke.glue.xmpp.config.XMPPPropertiesConfigurationLoader;

public class TestClient {
	public static void main(String args[]) throws TransportException,
			URISyntaxException {

		// init the transport registry
		TransportRegistry.getInstance().registerTransportFactory(
				"xmpp",
				new XMPPTransportFactory(
						new XMPPPropertiesConfigurationLoader()));
		TransportRegistry.getInstance().setDefaultTransportFactory("xmpp");

		// get a transport
		final Transport xmpp = TransportRegistry
				.getInstance()
				.getDefaultTransportFactory()
				.createTransport(
						new URI("xmpp:shaun@bison.cs.uni-magdeburg.de"));

	}
}
