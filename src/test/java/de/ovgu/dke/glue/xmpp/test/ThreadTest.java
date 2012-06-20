package de.ovgu.dke.glue.xmpp.test;

import java.util.concurrent.Executors;

import de.ovgu.dke.glue.api.transport.TransportRegistry;

public class ThreadTest {

	// TODO: schema registry is a singleton -> schema records for same schema
	// overwrite themselves but are needed when processing in threads, otherwise
	// for one schema only one and for all transports the same packethanlder and
	// serializationprovider can be installed
	public static void main(String[] args) {
		System.out.println("Setting up receiver");
		ReceiverClient receiver = new ReceiverClient();
		Executors.newSingleThreadExecutor().execute(receiver);

		while (receiver.getStatus() != ClientStatus.LISTENING) {
			System.out.println("RECEIVER " + receiver.getStatus());
			sleep(1000);
		}
		System.out.println("Setting up sender");
		SenderClient sender = new SenderClient();
		Executors.newSingleThreadExecutor().execute(sender);

		while (receiver.getStatus() != ClientStatus.FINISHED) {
			System.out.println("RECEIVER " + receiver.getStatus());
			System.out.println("SENDER " + sender.getStatus());
			sleep(1000);
		}
		TransportRegistry.getInstance().disposeAll();
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
