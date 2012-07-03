package de.ovgu.dke.glue.xmpp.test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.ovgu.dke.glue.xmpp.test.AbstractPeer.PeerStatus;

public class StreamStatusConverter implements Runnable {

	private static BufferedReader br = null;
	private boolean listening;
	private PeerStatus oldStatus = PeerStatus.INIT;
	private PeerStatus status = PeerStatus.INIT;

	List<PropertyChangeListener> listeners;

	public StreamStatusConverter(InputStream is) {
		br = new BufferedReader(new InputStreamReader(is));
		listening = true;
		listeners = new ArrayList<PropertyChangeListener>();
	}

	public void run() {
		while (listening) {
			String line;
			try {
				if ((line = br.readLine()) != null) {
					// System.out.println("SENDER: " + line);
					String[] lines = line.split("\n");
					for (String l : lines) {
						if (l.startsWith("STATUS_")) {
							setStatus(PeerStatus.valueOf(l.substring(l
									.lastIndexOf("_") + 1)));
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		listening = false;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	public void setStatus(PeerStatus newStatus) {
		oldStatus = status;
		status = newStatus;
		// notify only when status really changed
		if (!status.equals(oldStatus)) {			
			for (PropertyChangeListener listener : listeners) {
				listener.propertyChange(new PropertyChangeEvent(this, "status",
						oldStatus, this.status));
			}
		}
	}

}
