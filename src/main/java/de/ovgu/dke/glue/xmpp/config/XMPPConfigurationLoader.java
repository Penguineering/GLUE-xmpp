package de.ovgu.dke.glue.xmpp.config;

import org.apache.commons.configuration.ConfigurationException;

/**
 * Interface for configuration loader: load an XMPP configuration.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 */
public interface XMPPConfigurationLoader {
	public XMPPConfiguration loadConfiguration() throws ConfigurationException;
}
