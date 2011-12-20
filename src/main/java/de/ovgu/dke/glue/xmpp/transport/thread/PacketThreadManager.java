package de.ovgu.dke.glue.xmpp.transport.thread;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.ovgu.dke.glue.api.transport.PacketHandler;
import de.ovgu.dke.glue.api.transport.PacketThread;
import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.transport.XMPPTransport;
import de.ovgu.dke.glue.xmpp.transport.capabilities.CapabilitiesPacketHandler;

public class PacketThreadManager implements ThreadIDGenerator {
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
	 * @return
	 * @throws TransportException
	 */
	public PacketThread addThread(XMPPTransport transport, String id,
			PacketHandler handler) throws TransportException {
		// create packet thread
		XMPPPacketThread pt = new XMPPPacketThread(transport, id, handler);

		// register packet thread
		this.registerThread(pt);

		return pt;
	}

	public PacketThread createThread(XMPPTransport transport,
			PacketHandler handler) throws TransportException {
		// generate id
		final String id = this.generateThreadID();

		return addThread(transport, id, handler);
	}

	public PacketThread createMetaThread(XMPPTransport transport)
			throws TransportException {
		// fixed id for each transport
		final String id = this.generateMetaThreadID();

		// TODO capabilities packet handler
		final PacketHandler handler = new CapabilitiesPacketHandler();

		return addThread(transport, id, handler);
	}
}
