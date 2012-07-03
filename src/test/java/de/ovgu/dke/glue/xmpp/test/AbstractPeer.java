package de.ovgu.dke.glue.xmpp.test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPeer {

	public enum PeerStatus {
		INIT, PREPARING, CONNECTING, LISTENING, FINISHED, ERROR, SENDING
	}

	private PeerStatus status = PeerStatus.INIT;

	protected String identifier;
	protected String factoryClass;
	protected String propertiesKey;
	protected String pathToProperties;

	List<PropertyChangeListener> listeners;

	public AbstractPeer(String identifier, String propertiesKey,
			String pathToProperties, String factoryClass) {
		this.identifier = identifier;
		this.factoryClass = factoryClass;
		this.propertiesKey = propertiesKey;
		this.pathToProperties = pathToProperties;
		this.listeners = new ArrayList<PropertyChangeListener>();
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	public synchronized PeerStatus getStatus() {
		return status;
	}

	public synchronized void setStatus(PeerStatus status) {
		PeerStatus oldStatus = this.status;
		this.status = status;
		// notify only when status really changed
		if (!status.equals(oldStatus)) {
			for (PropertyChangeListener listener : listeners) {
				listener.propertyChange(new PropertyChangeEvent(this, "status",
						oldStatus, this.status));
			}
		}
	}
}
