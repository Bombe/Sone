/*
 * FreenetSone - Sone.java - Copyright © 2010 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import freenet.keys.FreenetURI;

/**
 * A Sone defines everything about a user: the {@link User} itself, her profile,
 * her status updates.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Sone {

	/** The URI under which the Sone is stored in Freenet. */
	private final FreenetURI requestUri;

	/** The URI used to insert a new version of this Sone. */
	/* This will be null for remote Sones! */
	private final FreenetURI insertUri;

	/** All friend Sones. */
	private final Set<Sone> friendSones = new HashSet<Sone>();

	/**
	 * Creates a new Sone.
	 *
	 * @param requestUri
	 *            The request URI of the Sone
	 */
	public Sone(FreenetURI requestUri) {
		this(requestUri, null);
	}

	/**
	 * Creates a new Sone.
	 *
	 * @param requestUri
	 *            The request URI of the Sone
	 * @param insertUri
	 *            The insert URI of the Sone
	 */
	public Sone(FreenetURI requestUri, FreenetURI insertUri) {
		this.requestUri = requestUri;
		this.insertUri = insertUri;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the request URI of this Sone.
	 *
	 * @return The request URI of this Sone
	 */
	public FreenetURI requestUri() {
		return requestUri;
	}

	/**
	 * Returns the insert URI of this Sone.
	 *
	 * @return The insert URI of this Sone
	 */
	public FreenetURI insertUri() {
		return insertUri;
	}

	/**
	 * Returns all friend Sones of this Sone.
	 *
	 * @return The friend Sones of this Sone
	 */
	public Set<Sone> getFriendSones() {
		return Collections.unmodifiableSet(friendSones);
	}

	/**
	 * Returns whether this Sone has the given Sone as a friend Sone.
	 *
	 * @param friendSone
	 *            The friend Sone to check for
	 * @return {@code true} if this Sone has the given Sone as a friend,
	 *         {@code false} otherwise
	 */
	public boolean hasFriendSone(Sone friendSone) {
		return friendSones.contains(friendSone);
	}

	/**
	 * Adds the given Sone as a friend Sone.
	 *
	 * @param friendSone
	 *            The friend Sone to add
	 * @return This Sone (for method chaining)
	 */
	public Sone addFriendSone(Sone friendSone) {
		friendSones.add(friendSone);
		return this;
	}

	/**
	 * Removes the given Sone as a friend Sone.
	 *
	 * @param friendSone
	 *            The friend Sone to remove
	 * @return This Sone (for method chaining)
	 */
	public Sone removeFriendSone(Sone friendSone) {
		friendSones.remove(friendSone);
		return this;
	}

}
