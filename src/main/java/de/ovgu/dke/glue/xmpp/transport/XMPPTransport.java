package de.ovgu.dke.glue.xmpp.transport;

import java.net.URI;
import java.util.List;

import org.jivesoftware.smack.packet.Message;

import de.ovgu.dke.glue.api.serialization.SerializationException;
import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.api.serialization.Serializer;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketHandlerFactory;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.serialization.SmackMessageConverter;
import de.ovgu.dke.glue.xmpp.serialization.TextThreadSmackPacketConverter;
import de.ovgu.dke.glue.xmpp.serialization.XMPPThreadSmackPacketConverter;
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
		this.serializers = serializers;

		this.converter = new TextThreadSmackPacketConverter();
	}

	public final URI getPeer() {
		return peer;
	}

	public XMPPClient getClient() {
		return client;
	}

	@Override
	public PacketThread createThread(PacketHandler handler)
			throws TransportException {
		try {
			PacketHandler hnd = handler;
			if (hnd == null) {
				PacketHandlerFactory factory = client
						.getDefaultPacketHandlerFactory();
				if (factory != null)
					hnd = factory.createPacketHandler();
			}

			if (hnd == null)
				throw new TransportException(
						"Invalid value for packet handler: null!");

			return threads.createThread(this, hnd);
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

	public void sendPacket(final XMPPPacketThread thread,
			final XMPPPacket packet) throws TransportException {
		// check thread
		final XMPPPacketThread lt = threads.retrieveThread(thread.getId());

		if (lt == null || thread.getTransport() != this)
			throw new TransportException("Packet thread " + thread.getId()
					+ " is not registered on this transport!");

		// create an XMPP message
		SmackMessageConverter conv = this.converter;
		if (conv == null) {
			// be on the safe side and encode thread IDs in the message body
			conv = new TextThreadSmackPacketConverter();
			// a converter will be stored by the client upon receipt of a
			// message
		}

		try {
			final Message msg = conv.toSmack(packet);
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
			// if there is no converter, we try different methods:
			// first the XMPP threading (XEP-0201),
			// then thread info in body
			if (conv == null) {
				// try the XMPP threading converter first
				conv = new XMPPThreadSmackPacketConverter();
				pkt = conv.fromSmack(msg, currentSerializer);

				// use the text based threading converter if there was no thread
				// attached
				if (pkt.thread_id == null || pkt.thread_id.isEmpty()) {
					conv = new TextThreadSmackPacketConverter();
					pkt = conv.fromSmack(msg, currentSerializer);
				}
			} else
				pkt = conv.fromSmack(msg, currentSerializer);

			if (pkt.thread_id == null) {
				throw new TransportException("Packet thread ID for "
						+ pkt.thread_id + " could not be retrieved!");
			} else if (this.getConverter() == null) {
				// if we are fine here, store the converter
				this.setConverter(conv);
			}
			
			// deserialize the content

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
	public boolean checkCapabilities() throws TransportException {
		// TODO das muss ausgehandelt werden!
		if (serializers != null) {

			// our format is String
			final String format = SerializationProvider.STRING;

			// get the list
			final List<String> schemas = serializers.getSchemas(format);
			final String schema = schemas.get(0);

			try {
				currentSerializer = serializers.getSerializer(format, schema);
				return true;
			} catch (SerializationException e) {
				throw new TransportException(
						"Error obtaining serializer for format " + format
								+ " and schema " + schema);
			}
		}
		return false;
	}

	@Override
	public Serializer getSerializer() {
		if (currentSerializer == null)
			try {
				checkCapabilities();
			} catch (TransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return currentSerializer;
	}
}
