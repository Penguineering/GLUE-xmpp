package de.ovgu.dke.glue.xmpp.serialization;

import java.util.Map.Entry;
import java.util.Properties;

import org.jivesoftware.smack.packet.Message;


/**
 * <p>
 * Command message to be sent via XMPP.
 * </p>
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 */
public class XMPPTextCommandMessage implements XMPPCommandMessage {
	private final String m_command;
	private final Properties m_properties;

	/**
	 * Create a new command message to be sent via XMPP.
	 * 
	 * @param command
	 *            Command to send.
	 * @param props
	 *            Properties to be sent as parameters. Use the property
	 *            &quot;data&quot; to attach a data object, which must implement
	 *            the <code>toString</code> accordingly.
	 */
	public XMPPTextCommandMessage(final String command, final Properties props) {
		m_command = command;
		m_properties = (Properties) props.clone();
	}

	/**
	 * Create a command message from a received XMPP message.
	 * 
	 * @param msg
	 *            The message to be decoded.
	 * @throws IllegalArgumentException
	 *             if the body is not valid.
	 */
	public XMPPTextCommandMessage(Message msg) {
		final String body = msg.getBody();
		final int nl = body.indexOf('\n');
		if (nl < 0)
			throw new IllegalArgumentException(
					"Error on decoding message body!");
		m_command = body.substring(0, nl);

		final Properties props = decodeBody(body);
		if (props == null)
			throw new IllegalArgumentException(
					"Error on decoding message body!");
		m_properties = props;
	}

	@Override
	public String getCommand() {
		return m_command;
	}

	@Override
	public String getProperty(final String key) {
		return m_properties.getProperty(key);
	}

	@Override
	public String getProperty(final String key, final String defaultValue) {
		return m_properties.getProperty(key, defaultValue);
	}

	@Override
	public String getOwner() {
		return getProperty("owner");
	}

	@Override
	public String getId() {
		return getProperty("id");
	}

	/**
	 * Get the type attached to this message.
	 * 
	 * This is a convenience method.
	 * 
	 * @return the "type" property.
	 */
	public String getType() {
		return getProperty("type");
	}

	/**
	 * Create a command message body to be sent over XMPP.
	 * 
	 * @return The text to be put in the command message body.
	 */
	public String createBody() {
		final StringBuffer body = new StringBuffer(m_command);

		for (Entry<Object, Object> entry : m_properties.entrySet())
			if (!entry.getKey().equals("data"))
				appendBody(body, entry.getKey().toString(), entry.getValue()
						.toString());

		final String data = m_properties.getProperty("data");
		if (data != null) {
			appendBody(body, "-", "DATA SECTION");
			body.append("\n");
			body.append(data);
		}

		return body.toString();
	}

	private final void appendBody(final StringBuffer body, final String name,
			final String value) {
		if (body.length() > 0)
			body.append("\n");

		body.append(name);
		body.append(":");
		body.append(value);
	}

	private Properties decodeBody(final String body) {
		final Properties props = new Properties();

		// skip first line
		int pos = body.indexOf('\n');
		if (pos < 0)
			return null;

		while (pos >= 0 && pos + 1 < body.length()) {
			// do we have '-' in first row? this starts the BeeF data section
			if (body.charAt(pos + 1) == '-') {
				// extract data
				pos = body.indexOf('\n', pos + 1);
				if (pos < 0)
					return null;

				props.put("data", body.substring(pos + 1));

				pos = body.length();

			} else {
				// find dots
				final int dots = body.indexOf(':', pos);
				if (dots < 0)
					return null;

				// find next newline
				final int nl = body.indexOf('\n', dots);

				// extract key and value
				final String key = body.substring(pos + 1, dots);
				final String value = body.substring(dots + 1, nl < 0 ? body
						.length() : nl);
				props.put(key, value);

				// go to next line
				pos = nl;
			}
		}

		return props;
	}
}
