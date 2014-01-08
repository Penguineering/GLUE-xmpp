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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.endpoint.Endpoint;
import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.Serializer;
import de.ovgu.dke.glue.api.transport.Connection;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketHandlerFactory;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.serialization.SmackMessageConverter;
import de.ovgu.dke.glue.xmpp.serialization.TextSmackMessageConverter;
import de.ovgu.dke.glue.xmpp.transport.thread.PacketThreadManager;
import de.ovgu.dke.glue.xmpp.transport.thread.XMPPPacketThread;

// follows http://xmpp.org/extensions/xep-0201.html for message threading
// TODO variables threading-verfahren korrekt umsetzen
public class XMPPTransport implements Transport {
	static final Log log = LogFactory.getLog(XMPPTransport.class);

	private final URI peer;
	private final XMPPClient client;

	private final PacketThreadManager threads;

	public XMPPTransport(final URI peer, final XMPPClient client,
			PacketThreadManager threads) {
		this.peer = peer;
		this.client = client;

		this.threads = threads;

		// register as middleware to handle capabilities
		/*
		 * final SchemaRecord record = SchemaRecord.valueOf(
		 * CapabilitiesSerializer.SCHEMA, SingletonPacketHandlerFactory
		 * .valueOf(new CapabilitiesPacketHandler()),
		 * SingleSerializerProvider.of(new TextCapabilitiesSerializer()));
		 * SchemaRegistry.getInstance().registerSchemaRecord(record);
		 */
	}

	public final URI getPeer() {
		return peer;
	}

	public XMPPClient getClient() {
		return client;
	}

	@Override
	public Connection getConnection(String schema) throws TransportException {
		return new XMPPConn(schema, this);
	}

	public PacketThread createThread(final Endpoint endpoint,
			final XMPPConn con, final PacketHandler handler)
			throws TransportException {
		try {
			PacketHandler hnd = handler;
			if (hnd == null) {
				PacketHandlerFactory factory = endpoint
						.getPacketHandlerFactory();
				if (factory != null)
					hnd = factory.createPacketHandler();
			}

			if (hnd == null)
				throw new TransportException(
						"Invalid value for packet handler: null!");

			return threads.createThread(endpoint, con, hnd);
		} catch (InstantiationException e) {
			throw new TransportException(
					"Could not instantiate packet handler: " + e.getMessage(),
					e);
		}
	}

	public void disposeThread(PacketThread thread) {
		if (thread != null)
			threads.removeThread(thread.getId());
	}

	/**
	 * 
	 * @param thread
	 * @param packet
	 *            Packet with serialized(!) payload
	 * @throws TransportException
	 */
	public void sendPacket(final XMPPPacketThread thread,
			final XMPPPacket packet) throws TransportException {

		// check local thread (lt)
		final XMPPPacketThread lt = threads.retrieveThread(thread.getId());

		final Connection con = thread.getConnection();

		if (lt == null || con.getTransport() != this)
			throw new TransportException("Packet thread " + thread.getId()
					+ " is not registered on this transport!");

		try {
			// get the serializer
			Serializer ser = lt.getEndpoint().getSerializationProvider()
					.getSerializer(con.getSerializationFormat());

			// create an XMPP message
			SmackMessageConverter conv = new TextSmackMessageConverter(lt
					.getEndpoint().getSerializationProvider());

			// log message content
			if (log.isDebugEnabled())
				log.debug("Sending packet to "
						+ packet.getReceiver().toASCIIString() + " "
						+ packet.getPayload());

			final Message msg = conv.toSmack(packet, ser);
			client.enqueuePacket(msg);
		} catch (InterruptedException e) {
			throw new TransportException("Error sending XMPP packet: "
					+ e.getMessage(), e);
		} catch (SerializationException e) {
			throw new TransportException("Error converting XMPP packet: "
					+ e.getMessage(), e);
		}
	}

	public XMPPPacket processSmackMessage(Message msg)
			throws TransportException {
		try {
			// create XMPP packet from smack packet
			XMPPPacket pkt = null;

			// get the converter
			SmackMessageConverter conv = new TextSmackMessageConverter(client
					.getDefaultEndpoint().getSerializationProvider());
			pkt = conv.fromSmack(msg);

			if (pkt.getThreadId() == null)
				throw new TransportException(
						"Received packet without thread ID!");

			// return the packet
			return pkt;
		} catch (SerializationException e) {
			throw new TransportException("Error converting Smack message: "
					+ e.getMessage(), e);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((peer == null) ? 0 : peer.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XMPPTransport other = (XMPPTransport) obj;
		if (peer == null) {
			if (other.peer != null)
				return false;
		} else if (!peer.equals(other.peer))
			return false;
		return true;
	}

	public boolean checkCapabilities(final String schema) {
		// TODO das muss ausgehandelt werden!
		/*
		 * try { final Serializer cap_ser = SchemaRegistry.getInstance()
		 * .getSerializationProvider(CapabilitiesSerializer.SCHEMA)
		 * .getSerializer(SerializationProvider.STRING);
		 * 
		 * final List<SerializationCapability> caps = SerializationCapability
		 * .retrieveSerializationCapabilities();
		 * 
		 * // serializers message final Object payload =
		 * cap_ser.serialize(caps);
		 * 
		 * // send to peer final XMPPPacketThread meta = (XMPPPacketThread)
		 * threads .createMetaThread(this);
		 * 
		 * // create an XMPP message SmackMessageConverter conv =
		 * this.getConverter();
		 * 
		 * XMPPConn con = (XMPPConn)
		 * getConnection(CapabilitiesSerializer.SCHEMA);
		 * 
		 * final Message msg = conv.toSmack( con.createPacket(meta, payload,
		 * Packet.Priority.HIGH), cap_ser); client.enqueuePacket(msg);
		 * 
		 * // TODO wait for response
		 * 
		 * // TODO check matching
		 * 
		 * // TODO respond accordingly
		 * 
		 * return true;
		 * 
		 * } catch (InterruptedException e) { throw new TransportException(
		 * "Error sending XMPP capabilities packet: " + e.getMessage(), e); }
		 * catch (SerializationException e) { throw new TransportException(
		 * "Error converting XMPP capabilities packet: " + e.getMessage(), e); }
		 */
		return true;
	}

	@Override
	public Endpoint getDefaultEndpoint() {
		return client.getDefaultEndpoint();
	}

}
