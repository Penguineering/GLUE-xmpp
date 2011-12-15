package de.ovgu.dke.glue.xmpp.serialization;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.Serializer;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacket;

public interface SmackMessageConverter {
	public Message toSmack(XMPPPacket pkt) throws SerializationException;

	public XMPPPacket fromSmack(Message msg, Serializer serializer) throws SerializationException;
}
