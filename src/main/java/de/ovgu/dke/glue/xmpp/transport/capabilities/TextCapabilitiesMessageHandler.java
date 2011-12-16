package de.ovgu.dke.glue.xmpp.transport.capabilities;

import java.util.Collections;
import java.util.List;

import de.ovgu.dke.glue.api.transport.TransportException;

public class TextCapabilitiesMessageHandler implements
		CapabilitiesMessageHandler {

	@Override
	public Object renderCapabilitiesMessage(
			List<SerializationCapability> capabilities)
			throws TransportException {
		if (capabilities == null)
			throw new TransportException("Capabilities list may not be null!");

		StringBuffer payload = new StringBuffer("SERIALIZERS ");
		payload.append(capabilities.size());
		payload.append("\n");

		// TODO format may contain spaces?
		for (final SerializationCapability cap : capabilities) {
			payload.append(cap.getFormat());
			payload.append(" ");
			payload.append(cap.getSchema());
			payload.append("\n");
		}

		return payload;
	}

	@Override
	public List<SerializationCapability> parseSerializationCapabilities(
			Object payload) throws TransportException {
		if (payload == null)
			return Collections.emptyList();

		if (! (payload instanceof String))
			throw new TransportException("String payload expected!");
		
		final String[] lines = ((String)payload).split("\n");
		
		int c = 0;
		
		// look for the SERIALIZERS line
		while (c < lines.length && !lines[c].startsWith("SERIALIZERS"))
			c++;
		
		// no capabilities if we reached the end
		if (c == lines.length)
			return Collections.emptyList();
		
		// we have the SERIALIZERS line
		// there should be the number of serializers
		
		//TODO finish …
		
		return null;
	}
}
