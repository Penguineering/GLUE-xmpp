package de.ovgu.dke.glue.xmpp.serialization;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacket;

public class TextThreadSmackPacketConverter extends
		AbstractSmackPacketConverter {

	@Override
	protected void addPayloadAndThread(Message msg, XMPPPacket pkt)
			throws SerializationException {
		StringBuffer payload = new StringBuffer();

		payload.append(pkt.thread_id);

		if (pkt.getPayload() != null) {
			payload.append("\n");
			payload.append(pkt.getPayload());
		}

		msg.setBody(payload.toString());
	}

	@Override
	protected String getPayload(Message msg) throws SerializationException {
		String payload = msg.getBody();

		if (payload != null) {
			// take the first line from payload
			int br_idx = payload.indexOf('\n');
			if (br_idx > 0)
				payload = payload.substring(br_idx + 1);
			else
				payload = null;
		}

		return payload;
	}

	@Override
	protected String getThread(Message msg) throws SerializationException {
		String payload = msg.getBody();
		String id = null;

		if (payload != null) {
			// take the first line from payload
			int br_idx = payload.indexOf('\n');
			if (br_idx > 0)
				id = payload.substring(0, br_idx);
			else
				id = payload;
		}

		return id;
	}

}
