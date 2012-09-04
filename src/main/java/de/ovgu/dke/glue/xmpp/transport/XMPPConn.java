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

import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.api.transport.Connection;
import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.transport.thread.XMPPPacketThread;

public class XMPPConn implements Connection {
	private final String connection_schema;
	final XMPPTransport transport;

	/**
	 * This reflects the last known peer's JID, including a resource. The id
	 * changes depending on the last received message and is the target for
	 * subsequent replies.
	 */
	private URI effective_jid;

	public XMPPConn(final String connectionSchema, final XMPPTransport transport) {
		this.connection_schema = connectionSchema;
		this.transport = transport;

		this.effective_jid = transport.getPeer();
	}

	/**
	 * Get the connection schema for this connection, which must be set upon
	 * creation and cannot be changed.
	 * 
	 * @return The serialization schema, which cannot be {@code null}
	 */
	@Override
	public final String getConnectionSchema() {
		return connection_schema;
	}

	public URI getEffectiveJID() {
		synchronized (this) {
			return effective_jid;
		}
	}

	public void setEffectiveJID(URI jid) {
		synchronized (this) {
			this.effective_jid = jid;
		}
	}

	@Override
	public PacketThread createThread(PacketHandler handler)
			throws TransportException {
		return transport.createThread(this, handler);
	}

	@Override
	public Transport getTransport() {
		return transport;
	}

	@Override
	public URI getPeer() {
		return transport.getPeer();
	}

	public void disposeThread(PacketThread thread) {
		transport.disposeThread(thread);
	}

	public XMPPPacket createPacket(XMPPPacketThread pt, Object payload,
			Packet.Priority priority) throws TransportException {
		return new XMPPPacket(payload, priority, transport.getClient()
				.getLocalURI(), getEffectiveJID(), pt.getId(),
				this.getConnectionSchema());
	}

	@Override
	public boolean checkCapabilities() throws TransportException {
		return transport.checkCapabilities(getConnectionSchema());
	}

	@Override
	public String getSerializationFormat() {
		// TODO later: make this according to client capabilities
		return SerializationProvider.STRING;
	}

}
