package de.ovgu.dke.glue.xmpp.serialization;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacket;

public class XMPPThreadSmackPacketConverter extends
		AbstractSmackPacketConverter {

	@Override
	protected void addPayloadAndThread(Message msg, XMPPPacket pkt)
			throws SerializationException {
		msg.setThread(pkt.thread_id);

		if (pkt.getPayload() != null)
			msg.setBody(pkt.getPayload().toString());

	}

	@Override
	protected String getPayload(Message msg) throws SerializationException {
		return msg.getBody();
	}

	@Override
	protected String getThread(Message msg) throws SerializationException {
		return msg.getThread();
	}
}
