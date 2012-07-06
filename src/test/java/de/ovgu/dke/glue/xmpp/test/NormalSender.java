package de.ovgu.dke.glue.xmpp.test;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.Executors;

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

public class NormalSender extends AbstractPeer implements Runnable {

	private String peer;
	private Object payload;

	public NormalSender(String identifier, String propertiesKey,
			String pathToProperties, String factoryClass, String peer,
			Object payload) {
		super(identifier, propertiesKey, pathToProperties, factoryClass);
		this.peer = peer;
		this.payload = payload;
	}

	@Override
	public void run() {
		// initialize and register transport factory
		try {
			setStatus(PeerStatus.PREPARING);
			// set own config path via properties file
			Properties prop = new Properties();
			prop.setProperty(XMPPPropertiesConfigurationLoader.CONFIG_PATH,
					pathToProperties);

			// register the transport factory
			TransportRegistry.getInstance().loadTransportFactory(factoryClass,
					prop, TransportRegistry.NO_DEFAULT, identifier);

			// register the "middle-ware"
			SchemaRegistry.getInstance().registerSchemaRecord(
					SchemaRecord.valueOf("http://dke.ovgu.de/glue/xmpp/test",
							new EchoPacketHandlerFactory(),
							SingleSerializerProvider.of(NullSerializer
									.of(SerializationProvider.STRING))));

			// get a transport
			setStatus(PeerStatus.CONNECTING);
			final Transport xmpp = TransportRegistry.getInstance()
					.getTransportFactory(identifier)
					.createTransport(URI.create(peer));
			// create a connection
			final Connection con = xmpp
					.getConnection("http://dke.ovgu.de/glue/xmpp/test");
			// create a packet thread
			setStatus(PeerStatus.SENDING);
			final PacketThread thread = con
					.createThread(PacketThread.DEFAULT_HANDLER);

			// send something
			thread.send(payload, Packet.Priority.DEFAULT);

			// finish thread
			thread.dispose();

			setStatus(PeerStatus.FINISHED);
		} catch (TransportException e) {
			e.printStackTrace();
			setStatus(PeerStatus.ERROR);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			setStatus(PeerStatus.ERROR);
		}

	}

	public static void main(String[] args) throws InterruptedException {

		NormalSender s = new NormalSender(args[0], args[1], args[2],
				args[3], args[4], args[5]);
		Executors.newSingleThreadExecutor().execute(s);
		PeerStatus status;
		do {
			status = s.getStatus();
			// redirect status to output stream
			System.out.println("STATUS_" + status);
		} while (status != PeerStatus.FINISHED);
		// dispose the transport factories
		TransportRegistry.getInstance().disposeAll();
	}

}
