/*
 * Copyright 2012 Stefan Haun, Thomas Low, Sebastian Stober, Andreas Nürnberger
 * 
 *      Data and Knowledge Engineering Group, 
 * 		Faculty of Computer Science,
 *		Otto-von-Guericke University,
 *		Magdeburg, Germany
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ovgu.dke.glue.xmpp.transport.thread;

import java.net.URI;
import java.net.URISyntaxException;

import net.jcip.annotations.Immutable;

/**
 * Packet Thread ID representation
 * 
 * @author Stefan Haun (stefan.haun@ovgu.de)
 * 
 */
@Immutable
public class XMPPThreadId {
	private final URI id;

	// cache value, derived from id
	private URI local_jid;

	/**
	 * Get the representation for a specific URI.
	 * 
	 * The instances may be pooled and reused!
	 * 
	 * @param id
	 *            thread id
	 * @return an instance representing the id
	 * @throws NullPointerException
	 *             if the id parameter is null
	 */
	public static XMPPThreadId fromURI(final URI id) {
		return new XMPPThreadId(id);
	}

	/**
	 * Get the representation for a URI in @code{String} format
	 * 
	 * @param id
	 *            thread id
	 * @return an instance representing the id
	 * @throws URISyntaxException
	 *             if the id is not a valid URI
	 * @throws NullPointerException
	 *             if the id parameter is null
	 */
	public static XMPPThreadId fromString(final String id)
			throws URISyntaxException {
		final URI uri = new URI(id);
		return new XMPPThreadId(uri);
	}

	/**
	 * Instance control: may not be instanciated by foreign class!
	 * 
	 * @param id
	 *            the represented thread ID
	 * @throws NullPointerException
	 *             if the id parameter is null
	 * @throws IllegalArgumentException
	 *             if the URL format does not match xmpp:[jid]:[thread_id]
	 */
	private XMPPThreadId(final URI id) {
		if (id == null)
			throw new NullPointerException("Id may not be null!");

		// TODO move to thread ID generator?
		if (id.toString().indexOf(':') < 0)
			throw new IllegalArgumentException(
					"Thread ID does not match convention!");

		this.id = id;
		this.local_jid = null;
	}

	public URI getId() {
		return id;
	}

	/**
	 * 
	 * @param jid
	 * @return
	 * @throws NullPointerException
	 *             if the jid parameter is null
	 */
	// TODO move to thread ID generator?
	public boolean isSameClient(final URI jid) {
		if (jid == null)
			throw new NullPointerException("jid parameter may not be null!");

		// check cache value
		if (local_jid == null) {
			final String _id = id.toString();
			final int _idx = _id.lastIndexOf(':');
			local_jid = URI.create(_id.substring(0, _idx));
		}

		return jid.equals(local_jid);
	}

	@Override
	public int hashCode() {
		// just return the id's hash code
		// id cannot be null
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// id cannot be null -> this gets a bit simpler
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XMPPThreadId other = (XMPPThreadId) obj;
		return id.equals(other.id);
	}

	@Override
	public String toString() {
		// just return the id's string representation
		// id cannot be null
		return id.toString();
	}
}
