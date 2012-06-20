package de.ovgu.dke.glue.xmpp.test;

import java.util.concurrent.Executors;

import de.ovgu.dke.glue.api.transport.TransportRegistry;

public class SenderTest {

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		Executors.newSingleThreadExecutor().execute(new SenderClient());
		// new SenderClient().run();
		// ClientStatus status;
		// while ((status = r.getStatus()) != ClientStatus.FINISHED) {
		// Thread.sleep(1000);
		// switch (status) {
		// case PREPARING:
		// System.out.println("Preparing");
		// break;
		// case CONNECTING:
		// System.out.println("Connecting");
		// break;
		// case LISTENING:
		// System.out.println("Listening");
		// break;
		// case FINISHED:
		// System.out.println("Finished");
		// break;
		// case ERROR:
		// System.out.println("Error");
		// break;
		// default:
		// break;
		// }
		// }
		Thread.sleep(5000);
		// dispose the transport factories
		TransportRegistry.getInstance().disposeAll();
	}

}
