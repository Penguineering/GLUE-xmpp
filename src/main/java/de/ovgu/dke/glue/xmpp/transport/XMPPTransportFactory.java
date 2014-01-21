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
package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;

import de.ovgu.dke.glue.api.endpoint.Endpoint;
import de.ovgu.dke.glue.api.reporting.ReportListener;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportFactory;
import de.ovgu.dke.glue.api.transport.TransportLifecycleListener;
import de.ovgu.dke.glue.xmpp.config.XMPPConfiguration;
import de.ovgu.dke.glue.xmpp.config.XMPPConfigurationLoader;
import de.ovgu.dke.glue.xmpp.config.XMPPPropertiesConfigurationLoader;

public class XMPPTransportFactory implements TransportFactory {
	public static final String DEFAULT_REGISTRY_KEY = "xmpp";

	private XMPPClient client;

	public XMPPTransportFactory() {
	}

	/**
	 * @param config
	 *            The configuration, which may have one of three states: Either
	 *            it contains the configuration keys for the XMPP transport (see
	 *            configuration example file), or it contains the key
	 *            de.ovgu.dke.glue.xmpp.configpath with a path to the
	 *            configuration file, or it is empty with respect to XMPP
	 *            configuration, then the configuration file will be searched in
	 *            the standards paths.
	 */
	@Override
	public void init(final Properties config) throws TransportException {
		try {
			// The XMPP configuration is loaded directly from the properties, if
			// sufficient, otherwise the file loading mechanism is used.
			final XMPPConfiguration xmpp_conf;
			if (XMPPConfiguration.isValidEnvironment(config))
				xmpp_conf = XMPPConfiguration.fromProperties(config);
			else {
				final XMPPConfigurationLoader confLoader = new XMPPPropertiesConfigurationLoader();
				xmpp_conf = confLoader.loadConfiguration(config);
			}
			this.client = new XMPPClient(xmpp_conf);

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

	@Override
	public boolean servesPeer(URI peer, String schema) {
		// TODO should be more sophisticated
		return peer.getScheme().equals("xmpp");
		// && defaultEndpoints.keySet().contains(schema); ?
	}

	@Override
	public void addInboundEndpoint(Endpoint inboundEndpoint) {
		if (client == null)
			throw new IllegalStateException(
					"The transport factory has not been provided with a client!");
		client.addInboundEndpoint(inboundEndpoint);
	}

}
