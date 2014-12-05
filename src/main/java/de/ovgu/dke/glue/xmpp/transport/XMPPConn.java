/*
 * Copyright 2012-2014 Stefan Haun, Thomas Low, Sebastian Stober, Andreas Nürnberger
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

import de.ovgu.dke.glue.api.endpoint.Endpoint;
import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.api.transport.Connection;
import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.transport.thread.XMPPPacketThread;

/**
 * XMPP implementation of a Connection.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 * 
 */
public class XMPPConn implements Connection {
	final XMPPTransport transport;
	final Endpoint endpoint;

	/**
	 * This reflects the last known peer's JID, including a resource. The id
	 * changes depending on the last received message and is the target for
	 * subsequent replies.
	 */
	private URI effective_jid;

	/**
	 * Create a connection on a specific end-point for an XMPP transport.
	 * 
	 * @param endpoint
	 * @param transport
	 * @throws NullPointerException
	 *             if end-point or transport are @code{null}.
	 */
	public XMPPConn(final Endpoint endpoint, final XMPPTransport transport) {
		super();

		if (endpoint == null)
			throw new NullPointerException(
					"Endpoint argument must not be null!");
		this.endpoint = endpoint;

		if (transport == null)
			throw new NullPointerException(
					"Transport argument must not be null!");
		this.transport = transport;

		this.effective_jid = transport.getPeer();
	}

	@Override
	public Endpoint getEndpoint() {
		return endpoint;
	}

	@Override
	public URI getPublicAddress() {
		// get the client's local URI
		final XMPPTransport _transport = (XMPPTransport) getTransport();
		final XMPPClient _client = _transport.getClient();
		final URI _local = _client.getLocalURI();

		return _local;
	}

	@Override
	public String getSerializationFormat() {
		// TODO later: make this according to client capabilities
		return SerializationProvider.STRING;
	}

	/**
	 * Get the effective Jabber ID as URI, i.e. the ID an XMPP server has
	 * assigned if the peer URI was incomplete.
	 * 
	 * @return
	 */
	public URI getEffectiveJID() {
		synchronized (this) {
			return effective_jid;
		}
	}

	/**
	 * Set the effective Jabber ID from its URI, i.e. the ID an XMPP server has
	 * assigned if the peer URI was incomplete.
	 */
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
				.getLocalURI(), getEffectiveJID(), pt.getId(), pt
				.getConnection().getEndpoint().getSchema());
	}

	@Override
	public boolean checkCapabilities() throws TransportException {
		// return transport.checkCapabilities(getConnectionSchema());
		// TODO returning true while capabilities are not really negotiated
		return true;
	}
}
