package de.ovgu.dke.glue.xmpp.transport.capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.ovgu.dke.glue.api.serialization.SerializationProvider;

import net.jcip.annotations.Immutable;

@Immutable
public class SerializationCapability {
	private final String format;
	private final String schema;

	public SerializationCapability(String format, String schema) {
		super();
		this.format = format;
		this.schema = schema;
	}

	public String getFormat() {
		return format;
	}

	public String getSchema() {
		return schema;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SerializationCapability other = (SerializationCapability) obj;
		if (format == null) {
			if (other.format != null)
				return false;
		} else if (!format.equals(other.format))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}	

	public static List<SerializationCapability> retrieveSerializationCapabilities(
			final SerializationProvider serializers) {
		if (serializers == null)
			return Collections.emptyList();

		final List<SerializationCapability> result = new ArrayList<SerializationCapability>();

		for (final String format : serializers.availableFormats())
			for (final String schema : serializers.getSchemas(format))
				result.add(new SerializationCapability(format, schema));

		return result;
	}
}
