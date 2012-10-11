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

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.Serializer;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacket;

/**
 * Conversion between GLUE and Smack messages.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 */
public interface SmackMessageConverter {
	/**
	 * Create a smack packet from an XMPP packet with serialized payload.
	 * 
	 * @param pkt
	 *            The packet to convert
	 * @param serializer
	 *            The serializer.
	 * @return Smack packet which can be sent
	 * @throws SerializationException
	 *             if the peer URI is not an XMPP URI
	 */
	public Message toSmack(final XMPPPacket pkt, Serializer serializer)
			throws SerializationException;

	/**
	 * Create an XMPP packet from a smack message. The payload will be
	 * deserialized.
	 * 
	 * @param msg
	 *            The incoming message.
	 * @param provider
	 *            The serialization provider.
	 * @return XMPP packet with de-serialized payload.
	 * @throws SerializationException
	 *             If de-serialization fails.
	 */
	public XMPPPacket fromSmack(final Message msg)
			throws SerializationException;
}
