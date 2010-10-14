/*
 * Sone - SoneShell.java - Copyright © 2010 David Roden
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
 * {@link Shell} around a {@link Sone} that has not yet been retrieved.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneShell extends Sone implements Shell<Sone> {

	/** A GUID for this Sone. */
	private UUID id;

	/** The name of this Sone. */
	private String name;

	/** The URI under which the Sone is stored in Freenet. */
	private FreenetURI requestUri;

	/** The profile of this Sone. */
	private Profile profile;

	/** All friend Sones. */
	private final Set<Sone> friendSones = new HashSet<Sone>();

	/** All posts. */
	private final List<Post> posts = new ArrayList<Post>();

	/** All replies. */
	private final Set<Reply> replies = new HashSet<Reply>();

	/**
	 * Creates a new Sone shell.
	 */
	public SoneShell() {
		super(null, null, null);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the ID of this Sone.
	 *
	 * @return The ID of this Sone
	 */
	@Override
	public String getId() {
		return id.toString();
	}

	/**
	 * Sets the ID of the Sone.
	 *
	 * @param id
	 *            The ID of the Sone
	 * @return This Sone shell (for method chaining)
	 */
	public SoneShell setId(UUID id) {
		this.id = id;
		return this;
	}

	/**
	 * Returns the name of this Sone.
	 *
	 * @return The name of this Sone
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the Sone.
	 *
	 * @param name
	 *            The name of the Sone
	 * @return This Sone shell (for method chaining)
	 */
	public SoneShell setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Returns the request URI of this Sone.
	 *
	 * @return The request URI of this Sone
	 */
	@Override
	public FreenetURI getRequestUri() {
		return requestUri;
	}

	/**
	 * Sets the request URI of the Sone.
	 *
	 * @param requestUri
	 *            The request URI of the Sone
	 * @return This Sone shell (for method chaining)
	 */
	public SoneShell setRequestUri(FreenetURI requestUri) {
		this.requestUri = requestUri;
		return this;
	}

	/**
	 * Returns a copy of the profile. If you want to update values in the
	 * profile of this Sone, update the values in the returned {@link Profile}
	 * and use {@link #setProfile(Profile)} to change the profile in this Sone.
	 *
	 * @return A copy of the profile
	 */
	@Override
	public Profile getProfile() {
		return new Profile(profile);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	/**
	 * Returns all friend Sones of this Sone.
	 *
	 * @return The friend Sones of this Sone
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
	public Sone removeFriendSone(Sone friendSone) {
		friendSones.remove(friendSone);
		return this;
	}

	/**
	 * Returns the list of posts of this Sone.
	 *
	 * @return All posts of this Sone
	 */
	@Override
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
	@Override
	public void addPost(Post post) {
		posts.add(post);
	}

	/**
	 * Removes the given post from this Sone.
	 *
	 * @param post
	 *            The post to remove
	 */
	@Override
	public void removePost(Post post) {
		posts.remove(post);
	}

	/**
	 * Returns all replies this Sone made.
	 *
	 * @return All replies this Sone made
	 */
	@Override
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
	@Override
	public void addReply(Reply reply) {
		replies.add(reply);
	}

	/**
	 * Removes a reply from this Sone.
	 *
	 * @param reply
	 *            The reply to remove
	 */
	@Override
	public void removeReply(Reply reply) {
		replies.remove(reply);
	}

	//
	// INTERFACE Shell
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canUnshell() {
		return (id != null) && (name != null) && (requestUri != null) && (profile != null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sone getShelled() {
		if (canUnshell()) {
			Sone sone = new Sone(id, name, requestUri);
			sone.setProfile(profile);
			for (Sone friendSone : friendSones) {
				sone.addFriendSone(friendSone);
			}
			for (Post post : posts) {
				sone.addPost(post);
			}
			for (Reply reply : replies) {
				sone.addReply(reply);
			}
			return sone;
		}
		return this;
	}

}
