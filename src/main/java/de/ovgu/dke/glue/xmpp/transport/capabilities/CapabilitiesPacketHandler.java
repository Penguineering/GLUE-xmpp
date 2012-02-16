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
