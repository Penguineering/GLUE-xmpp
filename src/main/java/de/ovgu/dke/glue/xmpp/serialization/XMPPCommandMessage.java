package de.ovgu.dke.glue.xmpp.serialization;

/**
 * Generic interface for an XMPP command message, which is independent of the message format.
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 */
// TODO use XMPPException
public interface XMPPCommandMessage {
	/**
	 * Get the command.
	 * 
	 * @return the command encoded in the message.
	 */
	public String getCommand();

	/**
	 * Get a specific property.
	 * 
	 * @param key
	 *            The property name.
	 * @return The property value or <code>null</code> if not available.
	 */
	public String getProperty(final String key);

	/**
	 * Get a specific property, if not available, use the provided default
	 * value.
	 * 
	 * @param key
	 *            The property name.
	 * @param defaultValue
	 *            default value to return if property is not available.
	 * @return The property's value or the defaultValue if the property is not
	 *         available.
	 */
	public String getProperty(final String key, final String defaultValue);
	
	/**
	 * Get the Owner attached to this message.
	 * 
	 * @return The owner of the message.
	 */
	public String getOwner();

	/**
	 * Get the ID attached to this message, which is unique to the owner.
	 * 
	 * @return the ID of this message.
	 */
	public String getId();


}
