package de.ovgu.dke.glue.xmpp.transport.capabilities;

import java.util.List;

import de.ovgu.dke.glue.api.serialization.SerializationException;

public interface CapabilityMatcher {
	public SerializationCapability matchSerializationCapabilities(
			final List<SerializationCapability> local,
			final List<SerializationCapability> remote)
			throws SerializationException;
}
