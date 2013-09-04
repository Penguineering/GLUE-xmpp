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
package de.ovgu.dke.glue.xmpp.transport.thread;

import de.ovgu.dke.glue.api.transport.Packet.Priority;
import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.transport.XMPPConn;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacket;
import de.ovgu.dke.glue.xmpp.transport.XMPPTransport;

//TODO synchronization
public class XMPPPacketThread extends PacketThread {

	private PacketHandler handler;

	public XMPPPacketThread(XMPPConn connection, String id,
			PacketHandler handler) throws TransportException {
		super(connection, id.toString());

		// if (id == null)
		// throw new NullPointerException("Packet thread id may not be null!");
		this.handler = handler;
	}

	/**
	 * Print the thread id as string representation.
	 */
	@Override
	public String toString() {
		return this.getId();
	}

	public PacketHandler getHandler() {
		return handler;
	}

	public void setHandler(PacketHandler handler) {
		this.handler = handler;
	}

	@Override
	public void dispose() {
		((XMPPConn) getConnection()).disposeThread(this);
	}

	@Override
	public void sendSerializedPayload(Object payload, Priority priority)
			throws TransportException {
		final XMPPTransport transport = (XMPPTransport) getConnection()
				.getTransport();
		if (transport == null)
			throw new TransportException(
					"Transport is not available for packet thread "
							+ this.getId());

		final XMPPPacket pkt = ((XMPPConn) getConnection()).createPacket(this,
				payload, priority);

		transport.sendPacket(this, pkt);
	}
}
