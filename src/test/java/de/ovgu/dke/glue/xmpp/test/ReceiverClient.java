package de.ovgu.dke.glue.xmpp.test;

import java.util.Properties;

import de.ovgu.dke.glue.api.serialization.SerializationProvider;
import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.SchemaRecord;
import de.ovgu.dke.glue.api.transport.SchemaRegistry;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportRegistry;
import de.ovgu.dke.glue.util.serialization.NullSerializer;
import de.ovgu.dke.glue.util.serialization.SingleSerializerProvider;
import de.ovgu.dke.glue.util.transport.SingletonPacketHandlerFactory;
import de.ovgu.dke.glue.xmpp.config.XMPPPropertiesConfigurationLoader;

public class ReceiverClient implements Runnable {

	public boolean received = false;

	@Override
	public void run() {
		// initialize and register transport factory
		try {
			Properties prop = new Properties();
			prop.setProperty(XMPPPropertiesConfigurationLoader.CONFIG_PATH,
					"src/main/config/peer2@jabber.org.properties");

			TransportRegistry.getInstance().loadTransportFactory(
					"de.ovgu.dke.glue.xmpp.transport.XMPPTransportFactory",
					prop, TransportRegistry.AS_DEFAULT, "RECEIVER");

			// register the "middle-ware"
			SchemaRegistry.getInstance().registerSchemaRecord(
					SchemaRecord.valueOf(
							"http://dke.ovgu.de/glue/xmpp/test",
							SingletonPacketHandlerFactory
									.valueOf(new ToConsolePacketHandler()),
							new SingleSerializerProvider(NullSerializer
									.valueOf(SerializationProvider.STRING))));
		} catch (TransportException e) {
			e.printStackTrace();
		}

	}

	class ToConsolePacketHandler implements PacketHandler {

		@Override
		public void handle(PacketThread packetThread, Packet packet) {
			System.out.println("Receiver: " + packet.getPayload());
			received = true;
		}

	}

}
