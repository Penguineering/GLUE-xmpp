package de.ovgu.dke.glue.xmpp.config;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Load configuration from one of the standard locations.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 */
public class XMPPPropertiesConfigurationLoader implements XMPPConfigurationLoader {
	static Log logger = LogFactory.getLog(XMPPPropertiesConfigurationLoader.class);

	public static final String[] CONFIG_LOCATIONS = { "conf/xmpp.properties",
	"src/main/config/xmpp.properties" };

	public static File getConfigFile() throws ConfigurationException {
		File cfile = null;
		for (int i = 0; i < CONFIG_LOCATIONS.length && cfile == null; i++) {
			// check for location of config file
			cfile = new File(CONFIG_LOCATIONS[i]);
			if (!cfile.exists()) {
				logger.debug("Config file at location "
						+ cfile.getAbsolutePath() + " [" + i
						+ "] does not exist.");
				cfile = null;
			} else 
				logger.debug("Using XMPP config file at location "
						+ cfile.getAbsolutePath() + " [" + i + "].");			
		}

		if (cfile == null)
			throw new ConfigurationException("Config file could not be found!");

		return cfile;
	}

	@Override
	public XMPPConfiguration loadConfiguration() throws ConfigurationException {
		final File cfile = getConfigFile();

		final XMPPConfiguration config = new XMPPConfiguration(
				cfile.getAbsolutePath());

		return config;
	}

}
