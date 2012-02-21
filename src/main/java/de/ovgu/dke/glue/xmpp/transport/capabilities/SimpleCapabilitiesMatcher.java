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

import de.ovgu.dke.glue.api.serialization.SerializationException;

/**
 * Very simple capabilities matcher that just iterates the lists and returns the
 * first match.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 * 
 */
public class SimpleCapabilitiesMatcher implements CapabilityMatcher {

	@Override
	public SerializationCapability matchSerializationCapabilities(
			List<SerializationCapability> local,
			List<SerializationCapability> remote) throws SerializationException {
		if (local == null || remote == null)
			throw new SerializationException(
					"Capabilities list may not be null!");

		// go through the local list of capabilities
		for (final SerializationCapability lcap : local)
			// look for matching remote capability
			for (final SerializationCapability rcap : remote)
				// return if they are equal
				if (lcap.equals(rcap))
					return lcap;

		// nothing has been found
		return null;
	}
}
