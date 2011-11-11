package de.ovgu.dke.glue.xmpp.test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketHandlerFactory;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportFactory;
import de.ovgu.dke.glue.api.transport.TransportRegistry;
import de.ovgu.dke.glue.xmpp.transport.XMPPTransportFactory;

public class TestClient {
	public static void main(String args[]) throws TransportException,
			IOException {

		// initialize and register transport factory
		initTransportFactory(
				"de.ovgu.dke.glue.xmpp.transport.XMPPTransportFactory",
				new EchoPacketHandlerFactory(), true);

		// get a transport
		final Transport xmpp = TransportRegistry.getInstance()
				.getDefaultTransportFactory().createTransport(
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

		// dispose the transport factory
		((XMPPTransportFactory) TransportRegistry.getInstance()
				.getDefaultTransportFactory()).dispose();

	}

	// TODO in die registry
	public static TransportFactory initTransportFactory(String factoryClass,
			PacketHandlerFactory handlerFactory, boolean asDefault)
			throws TransportException {
		try {
			// get the class
			final Class<?> clazz = Class.forName(factoryClass);

			// create instance
			final Constructor<?> con = clazz.getConstructor();
			final XMPPTransportFactory factory = (XMPPTransportFactory) con
					.newInstance();

			// some setup
			if (factory != null) {
				factory.setDefaultPacketHandlerFactory(handlerFactory);
				if (asDefault)
					factory.registerAsDefault();
			}

			return factory;
		} catch (ClassNotFoundException e) {
			throw new TransportException("Factory class " + factoryClass
					+ " could not be found!", e);
		} catch (SecurityException e) {
			throw new TransportException(
					"Security exception on accessing constructor for "
							+ factoryClass + ": " + e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			throw new TransportException("Method could not be found: "
					+ e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new TransportException(
					"Illegal arguments calling constructor for " + factoryClass
							+ ": " + e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new TransportException("Could not instantiate "
					+ factoryClass + ": " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new TransportException("Illegal access: " + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new TransportException("Invocation target exception: "
					+ e.getMessage(), e);
		}

	}
}

class EchoPacketHandlerFactory implements PacketHandlerFactory {
	private static PacketHandler echoHandler = null;

	@Override
	public synchronized PacketHandler createPacketHandler()
			throws InstantiationException {
		if (echoHandler == null)
			echoHandler = new EchoHandler();

		return echoHandler;
	}
}

class EchoHandler implements PacketHandler {
	@Override
	public void handle(PacketThread packetThread, Packet packet) {
		try {
			packetThread.send(packet.getPayload(), packet.getPriority());
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
