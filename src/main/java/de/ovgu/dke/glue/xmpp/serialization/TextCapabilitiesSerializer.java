package de.ovgu.dke.glue.xmpp.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.xmpp.transport.capabilities.SerializationCapability;

/**
 * <p>
 * Serializer for a capabilities message, which serializes to Text returned as
 * <code>String</code>.
 * </p>
 * 
 * <p>
 * The schema is http://dke.ovgu.de/glue/xmpp/Capabilities/text
 * </p>
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 * 
 */
public class TextCapabilitiesSerializer extends CapabilitiesSerializer {
	@Override
	public String getFormat() {
		return SerializationProvider.STRING;
	}

	@Override
	public Object serialize(Object o) throws SerializationException {
		// check object type
		if (!(o instanceof List<?>))
			throw new SerializationException(
					"Serializer expected list of SerializationCapability, got "
							+ (o == null ? "null" : o.getClass()
									.getCanonicalName()) + " instead!");

		@SuppressWarnings("unchecked")
		final List<SerializationCapability> capabilities = (List<SerializationCapability>) o;

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

		return payload.toString();
	}

	@Override
	public Object deserialize(Object payload) throws SerializationException {
		if (payload == null)
			return Collections.emptyList();

		if (!(payload instanceof String))
			throw new SerializationException("String payload expected!");

		final String[] lines = ((String) payload).split("\n");

		int c = 0;

		// look for the SERIALIZERS line
		while (c < lines.length && !lines[c].startsWith("SERIALIZERS"))
			c++;

		// no capabilities if we reached the end
		if (c == lines.length)
			return Collections.emptyList();

		// we have the SERIALIZERS line
		// there should be the number of serializers
		final String meta_line = lines[c];
		final String[] meta_fields = meta_line.split(" ");
		if (meta_fields.length != 2)
			throw new SerializationException(
					"Invalid format for SERIALIZERS line (line " + c + ")!");
		// decode number of serializers from the 2nd field
		final int sercount;
		try {
			String _sercount = meta_fields[1];
			sercount = Integer.parseInt(_sercount);
		} catch (NumberFormatException e) {
			throw new SerializationException(
					"Number format error in number of serializers (line " + c
							+ "): " + e.getMessage(), e);
		}

		if (sercount == 0)
			return Collections.emptyList();

		// check if there is a sufficient number of lines left
		if (lines.length - 1 < c + sercount)
			throw new SerializationException(
					"Too few remaining lines for the given number of serializers (line "
							+ c + ")!");

		// go to the next line
		++c;

		// result list
		final List<SerializationCapability> caps = new ArrayList<SerializationCapability>();

		// get the serializers
		for (int i = c; i < c + sercount; i++) {
			final String[] ser_fields = lines[c].split(" ");

			if (ser_fields.length != 2)
				throw new SerializationException(
						"Format error in serializer line, two fields expected (line"
								+ i + ")!");

			final String format = ser_fields[0];
			final String schema = ser_fields[1];

			caps.add(new SerializationCapability(format, schema));
		}

		return caps;
	}
}
