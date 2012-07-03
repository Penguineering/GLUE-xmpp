package de.ovgu.dke.glue.xmpp.test;

// TODO test case has to be defined
public class ThreadTest {

	// TODO: schema registry is a singleton -> schema records for same schema
	// overwrite themselves but are needed when processing in threads, otherwise
	// for one schema only one and for all transports the same packethanlder and
	// serializationprovider can be installed
	public static void main(String[] args) {
		// System.out.println("Setting up receiver");
		// NormalReceiverPeer receiver = new NormalReceiverPeer();
		// Executors.newSingleThreadExecutor().execute(receiver);
		//
		// while (receiver.getStatus() != PeerStatus.LISTENING) {
		// System.out.println("RECEIVER " + receiver.getStatus());
		// sleep(1000);
		// }
		// System.out.println("Setting up sender");
		// NormalSenderPeer sender = new NormalSenderPeer();
		// Executors.newSingleThreadExecutor().execute(sender);
		//
		// while (receiver.getStatus() != PeerStatus.FINISHED) {
		// System.out.println("RECEIVER " + receiver.getStatus());
		// System.out.println("SENDER " + sender.getStatus());
		// sleep(1000);
		// }
		// TransportRegistry.getInstance().disposeAll();
	}

	// private static void sleep(long millis) {
	// try {
	// Thread.sleep(millis);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }

}
