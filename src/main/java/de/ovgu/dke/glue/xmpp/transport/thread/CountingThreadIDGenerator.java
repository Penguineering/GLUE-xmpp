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

import de.ovgu.dke.glue.api.transport.TransportException;

public class CountingThreadIDGenerator implements ThreadIDGenerator {
	final URI local_peer;
	private Integer last_id = 0;

	public CountingThreadIDGenerator(URI localPeer) {
		this.local_peer = localPeer;
	}

	@Override
	public XMPPThreadId generateThreadID() throws TransportException {
		synchronized (last_id) {
			last_id++;
			final String id = local_peer.toASCIIString() + ":"
					+ Integer.toString(last_id);

			return createThreadId(id);
		}
	}

	@Override
	public XMPPThreadId generateMetaThreadID() throws TransportException {
		final String id = local_peer.toASCIIString() + ":meta";

		return createThreadId(id);
	}

	// only if the URI is certainly correct!!!
	private XMPPThreadId createThreadId(String uri) {
		try {
			return XMPPThreadId.fromString(uri);
		} catch (URISyntaxException e) {
			// this should never be possible!
			throw new IllegalStateException(
					"Assertion failed: got URI syntax exception which should not be possible: "
							+ e.getMessage(), e);
		}
	}
}
