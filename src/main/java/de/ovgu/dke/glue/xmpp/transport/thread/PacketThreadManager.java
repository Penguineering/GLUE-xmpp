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

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.transport.XMPPConn;
import de.ovgu.dke.glue.xmpp.transport.XMPPTransport;
import de.ovgu.dke.glue.xmpp.transport.capabilities.CapabilitiesPacketHandler;

public class PacketThreadManager implements ThreadIDGenerator {
	/**
	 * Check if the ID is local to the jid.
	 * 
	 * @param id
	 *            The packet thread ID to be checked
	 * @param jid
	 *            Local Jabber ID
	 * @return true if the packet thread ID is local.
	 * @throws NullPointerException
	 *             if one of the arguments is null
	 * @throws IllegalArgumentException
	 *             if the ID argument is not a valid XMPP packet thread ID
	 */
	public static boolean isLocalID(String id, URI jid) {
		final int _idx = id.lastIndexOf(':');
		if (_idx < 0)
			throw new IllegalArgumentException(
					"Missing \":\" in packet thread ID!");
		URI local_jid = URI.create(id.substring(0, _idx));

		return jid.equals(local_jid);
	}

	private final Map<String, XMPPPacketThread> threads;
	private final ThreadIDGenerator generator;

	public PacketThreadManager(final ThreadIDGenerator generator) {
		this.generator = generator;
		this.threads = new ConcurrentHashMap<String, XMPPPacketThread>();
	}

	public void registerThread(XMPPPacketThread thread) {
		threads.put(thread.getId(), thread);
	}

	public void removeThread(String id) {
		threads.remove(id);
	}

	public XMPPPacketThread retrieveThread(String id) {
		return threads.get(id);
	}

	public Collection<String> getThreadIDs() {
		return Collections.unmodifiableCollection(threads.keySet());
	}

	@Override
	public String generateThreadID() throws TransportException {
		return generator.generateThreadID();
	}

	@Override
	public String generateMetaThreadID() throws TransportException {
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
	public PacketThread addThread(XMPPConn connection, String id,
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
		final String id = this.generateThreadID();

		return addThread(connection, id, handler);
	}

	public PacketThread createMetaThread(XMPPTransport transport)
			throws TransportException {
		// fixed id for each transport
		final String id = this.generateMetaThreadID();

		// TODO capabilities packet handler
		final PacketHandler handler = new CapabilitiesPacketHandler();

		// TODO which endpoint?
		final XMPPConn con = (XMPPConn) transport.getConnection(null);

		return addThread(con, id, handler);
	}
}
