package de.ovgu.dke.glue.xmpp.test;

import java.io.IOException;
import java.net.URI;

import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketHandlerFactory;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportRegistry;
import de.ovgu.dke.glue.xmpp.transport.XMPPTransportFactory;

public class TestClient {
	public static void main(String args[]) throws TransportException, IOException {

		// initialize and register transport factory
		new XMPPTransportFactory(new EchoPacketHandlerFactory())
				.registerAsDefault();

		// get a transport
		final Transport xmpp = TransportRegistry
				.getInstance()
				.getDefaultTransportFactory()
				.createTransport(
						URI.create("xmpp:shaun@bison.cs.uni-magdeburg.de"));

		// create a packet thread
		final PacketThread thread = xmpp
				.createThread(PacketThread.DEFAULT_HANDLER);

		// send something
		thread.send("Hallo Welt!", Packet.Priority.DEFAULT);

		// wait for responses
		System.out.println("Press any key...");
		System.in.read();

		// finish thread
		thread.dispose();

		// dispose the transport registry
		((XMPPTransportFactory) TransportRegistry.getInstance()
				.getDefaultTransportFactory()).dispose();

	}
}

class EchoPacketHandlerFactory implements PacketHandlerFactory {
	private static PacketHandler echoHandler = null;

	@Override
	public synchronized PacketHandler createPacketHandler()
			throws TransportException {
		if (echoHandler == null)
			echoHandler = new EchoHandler();

		return echoHandler;
	}
}

class EchoHandler implements PacketHandler {
	@Override
	public void handle(PacketThread packetThread, Packet packet)
			throws TransportException {
		packetThread.send(packet.getPayload(), packet.getPriority());
	}
}
