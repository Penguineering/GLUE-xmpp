package de.ovgu.dke.glue.xmpp.test;

import java.util.Properties;

import de.ovgu.dke.glue.xmpp.config.XMPPPropertiesConfigurationLoader;

public class MessageTest extends
		de.ovgu.dke.glue.test.integration.scenarios.MessageTest {

	@Override
	public String getTransportFactoryClassName() {
		return "de.ovgu.dke.glue.xmpp.transport.XMPPTransportFactory";
	}

	@Override
	public Properties getPropertiesForReceiver() {
		Properties p = new Properties();
		p.setProperty(XMPPPropertiesConfigurationLoader.CONFIG_PATH,
				"src/main/config/peer2@jabber.org.properties");
		return p;
	}

	@Override
	public String getReceiverURI() {
		return "xmpp:peer2@jabber.org";
	}

	@Override
	public Properties getPropertiesForSender() {
		Properties p = new Properties();
		p.setProperty(XMPPPropertiesConfigurationLoader.CONFIG_PATH,
				"src/main/config/peer1@jabber.org.properties");
		return p;
	}

}
