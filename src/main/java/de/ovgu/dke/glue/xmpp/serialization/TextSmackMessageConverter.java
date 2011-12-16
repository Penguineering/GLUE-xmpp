package de.ovgu.dke.glue.xmpp.serialization;

import java.net.URI;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.Serializer;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacket;

public class TextSmackMessageConverter implements SmackMessageConverter {

	@Override
	public Message toSmack(XMPPPacket pkt) throws SerializationException {
		// create the message
		Message msg = new Message(uri2jid(pkt.getReceiver()));
		msg.setType(Message.Type.chat);

		// add payload
		StringBuffer payload = new StringBuffer();

		payload.append(pkt.getThreadId());

		if (pkt.getPayload() != null) {
			payload.append("\n");
			payload.append(pkt.getPayload());
		}

		msg.setBody(payload.toString());

		return msg;
	}

	@Override
	public XMPPPacket fromSmack(Message msg, Serializer serializer)
			throws SerializationException {
		String body = msg.getBody();
		String id = null;

		if (body != null) {
			// take the first line from payload
			int br_idx = body.indexOf('\n');
			if (br_idx > 0) {
				id = body.substring(0, br_idx);
				body = body.substring(br_idx + 1);
			} else {
				id = body;
				body = null;
			}
		}

		Object payload = null;
		if (serializer != null && body != null) {
			payload = serializer.deserialize(body);
		}
		XMPPPacket pkt = new XMPPPacket(payload,
				de.ovgu.dke.glue.api.transport.Packet.Priority.DEFERRABLE,
				URI.create("xmpp:" + msg.getFrom()), URI.create("xmpp:"
						+ msg.getTo()), id, msg);

		return pkt;
	}

	protected static String uri2jid(URI peer) throws SerializationException {
		if (!peer.toString().startsWith("xmpp:"))
			throw new SerializationException(
					"Target peer does not use the xmpp protocol: " + peer);

		return peer.toString().substring(5);
	}

}
