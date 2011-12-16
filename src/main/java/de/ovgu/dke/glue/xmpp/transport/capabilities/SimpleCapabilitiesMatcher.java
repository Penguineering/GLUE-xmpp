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
