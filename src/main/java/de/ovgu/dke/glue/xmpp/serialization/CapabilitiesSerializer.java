package de.ovgu.dke.glue.xmpp.serialization;

import de.ovgu.dke.glue.api.serialization.Serializer;

public abstract class CapabilitiesSerializer implements Serializer {
	public static final String SCHEMA = "http://dke.ovgu.de/glue/xmpp/Capabilities";

	@Override
	public String getSchema() {
		return SCHEMA;
	}
}
