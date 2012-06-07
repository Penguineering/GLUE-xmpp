package de.ovgu.dke.glue.xmpp.test;

import java.util.concurrent.Executors;

import de.ovgu.dke.glue.api.transport.TransportRegistry;

public class SenderReceiverTest {

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		ReceiverClient r = new ReceiverClient();
		Executors.newSingleThreadExecutor().execute(r);

		Executors.newSingleThreadExecutor().execute(new SenderClient());

		while(!r.received)
			Thread.sleep(10000);

		// dispose the transport factories
		TransportRegistry.getInstance().disposeAll();
	}

}
