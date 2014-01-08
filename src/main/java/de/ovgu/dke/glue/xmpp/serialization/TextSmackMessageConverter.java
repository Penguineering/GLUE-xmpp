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
package de.ovgu.dke.glue.xmpp.serialization;

import java.net.URI;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.api.serialization.Serializer;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacket;

public class TextSmackMessageConverter implements SmackMessageConverter {
	protected static String URI_PREFIX = "xmpp:";

	private final SerializationProvider serializationProvider;

	public TextSmackMessageConverter(SerializationProvider serializationProvider) {
		super();
		this.serializationProvider = serializationProvider;
	}

	@Override
	public Message toSmack(XMPPPacket pkt, Serializer serializer)
			throws SerializationException {
		// create the message
		Message msg = new Message(uri2jid(pkt.getReceiver()));
		msg.setType(Message.Type.chat);

		// add payload
		StringBuilder payload = new StringBuilder();

		// first line: thread id
		payload.append(pkt.getThreadId());

		// second line: schema
		payload.append("\n");
		payload.append(pkt.getSchema() == null ? "\n" : pkt.getSchema());

		// next lines: payload
		if (pkt.getPayload() != null) {
			payload.append("\n");
			payload.append(pkt.getPayload());
		}

		msg.setBody(payload.toString());

		return msg;
	}

	@Override
	public XMPPPacket fromSmack(Message msg) throws SerializationException {
		String body = msg.getBody();
		String id = null;
		String schema = null;

		String _id = null;
		// first line: thread id
		if (body != null) {
			// take the first line from payload
			int br_idx = body.indexOf('\n');
			if (br_idx >= 0) {
				_id = body.substring(0, br_idx);
				if (body.length() > br_idx)
					body = body.substring(br_idx + 1);
				else
					body = null;
			} else if (br_idx == 0) {
				_id = null;
			} else {
				_id = body;
				body = null;
			}
		}

		// TODO check ID syntax
		id = _id;

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

		// TODO anpassen
		// retrieve the serializer
		Serializer serializer = null;
		if (schema != null) {
			if (serializationProvider == null)
				throw new SerializationException(
						"No serialization provider available to resolve schema \""
								+ schema + "\"!");

			serializer = serializationProvider
					.getSerializer(SerializationProvider.STRING);
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
						+ msg.getTo()), id, schema, msg);

		return pkt;
	}

	protected static String uri2jid(URI peer) throws SerializationException {
		if (!peer.toString().startsWith(URI_PREFIX))
			throw new SerializationException(
					"Target peer does not use the xmpp protocol: " + peer);

		return peer.toString().substring(URI_PREFIX.length());
	}

}
