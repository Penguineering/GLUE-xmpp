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
package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;

import net.jcip.annotations.ThreadSafe;

import org.jivesoftware.smack.packet.Packet;

import de.ovgu.dke.glue.util.transport.AbstractPacket;

@ThreadSafe
public class XMPPPacket extends AbstractPacket {
	private final URI sender;
	private final URI receiver;
	private final String thread_id;
	private final String schema;
	private final Packet xmpp_packet;

	public XMPPPacket(Object payload, Priority priority, URI sender,
			URI receiver, String thread_id, String schema) {
		this(payload, priority, sender, receiver, thread_id, schema, null);
	}

	public XMPPPacket(Object payload, Priority priority, URI sender,
			URI receiver, String thread_id, String schema, Packet xmpp_packet) {
		super(payload, priority);
		this.sender = sender;
		this.receiver = receiver;
		this.thread_id = thread_id;
		this.schema = schema;
		this.xmpp_packet = xmpp_packet;
	}

	public URI getSender() {
		return sender;
	}

	public URI getReceiver() {
		return receiver;
	}

	public String getThreadId() {
		return thread_id;
	}

	public String getSchema() {
		return schema;
	}

	public Packet getXMPPPacket() {
		return xmpp_packet;
	}

	@Override
	public Object getAttribute(String key) {
		if ("sender".equals(key))
			return sender;
		else if ("receiver".equals(key))
			return receiver;
		else if ("thread".equals(key))
			return thread_id;
		else if ("xmpp.packet".equals(key))
			return xmpp_packet;
		else
			return super.getAttribute(key);
	}
}
