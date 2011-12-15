package de.ovgu.dke.glue.xmpp.serialization;

import java.net.URI;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.Serializer;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacket;

public abstract class AbstractSmackPacketConverter implements
		SmackMessageConverter {
	@Override
	public Message toSmack(XMPPPacket pkt) throws SerializationException {
		Message msg = new Message(uri2jid(pkt.receiver));
		msg.setType(Message.Type.chat);

		addPayloadAndThread(msg, pkt);

		return msg;
	}

	@Override
	public XMPPPacket fromSmack(Message msg, Serializer serializer)
			throws SerializationException {
		Object payload = getPayload(msg);
		if (serializer != null) {
			payload = serializer.deserialize(payload);
		}
		XMPPPacket pkt = new XMPPPacket(payload,
				de.ovgu.dke.glue.api.transport.Packet.Priority.DEFERRABLE);
		pkt.receiver = URI.create("xmpp:" + msg.getTo());
		pkt.sender = URI.create("xmpp:" + msg.getFrom());
		pkt.thread_id = getThread(msg);

		return pkt;
	}

	protected abstract void addPayloadAndThread(Message msg, XMPPPacket pkt)
			throws SerializationException;

	protected abstract String getPayload(Message msg)
			throws SerializationException;

	protected abstract String getThread(Message msg)
			throws SerializationException;

	protected static String uri2jid(URI peer) throws SerializationException {
		if (!peer.toString().startsWith("xmpp:"))
			throw new SerializationException(
					"Target peer does not use the xmpp protocol: " + peer);

		return peer.toString().substring(5);
	}

}
