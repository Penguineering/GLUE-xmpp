package de.ovgu.dke.glue.xmpp.transport.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.ovgu.dke.glue.api.transport.TransportException;
import de.ovgu.dke.glue.xmpp.transport.XMPPPacketThread;

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

	@Override
	public String generateThreadID() throws TransportException {
		return generator.generateThreadID();
	}
}
