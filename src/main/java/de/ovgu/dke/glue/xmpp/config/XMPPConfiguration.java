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

import java.util.Properties;

import net.jcip.annotations.Immutable;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This class holds all configuration parameters for the XMPP connection used to
 * transfer data between this service and any clients. The Value Object Pattern
 * is used to ensure thread safety.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 */
@Immutable
public class XMPPConfiguration {
	/**
	 * The prefix for XMPP configuration keys.
	 */
	public static final String prefix = "de.ovgu.dke.glue.xmpp.";

	private final String server;

	private final String user;

	private final String pass;

	private final String resource;

	private final int priority;

	private final boolean compression;

	/**
	 * Create an XMPPConfiguration object from the provided properties file.
	 * 
	 * @param propfile
	 *            Path to the properties file
	 * @throws ConfigurationException
	 *             if the configuration cannot be loaded
	 */
	public static XMPPConfiguration fromFile(final String propfile)
			throws ConfigurationException {
		return new XMPPConfiguration(new PropertiesConfiguration(propfile));
	}

	/**
	 * Create a configuration from a properties object.
	 * 
	 * @param props
	 *            Properties object containing the configuration values.
	 * @return XMPP configuration instance.
	 * @throws NullPointerException if the props parameter is @code{null}
	 */
	public static XMPPConfiguration fromProperties(final Properties props) {
		final Configuration conf = new PropertiesConfiguration();

		for (Object key : props.keySet())
			conf.addProperty(key.toString(), props.getProperty(key.toString()));

		return new XMPPConfiguration(conf);
	}

	/**
	 * Create an XMPPConfiguration object from the provided configuration
	 * 
	 * @param config
	 *            An Apache commons-configuration provider
	 */
	public XMPPConfiguration(Configuration config) {
		server = config.getString(prefix + "server");

		user = config.getString(prefix + "user");
		pass = config.getString(prefix + "pass");
		resource = config.getString(prefix + "resource");

		priority = config.getInt(prefix + "priority", 5);
		compression = config.getBoolean(prefix + "compression");
	}

	/**
	 * @return The server to connect to, 2dn part of the JID
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @return The user ID, 1st part of the JID
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @return The password to use when logging into the server.
	 */
	public String getPass() {
		return pass;
	}

	/**
	 * @return The resource name to be used with this connection.
	 */
	public String getResource() {
		return resource;
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * Compress the connection? Uses less bandwidth at the cost of more CPU
	 * consumption.
	 * 
	 * @return true if the connection is to be compressed
	 */
	public boolean isCompression() {
		return compression;
	}
}
