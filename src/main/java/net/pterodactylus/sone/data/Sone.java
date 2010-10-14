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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import freenet.keys.FreenetURI;

/**
 * A Sone defines everything about a user: the {@link User} itself, her profile,
 * her status updates.
 * <p>
 * Operations that modify the Sone need to synchronize on the Sone in question.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Sone {

	/** A GUID for this Sone. */
	private final UUID id;

	/** The name of this Sone. */
	private final String name;

	/** The URI under which the Sone is stored in Freenet. */
	private final FreenetURI requestUri;

	/** The URI used to insert a new version of this Sone. */
	/* This will be null for remote Sones! */
	private final FreenetURI insertUri;

	/** The profile of this Sone. */
	private Profile profile;

	/** All friend Sones. */
	private final Set<Sone> friendSones = new HashSet<Sone>();

	/** All posts. */
	private final List<Post> posts = new ArrayList<Post>();

	/** All replies. */
	private final Set<Reply> replies = new HashSet<Reply>();

	/** Modification count. */
	private volatile long modificationCounter = 0;

	/**
	 * Creates a new Sone.
	 *
	 * @param id
	 *            The ID of this Sone
	 * @param name
	 *            The name of the Sone
	 * @param requestUri
	 *            The request URI of the Sone
	 */
	public Sone(UUID id, String name, FreenetURI requestUri) {
		this(id, name, requestUri, null);
	}

	/**
	 * Creates a new Sone.
	 *
	 * @param id
	 *            The ID of this Sone
	 * @param name
	 *            The name of the Sone
	 * @param requestUri
	 *            The request URI of the Sone
	 * @param insertUri
	 *            The insert URI of the Sone
	 */
	public Sone(UUID id, String name, FreenetURI requestUri, FreenetURI insertUri) {
		this.id = id;
		this.name = name;
		this.requestUri = requestUri;
		this.insertUri = insertUri;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the ID of this Sone.
	 *
	 * @return The ID of this Sone
	 */
	public String getId() {
		return id.toString();
	}

	/**
	 * Returns the name of this Sone.
	 *
	 * @return The name of this Sone
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the request URI of this Sone.
	 *
	 * @return The request URI of this Sone
	 */
	public FreenetURI getRequestUri() {
		return requestUri;
	}

	/**
	 * Returns the insert URI of this Sone.
	 *
	 * @return The insert URI of this Sone
	 */
	public FreenetURI getInsertUri() {
		return insertUri;
	}

	/**
	 * Returns a copy of the profile. If you want to update values in the
	 * profile of this Sone, update the values in the returned {@link Profile}
	 * and use {@link #setProfile(Profile)} to change the profile in this Sone.
	 *
	 * @return A copy of the profile
	 */
	public Profile getProfile() {
		return new Profile(profile);
	}

	/**
	 * Sets the profile of this Sone. A copy of the given profile is stored so
	 * that subsequent modifications of the given profile are not reflected in
	 * this Sone!
	 *
	 * @param profile
	 *            The profile to set
	 */
	public synchronized void setProfile(Profile profile) {
		this.profile = new Profile(profile);
		modificationCounter++;
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
	public synchronized Sone addFriendSone(Sone friendSone) {
		if (friendSones.add(friendSone)) {
			modificationCounter++;
		}
		return this;
	}

	/**
	 * Removes the given Sone as a friend Sone.
	 *
	 * @param friendSone
	 *            The friend Sone to remove
	 * @return This Sone (for method chaining)
	 */
	public synchronized Sone removeFriendSone(Sone friendSone) {
		if (friendSones.remove(friendSone)) {
			modificationCounter++;
		}
		return this;
	}

	/**
	 * Returns the list of posts of this Sone.
	 *
	 * @return All posts of this Sone
	 */
	public List<Post> getPosts() {
		return Collections.unmodifiableList(posts);
	}

	/**
	 * Adds the given post to this Sone. The post will not be added if its
	 * {@link Post#getSone() Sone} is not this Sone.
	 *
	 * @param post
	 *            The post to add
	 */
	public synchronized void addPost(Post post) {
		if (post.getSone().equals(this) && posts.add(post)) {
			modificationCounter++;
		}
	}

	/**
	 * Removes the given post from this Sone.
	 *
	 * @param post
	 *            The post to remove
	 */
	public synchronized void removePost(Post post) {
		if (post.getSone().equals(this) && posts.remove(post)) {
			modificationCounter++;
		}
	}

	/**
	 * Returns all replies this Sone made.
	 *
	 * @return All replies this Sone made
	 */
	public Set<Reply> getReplies() {
		return Collections.unmodifiableSet(replies);
	}

	/**
	 * Adds a reply to this Sone. If the given reply was not made by this Sone,
	 * nothing is added to this Sone.
	 *
	 * @param reply
	 *            The reply to add
	 */
	public synchronized void addReply(Reply reply) {
		if (reply.getSone().equals(this) && replies.add(reply)) {
			modificationCounter++;
		}
	}

	/**
	 * Removes a reply from this Sone.
	 *
	 * @param reply
	 *            The reply to remove
	 */
	public synchronized void removeReply(Reply reply) {
		if (reply.getSone().equals(this) && replies.remove(reply)) {
			modificationCounter++;
		}
	}

	/**
	 * Returns the modification counter.
	 *
	 * @return The modification counter
	 */
	public synchronized long getModificationCounter() {
		return modificationCounter;
	}

	/**
	 * Sets the modification counter.
	 *
	 * @param modificationCounter
	 *            The new modification counter
	 */
	public synchronized void setModificationCounter(long modificationCounter) {
		this.modificationCounter = modificationCounter;
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Sone)) {
			return false;
		}
		return ((Sone) object).id.equals(id);
	}

}
