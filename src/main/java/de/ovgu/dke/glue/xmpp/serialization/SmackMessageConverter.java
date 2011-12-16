package de.ovgu.dke.glue.xmpp.serialization;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.Serializer;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacket;

public interface SmackMessageConverter {
	/**
	 * Create a smack packet from an XMPP packet with serialized payload.
	 * 
	 * @param pkt
	 *            The packet to convert
	 * @return Smack packet which can be sent
	 * @throws SerializationException
	 *             if the peer URI is not an XMPP URI
	 */
	public Message toSmack(final XMPPPacket pkt) throws SerializationException;

	/**
	 * Create an XMPP packet from a smack message. The payload will be
	 * deserialized.
	 * 
	 * @param msg
	 *            The incoming message.
	 * @param serializer
	 *            The serializer.
	 * @return XMPP packet with de-serialized payload.
	 * @throws SerializationException
	 *             If de-serialization fails.
	 */
	public XMPPPacket fromSmack(final Message msg, Serializer serializer)
			throws SerializationException;
}
