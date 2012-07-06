package de.ovgu.dke.glue.xmpp.test;

import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.Executors;

import org.junit.Test;

import de.ovgu.dke.glue.api.transport.TransportRegistry;
import de.ovgu.dke.glue.xmpp.config.XMPPPropertiesConfigurationLoader;
import de.ovgu.dke.glue.xmpp.test.AbstractPeer.PeerStatus;

public class MessageTest implements PropertyChangeListener {

	private PeerStatus receiverStatus = PeerStatus.PREPARING;
	private PeerStatus senderStatus = PeerStatus.PREPARING;

	private StreamStatusConverter streamStatusConv;

	@Test
	public void test() throws IOException, InterruptedException {
		String message = "Hello World! We're sending between different VMs.";

		NormalReceiver r = new NormalReceiver("RECEIVER",
				XMPPPropertiesConfigurationLoader.CONFIG_PATH,
				"src/main/config/peer2@jabber.org.properties",
				"de.ovgu.dke.glue.xmpp.transport.XMPPTransportFactory");
		r.addPropertyChangeListener(this);
		Executors.newSingleThreadExecutor().execute(r);
		boolean forked = false;
		while (receiverStatus != PeerStatus.FINISHED
				|| senderStatus != PeerStatus.FINISHED) {
			switch (receiverStatus) {
			case LISTENING:
				if (!forked) {
					forked = true;
					createSender(
							"de.ovgu.dke.glue.xmpp.test.NormalSender",
							"SENDER",
							XMPPPropertiesConfigurationLoader.CONFIG_PATH,
							"src/main/config/peer1@jabber.org.properties",
							"de.ovgu.dke.glue.xmpp.transport.XMPPTransportFactory",
							"xmpp:peer2@jabber.org", message);
				}
				break;
			case ERROR:
				fail("An unexpected error occurred in receiver peer.");
				break;
			default:
				break;
			}
		}
		streamStatusConv.stop();
		System.out.println("Message: " + r.getMessage());
		assertEquals(message, r.getMessage());
		// dispose the transport factories
		TransportRegistry.getInstance().disposeAll();
	}

	private void createSender(String mainClass, String identifier,
			String propertiesKey, String pathToProperties, String factoryClass,
			String peer, Object payload) throws IOException {
		final JavaProcessBuilder javaProcessBuilder = new JavaProcessBuilder();
		if (System.getProperty("os.name").toLowerCase().equals("mac os x")) {
			javaProcessBuilder
					.setJavaRuntime("/System/Library/Frameworks/JavaVM.framework/Versions/1.7/Home/bin/java");
		}
		// use working directory of actual vm
		javaProcessBuilder.setWorkingDirectory(null);
		// the actual classpath must also work for the new process because both
		// require same test classes and third party classes
		javaProcessBuilder.addClasspathEntry(System
				.getProperty("java.class.path"));
		javaProcessBuilder.setMainClass(mainClass);

		javaProcessBuilder.addArgument(identifier);
		javaProcessBuilder.addArgument(propertiesKey);
		javaProcessBuilder.addArgument(pathToProperties);
		javaProcessBuilder.addArgument(factoryClass);
		javaProcessBuilder.addArgument(peer);
		javaProcessBuilder.addArgument(String.valueOf(payload));

		final Process process = javaProcessBuilder.startProcess();
		// add this class as listener for status changes of remote vm process
		streamStatusConv = new StreamStatusConverter(process.getInputStream());
		streamStatusConv.addPropertyChangeListener(this);
		// start the stream listener
		Executors.newSingleThreadExecutor().execute(streamStatusConv);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof StreamStatusConverter) {
			senderStatus = (PeerStatus) evt.getNewValue();
			System.out.println("Actual remote state: " + senderStatus);
		} else if (evt.getSource() instanceof AbstractPeer) {
			receiverStatus = (PeerStatus) evt.getNewValue();
			System.out.println("Actual receiver state: " + receiverStatus);
		}
	}

}
