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
package de.ovgu.dke.glue.xmpp.test;

import java.io.IOException;
import java.net.URI;

import de.ovgu.dke.glue.api.transport.Connection;
import de.ovgu.dke.glue.api.transport.Packet;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketHandlerFactory;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.Transport;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.api.transport.TransportRegistry;

public class TestClient {
	public static void main(String args[]) throws TransportException,
			IOException, ClassNotFoundException {

		// initialize and register transport factory
		TransportRegistry.getInstance().loadTransportFactory(
				"de.ovgu.dke.glue.xmpp.transport.XMPPTransportFactory", null,
				TransportRegistry.AS_DEFAULT, TransportRegistry.DEFAULT_KEY);

		// TODO register the "middle-ware"
		/*
		 * SchemaRegistry.getInstance().registerSchemaRecord(
		 * SchemaRecord.valueOf( "http://dke.ovgu.de/glue/xmpp/test", new
		 * EchoPacketHandlerFactory(),
		 * SingleSerializerProvider.of(NullSerializer
		 * .of(SerializationProvider.STRING))));
		 */

		// get a transport
		final Transport xmpp = TransportRegistry.getDefaultTransportFactory()
				.createTransport(
						URI.create("xmpp:shaun@bison.cs.uni-magdeburg.de"));

		// create a connection
		final Connection con = xmpp.getConnection(null);

		// create a packet thread
		final PacketThread thread = con
				.createThread(PacketThread.DEFAULT_HANDLER);

		// send something
		thread.send("Hallo Welt!", Packet.Priority.DEFAULT);

		// wait for responses
		System.out.println("Press any key...");
		System.in.read();

		// finish thread
		thread.dispose();

		// dispose the transport factory
		TransportRegistry.getInstance().disposeAll();
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
