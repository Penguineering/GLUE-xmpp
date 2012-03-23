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
import java.util.List;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.api.serialization.Serializer;
import de.ovgu.dke.glue.api.transport.Connection;
import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketHandlerFactory;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.util.serialization.SingleSerializerProvider;
import de.ovgu.dke.glue.xmpp.serialization.CapabilitiesSerializationProviderWrapper;
import de.ovgu.dke.glue.xmpp.serialization.CapabilitiesSerializer;
import de.ovgu.dke.glue.xmpp.serialization.SmackMessageConverter;
import de.ovgu.dke.glue.xmpp.serialization.TextCapabilitiesSerializer;
import de.ovgu.dke.glue.xmpp.serialization.TextSmackMessageConverter;
import de.ovgu.dke.glue.xmpp.transport.capabilities.SerializationCapability;
import de.ovgu.dke.glue.xmpp.transport.thread.PacketThreadManager;
import de.ovgu.dke.glue.xmpp.transport.thread.XMPPPacketThread;

// follows http://xmpp.org/extensions/xep-0201.html for message threading
// TODO variables threading-verfahren korrekt umsetzen
public class XMPPTransport implements Transport {
	private final URI peer;
	private final XMPPClient client;

	private SmackMessageConverter converter;

	private final PacketThreadManager threads;
	private final SerializationProvider serializers;

	private Serializer currentSerializer = null;

	public XMPPTransport(final URI peer, final XMPPClient client,
			PacketThreadManager threads, SerializationProvider serializers) {
		this.peer = peer;
		this.client = client;

		this.threads = threads;
		this.serializers = new CapabilitiesSerializationProviderWrapper(
				serializers, new SingleSerializerProvider(
						new TextCapabilitiesSerializer()));

		this.converter = new TextSmackMessageConverter();
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

	public PacketThread createThread(final XMPPConn con,
			final PacketHandler handler) throws TransportException {
		try {
			PacketHandler hnd = handler;
			if (hnd == null) {
				PacketHandlerFactory factory = client
						.getDefaultPacketHandlerFactory();
				if (factory != null)
					hnd = factory
							.createPacketHandler(con.getConnectionSchema());
			}

			if (hnd == null)
				throw new TransportException(
						"Invalid value for packet handler: null!");

			return threads.createThread(con, hnd);
		} catch (InstantiationException e) {
			throw new TransportException(
					"Could not instantiate packet handler: " + e.getMessage(),
					e);
		}
	}

	public void disposeThread(PacketThread thread) {
		if (thread != null)
			threads.removeThread(((XMPPPacketThread) thread).getId());
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

		if (lt == null || thread.getConnection().getTransport() != this)
			throw new TransportException("Packet thread " + thread.getId()
					+ " is not registered on this transport!");

		// create an XMPP message
		SmackMessageConverter conv = this.getConverter();

		try {
			final Message msg = conv.toSmack(packet, currentSerializer);
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
			SmackMessageConverter conv = this.getConverter();
			pkt = conv.fromSmack(msg, serializers);

			if (pkt.getThreadId() == null) {
				throw new TransportException("Packet thread ID for "
						+ pkt.getThreadId() + " could not be retrieved!");
			} else if (this.getConverter() == null) {
				// if we are fine here, store the converter
				this.setConverter(conv);
			}

			// return the packet
			return pkt;
		} catch (SerializationException e) {
			throw new TransportException("Error converting Smack message: "
					+ e.getMessage(), e);
		}

	}

	protected SmackMessageConverter getConverter() {
		return converter;
	}

	protected void setConverter(SmackMessageConverter converter) {
		this.converter = converter;
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

	@Override
	public Serializer getSerializer(final String schema) {
		if (currentSerializer == null)
			try {
				// our format is String
				final String format = SerializationProvider.STRING;

				try {
					currentSerializer = serializers.getSerializer(format,
							schema);
				} catch (SerializationException e) {
					throw new TransportException(
							"Error obtaining serializer for format " + format
									+ " and schema " + schema);
				}
			} catch (TransportException e) {
				// should not happen!

				// TODO use reporting
				e.printStackTrace();
			}

		return currentSerializer;
	}
	
	public boolean checkCapabilities(final String schema) throws TransportException {
		// TODO das muss ausgehandelt werden!
		if (serializers != null) {
			try {
				final Serializer cap_ser = serializers.getSerializer(
						SerializationProvider.STRING,
						CapabilitiesSerializer.SCHEMA);

				final List<SerializationCapability> caps = SerializationCapability
						.retrieveSerializationCapabilities(serializers);

				// serializers message
				final Object payload = cap_ser.serialize(caps);

				// send to peer
				final XMPPPacketThread meta = (XMPPPacketThread) threads
						.createMetaThread(this);

				// create an XMPP message
				SmackMessageConverter conv = this.getConverter();

				XMPPConn con = (XMPPConn) getConnection(CapabilitiesSerializer.SCHEMA);

				final Message msg = conv.toSmack(
						con.createPacket(meta, payload, Packet.Priority.HIGH),
						cap_ser);
				client.enqueuePacket(msg);

				// TODO wait for response

				// TODO check matching

				// TODO respond accordingly

				return true;

			} catch (InterruptedException e) {
				throw new TransportException(
						"Error sending XMPP capabilities packet: "
								+ e.getMessage(), e);
			} catch (SerializationException e) {
				throw new TransportException(
						"Error converting XMPP capabilities packet: "
								+ e.getMessage(), e);
			}
		}
		return false;
	}

}
