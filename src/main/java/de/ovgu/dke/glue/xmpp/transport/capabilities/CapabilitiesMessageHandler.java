package de.ovgu.dke.glue.xmpp.transport.capabilities;

import java.util.List;

import de.ovgu.dke.glue.api.transport.TransportException;

public interface CapabilitiesMessageHandler {
	/**
	 * Create the payload for a capabilities message
	 * 
	 * @return The rendered payload.
	 * @throws TransportException
	 */
	public Object renderCapabilitiesMessage(
			List<SerializationCapability> capabilities)
			throws TransportException;

	public List<SerializationCapability> parseSerializationCapabilities(Object payload)
			throws TransportException;
}
