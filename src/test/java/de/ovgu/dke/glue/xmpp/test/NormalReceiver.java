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

public class NormalReceiver extends AbstractPeer implements Runnable {

	private String message = null;

	public NormalReceiver(String identifier, String propertiesKey,
			String pathToProperties, String factoryClass) {
		super(identifier, propertiesKey, pathToProperties, factoryClass);
	}

	@Override
	public void run() {
		// initialize and register transport factory
		try {
			setStatus(PeerStatus.PREPARING);
			Properties prop = new Properties();
			prop.setProperty(propertiesKey, pathToProperties);

			TransportRegistry.getInstance().loadTransportFactory(factoryClass,
					prop, TransportRegistry.NO_DEFAULT, identifier);
			setStatus(PeerStatus.CONNECTING);
			// register the "middle-ware"
			SchemaRegistry.getInstance().registerSchemaRecord(
					SchemaRecord.valueOf("http://dke.ovgu.de/glue/xmpp/test",
							SingletonPacketHandlerFactory
									.valueOf(new ToConsolePacketHandler()),
							SingleSerializerProvider.of(NullSerializer
									.of(SerializationProvider.STRING))));
			setStatus(PeerStatus.LISTENING);
		} catch (TransportException e) {
			e.printStackTrace();
			setStatus(PeerStatus.ERROR);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			setStatus(PeerStatus.ERROR);
		}
	}

	class ToConsolePacketHandler implements PacketHandler {

		@Override
		public void handle(PacketThread packetThread, Packet packet) {
			message = String.valueOf(packet.getPayload());
			setStatus(PeerStatus.FINISHED);
		}

	}

	public Object getMessage() {
		return message;
	}

}
