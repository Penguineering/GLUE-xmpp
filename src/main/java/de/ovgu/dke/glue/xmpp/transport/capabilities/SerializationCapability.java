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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.ovgu.dke.glue.api.serialization.SerializationProvider;

import net.jcip.annotations.Immutable;


/**
 * A serialization capability entry consisting of format and schema.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 *
 */
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
