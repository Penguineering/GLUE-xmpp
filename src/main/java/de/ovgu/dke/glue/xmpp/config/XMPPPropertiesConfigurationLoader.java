/*
 * Copyright 2012 Stefan Haun, Thomas Low, Sebastian Stober, Andreas Nürnberger
 * 
 *      Data and Knowledge Engineering Group, 
 * 		Faculty of Computer Science,
 *		Otto-von-Guericke University,
 *		Magdeburg, Germany
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ovgu.dke.glue.xmpp.config;

import java.io.File;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Load configuration from one of the standard locations.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 */
public class XMPPPropertiesConfigurationLoader implements
		XMPPConfigurationLoader {
	static Log logger = LogFactory
			.getLog(XMPPPropertiesConfigurationLoader.class);

	public static final String[] CONFIG_LOCATIONS = { "conf/xmpp.properties",
			"src/main/config/xmpp.properties" };

	public static final String CONFIG_PATH = "de.ovgu.dke.glue.xmpp.configpath";

	private static File getConfigFile() {
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

		return cfile;
	}

	// TODO environment should have precedence
	@Override
	public XMPPConfiguration loadConfiguration(final Properties env)
			throws ConfigurationException {
		final String path = env == null ? null : env.getProperty(CONFIG_PATH,
				null);
		File cfile;

		if (path != null) {
			cfile = new File(path);
			logger.debug("Using XMPP config from " + CONFIG_PATH + ": "
					+ cfile.getAbsolutePath());
		} else
			cfile = getConfigFile();

		final XMPPConfiguration config;

		if (cfile != null && cfile.exists())
			// load from configuration file
			config = new XMPPConfiguration(cfile.getAbsolutePath());
		else
			// or try to get from environment
			config = XMPPConfiguration.fromProperties(env);

		return config;
	}

}
