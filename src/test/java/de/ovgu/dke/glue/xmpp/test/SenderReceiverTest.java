package de.ovgu.dke.glue.xmpp.test;

import java.util.concurrent.Executors;

public class SenderReceiverTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Executors.newSingleThreadExecutor().execute(new ReceiverClient());

		Executors.newSingleThreadExecutor().execute(new SenderClient());

	}

}
