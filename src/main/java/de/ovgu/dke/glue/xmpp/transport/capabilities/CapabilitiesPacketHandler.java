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
package de.ovgu.dke.glue.xmpp.transport.capabilities;

import java.util.List;

import de.ovgu.dke.glue.api.reporting.ReportListener;
import de.ovgu.dke.glue.api.reporting.ReportListenerSupport;
import de.ovgu.dke.glue.api.reporting.Reporter;
import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;

public class CapabilitiesPacketHandler implements PacketHandler, Reporter {
	private final ReportListenerSupport reporting;

	public CapabilitiesPacketHandler() {
		reporting = new ReportListenerSupport(this);
	}

	@Override
	public void handle(PacketThread packetThread, Packet packet) {
		// TODO check packet thread ID (API call missing!)		

		// check payload
		final Object _payload = packet.getPayload();
		if (_payload == null) {
			reporting.fireReport(
					"Got capabilities packet without payload, ignoring.", null,
					Reporter.Level.WARN);
			return;
		}

		if (!(_payload instanceof List<?>)) {
			reporting.fireReport(
					"Capabilities payload is not of type List<?>, ignoring.",
					null, Reporter.Level.ERROR);
			return;
		}

		@SuppressWarnings("unchecked")
		final List<SerializationCapability> caps = (List<SerializationCapability>) _payload;

		for (SerializationCapability cap : caps)
			System.out.println(cap.getFormat() + " - " + cap.getSchema());

		// TODO set serializer
		// TODO report serializer
	}

	@Override
	public void addReportListener(ReportListener listener) {
		reporting.addReportListener(listener);
	}

	@Override
	public void removeReportListener(ReportListener listener) {
		reporting.removeReportListener(listener);
	}
}
