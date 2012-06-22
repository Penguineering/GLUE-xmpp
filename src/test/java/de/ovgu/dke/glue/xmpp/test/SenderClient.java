package de.ovgu.dke.glue.xmpp.test;

import java.net.URI;
import java.util.Properties;

import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.api.transport.Connection;
import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.SchemaRecord;
import de.ovgu.dke.glue.api.transport.SchemaRegistry;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportRegistry;
import de.ovgu.dke.glue.util.serialization.NullSerializer;
import de.ovgu.dke.glue.util.serialization.SingleSerializerProvider;
import de.ovgu.dke.glue.xmpp.config.XMPPPropertiesConfigurationLoader;

public class SenderClient implements Runnable {

	private ClientStatus status = ClientStatus.PREPARING;

	public synchronized ClientStatus getStatus() {
		return status;
	}

	public synchronized void setStatus(ClientStatus status) {
		this.status = status;
	}

	@Override
	public void run() {
		// initialize and register transport factory
		try {
			Properties prop = new Properties();
			prop.setProperty(XMPPPropertiesConfigurationLoader.CONFIG_PATH,
					"src/main/config/peer1@jabber.org.properties");

			TransportRegistry.getInstance().loadTransportFactory(
					"de.ovgu.dke.glue.xmpp.transport.XMPPTransportFactory",
					prop, TransportRegistry.NO_DEFAULT, "SENDER");

			// register the "middle-ware"
			SchemaRegistry.getInstance().registerSchemaRecord(
					SchemaRecord.valueOf(
							"http://dke.ovgu.de/glue/xmpp/test",
							new EchoPacketHandlerFactory(),
							new SingleSerializerProvider(NullSerializer
									.valueOf(SerializationProvider.STRING))));

			// get a transport
			final Transport xmpp = TransportRegistry.getInstance()
					.getTransportFactory("SENDER")
					.createTransport(URI.create("xmpp:peer2@jabber.org"));
			// final Transport xmpp =
			// TransportRegistry.getInstance().getTransportFactory("SENDER").createTransport(
			// URI.create("xmpp:basti.dorok@googlemail.com"));
			setStatus(ClientStatus.CONNECTING);
			// create a connection
			final Connection con = xmpp
					.getConnection("http://dke.ovgu.de/glue/xmpp/test");
			setStatus(ClientStatus.SENDING);
			// create a packet thread
			final PacketThread thread = con
					.createThread(PacketThread.DEFAULT_HANDLER);

			// send something
			thread.send("Hallo Welt!", Packet.Priority.DEFAULT);

			// finish thread
			// thread.dispose();

			setStatus(ClientStatus.FINISHED);
		} catch (TransportException e) {
			e.printStackTrace();
			setStatus(ClientStatus.ERROR);
		} catch (ClassNotFoundException e) {
			// TODO check
			e.printStackTrace();
			setStatus(ClientStatus.ERROR);
		}

	}

}
