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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.serialization.CapabilitiesSerializer;
import de.ovgu.dke.glue.xmpp.transport.XMPPConn;
import de.ovgu.dke.glue.xmpp.transport.XMPPTransport;
import de.ovgu.dke.glue.xmpp.transport.capabilities.CapabilitiesPacketHandler;

public class PacketThreadManager implements ThreadIDGenerator {
	private final Map<XMPPThreadId, XMPPPacketThread> threads;

	private final ThreadIDGenerator generator;

	public PacketThreadManager(final ThreadIDGenerator generator) {
		this.generator = generator;
		this.threads = new ConcurrentHashMap<XMPPThreadId, XMPPPacketThread>();
	}

	public void registerThread(XMPPPacketThread thread) {
		threads.put(thread.getId(), thread);
	}

	public void removeThread(XMPPThreadId id) {
		threads.remove(id);
	}

	public XMPPPacketThread retrieveThread(XMPPThreadId id) {
		return threads.get(id);
	}

	public Collection<XMPPThreadId> getThreadIDs() {
		return Collections.unmodifiableCollection(threads.keySet());
	}

	@Override
	public XMPPThreadId generateThreadID() throws TransportException {
		return generator.generateThreadID();
	}

	@Override
	public XMPPThreadId generateMetaThreadID() throws TransportException {
		return generator.generateMetaThreadID();
	}

	/**
	 * Add a thread with a known (remote) ID
	 * 
	 * @param transport
	 * @param id
	 * @param handler
	 * @param schema
	 * @return
	 * @throws TransportException
	 */
	public PacketThread addThread(XMPPConn connection, XMPPThreadId id,
			PacketHandler handler) throws TransportException {
		// create packet thread
		XMPPPacketThread pt = new XMPPPacketThread(connection, id, handler);

		// register packet thread
		this.registerThread(pt);

		return pt;
	}

	public PacketThread createThread(XMPPConn connection, PacketHandler handler)
			throws TransportException {
		// generate id
		final XMPPThreadId id = this.generateThreadID();

		return addThread(connection, id, handler);
	}

	public PacketThread createMetaThread(XMPPTransport transport)
			throws TransportException {
		// fixed id for each transport
		final XMPPThreadId id = this.generateMetaThreadID();

		// TODO capabilities packet handler
		final PacketHandler handler = new CapabilitiesPacketHandler();

		final XMPPConn con = (XMPPConn) transport
				.getConnection(CapabilitiesSerializer.SCHEMA);

		// TODO this can be done via schema registry
		return addThread(con, id, handler);
	}
}
