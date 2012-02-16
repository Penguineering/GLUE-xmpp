package de.ovgu.dke.glue.xmpp.serialization;

import java.util.List;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.api.serialization.Serializer;

/**
 * A wrapper for the capabilities serializer: Only returns formats/schemas for
 * the wrapped provider, i.e. the capabilities serializer will not be reported
 * in the capabilities. However, it has precedence if the format/schema matches.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 * 
 */
public class CapabilitiesSerializationProviderWrapper implements
		SerializationProvider {
	private final SerializationProvider wrappee;

	private final SerializationProvider cap;

	public CapabilitiesSerializationProviderWrapper(
			final SerializationProvider wrappee, SerializationProvider cap) {
		this.wrappee = wrappee;
		this.cap = cap;
	}

	@Override
	public List<String> availableFormats() {
		return wrappee.availableFormats();
	}

	@Override
	public List<String> getSchemas(String format) {
		return wrappee.getSchemas(format);
	}

	@Override
	public Serializer getSerializer(String format, String schema)
			throws SerializationException {
		Serializer ser;
		try {
			ser = cap.getSerializer(format, schema);
		} catch (SerializationException e) {
			// TODO how to determine whether the serializer is just not
			// avaiblable?
			ser = wrappee.getSerializer(format, schema);
		}

		return ser;
	}

}
