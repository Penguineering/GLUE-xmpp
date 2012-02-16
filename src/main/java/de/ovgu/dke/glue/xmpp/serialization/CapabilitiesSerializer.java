package de.ovgu.dke.glue.xmpp.serialization;

import de.ovgu.dke.glue.api.serialization.Serializer;

/**
 * <p>
 * Serializer for capabilities messages.
 * </p>
 * 
 * <p>
 * The schema is http://dke.ovgu.de/glue/xmpp/Capabilities
 * </p>
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 * 
 */
public abstract class CapabilitiesSerializer implements Serializer {
	public static final String SCHEMA = "http://dke.ovgu.de/glue/xmpp/Capabilities";

	@Override
	public String getSchema() {
		return SCHEMA;
	}
}
