package de.ovgu.dke.glue.xmpp.serialization;

import java.net.URI;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.api.serialization.Serializer;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacket;

public class TextSmackMessageConverter implements SmackMessageConverter {
	protected static String URI_PREFIX = "xmpp:";

	@Override
	public Message toSmack(XMPPPacket pkt, Serializer serializer)
			throws SerializationException {
		// create the message
		Message msg = new Message(uri2jid(pkt.getReceiver()));
		msg.setType(Message.Type.chat);

		// add payload
		StringBuffer payload = new StringBuffer();

		// first line: thread id
		payload.append(pkt.getThreadId());

		// second line: schema
		payload.append("\n");
		payload.append(serializer == null ? "\n" : serializer.getSchema());

		// next lines: payload
		if (pkt.getPayload() != null) {
			payload.append("\n");
			payload.append(pkt.getPayload());
		}

		msg.setBody(payload.toString());

		return msg;
	}

	@Override
	public XMPPPacket fromSmack(Message msg, SerializationProvider provider)
			throws SerializationException {
		String body = msg.getBody();
		String id = null;
		String schema = null;

		// first line: thread id
		if (body != null) {
			// take the first line from payload
			int br_idx = body.indexOf('\n');
			if (br_idx >= 0) {
				id = body.substring(0, br_idx);
				if (body.length() > br_idx)
					body = body.substring(br_idx + 1);
				else
					body = null;
			} else if (br_idx == 0) {
				id = null;
			} else {
				id = body;
				body = null;
			}
		}

		// second line: schema
		if (body != null) {
			int br_idx = body.indexOf('\n');
			if (br_idx > 0) {
				schema = body.substring(0, br_idx);
				if (body.length() > br_idx)
					body = body.substring(br_idx + 1);
				else
					body = null;
			} else if (br_idx == 0) {
				schema = null;
			} else {
				schema = body;
				body = null;
			}

		}

		// retrieve the serializer
		Serializer serializer = null;
		if (schema != null) {
			if (provider == null)
				throw new SerializationException(
						"No serialization provider available to resolve schema \""
								+ schema + "\"!");

			serializer = provider.getSerializer(SerializationProvider.STRING,
					schema);
			if (serializer == null)
				throw new SerializationException(
						"Cannot find serializer for schema \"" + schema
								+ "\" (format STRING)!");
		}

		// next lines: payload
		Object payload = null;
		if (serializer != null && body != null) {
			payload = serializer.deserialize(body);
		} else
			payload = body;

		XMPPPacket pkt = new XMPPPacket(payload,
				de.ovgu.dke.glue.api.transport.Packet.Priority.DEFERRABLE,
				URI.create(URI_PREFIX + msg.getFrom()), URI.create(URI_PREFIX
						+ msg.getTo()), id, msg);

		return pkt;
	}

	protected static String uri2jid(URI peer) throws SerializationException {
		if (!peer.toString().startsWith(URI_PREFIX))
			throw new SerializationException(
					"Target peer does not use the xmpp protocol: " + peer);

		return peer.toString().substring(URI_PREFIX.length());
	}

}
