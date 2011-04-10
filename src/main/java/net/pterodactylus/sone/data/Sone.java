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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.validation.Validation;
import freenet.keys.FreenetURI;

/**
 * A Sone defines everything about a user: her profile, her status updates, her
 * replies, her likes and dislikes, etc.
 * <p>
 * Operations that modify the Sone need to synchronize on the Sone in question.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Sone implements Fingerprintable, Comparable<Sone> {

	/** comparator that sorts Sones by their nice name. */
	public static final Comparator<Sone> NICE_NAME_COMPARATOR = new Comparator<Sone>() {

		@Override
		public int compare(Sone leftSone, Sone rightSone) {
			int diff = SoneAccessor.getNiceName(leftSone).compareToIgnoreCase(SoneAccessor.getNiceName(rightSone));
			if (diff != 0) {
				return diff;
			}
			return leftSone.getId().compareToIgnoreCase(rightSone.getId());
		}

	};

	/** The logger. */
	private static final Logger logger = Logging.getLogger(Sone.class);

	/** The ID of this Sone. */
	private final String id;

	/** The identity of this Sone. */
	private Identity identity;

	/** The URI under which the Sone is stored in Freenet. */
	private volatile FreenetURI requestUri;

	/** The URI used to insert a new version of this Sone. */
	/* This will be null for remote Sones! */
	private volatile FreenetURI insertUri;

	/** The latest edition of the Sone. */
	private volatile long latestEdition;

	/** The time of the last inserted update. */
	private volatile long time;

	/** The profile of this Sone. */
	private volatile Profile profile = new Profile();

	/** The client used by the Sone. */
	private volatile Client client;

	/** All friend Sones. */
	private final Set<String> friendSones = Collections.synchronizedSet(new HashSet<String>());

	/** All posts. */
	private final Set<Post> posts = Collections.synchronizedSet(new HashSet<Post>());

	/** All replies. */
	private final Set<Reply> replies = Collections.synchronizedSet(new HashSet<Reply>());

	/** The IDs of all liked posts. */
	private final Set<String> likedPostIds = Collections.synchronizedSet(new HashSet<String>());

	/** The IDs of all liked replies. */
	private final Set<String> likedReplyIds = Collections.synchronizedSet(new HashSet<String>());

	/** The albums of this Sone. */
	private final List<Album> albums = Collections.synchronizedList(new ArrayList<Album>());

	/**
	 * Creates a new Sone.
	 *
	 * @param id
	 *            The ID of the Sone
	 */
	public Sone(String id) {
		this.id = id;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the identity of this Sone.
	 *
	 * @return The identity of this Sone
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the identity of this Sone.
	 *
	 * @return The identity of this Sone
	 */
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * Sets the identity of this Sone. The {@link Identity#getId() ID} of the
	 * identity has to match this Sone’s {@link #getId()}.
	 *
	 * @param identity
	 *            The identity of this Sone
	 * @return This Sone (for method chaining)
	 * @throws IllegalArgumentException
	 *             if the ID of the identity does not match this Sone’s ID
	 */
	public Sone setIdentity(Identity identity) throws IllegalArgumentException {
		if (!identity.getId().equals(id)) {
			throw new IllegalArgumentException("Identity’s ID does not match Sone’s ID!");
		}
		this.identity = identity;
		return this;
	}

	/**
	 * Returns the name of this Sone.
	 *
	 * @return The name of this Sone
	 */
	public String getName() {
		return (identity != null) ? identity.getNickname() : null;
	}

	/**
	 * Returns the request URI of this Sone.
	 *
	 * @return The request URI of this Sone
	 */
	public FreenetURI getRequestUri() {
		return (requestUri != null) ? requestUri.setSuggestedEdition(latestEdition) : null;
	}

	/**
	 * Sets the request URI of this Sone.
	 *
	 * @param requestUri
	 *            The request URI of this Sone
	 * @return This Sone (for method chaining)
	 */
	public Sone setRequestUri(FreenetURI requestUri) {
		if (this.requestUri == null) {
			this.requestUri = requestUri.setKeyType("USK").setDocName("Sone").setMetaString(new String[0]);
			return this;
		}
		if (!this.requestUri.equalsKeypair(requestUri)) {
			logger.log(Level.WARNING, "Request URI %s tried to overwrite %s!", new Object[] { requestUri, this.requestUri });
			return this;
		}
		return this;
	}

	/**
	 * Returns the insert URI of this Sone.
	 *
	 * @return The insert URI of this Sone
	 */
	public FreenetURI getInsertUri() {
		return (insertUri != null) ? insertUri.setSuggestedEdition(latestEdition) : null;
	}

	/**
	 * Sets the insert URI of this Sone.
	 *
	 * @param insertUri
	 *            The insert URI of this Sone
	 * @return This Sone (for method chaining)
	 */
	public Sone setInsertUri(FreenetURI insertUri) {
		if (this.insertUri == null) {
			this.insertUri = insertUri.setKeyType("USK").setDocName("Sone").setMetaString(new String[0]);
			return this;
		}
		if (!this.insertUri.equalsKeypair(insertUri)) {
			logger.log(Level.WARNING, "Request URI %s tried to overwrite %s!", new Object[] { insertUri, this.insertUri });
			return this;
		}
		return this;
	}

	/**
	 * Returns the latest edition of this Sone.
	 *
	 * @return The latest edition of this Sone
	 */
	public long getLatestEdition() {
		return latestEdition;
	}

	/**
	 * Sets the latest edition of this Sone. If the given latest edition is not
	 * greater than the current latest edition, the latest edition of this Sone
	 * is not changed.
	 *
	 * @param latestEdition
	 *            The latest edition of this Sone
	 */
	public void setLatestEdition(long latestEdition) {
		if (!(latestEdition > this.latestEdition)) {
			logger.log(Level.FINE, "New latest edition %d is not greater than current latest edition %d!", new Object[] { latestEdition, this.latestEdition });
			return;
		}
		this.latestEdition = latestEdition;
	}

	/**
	 * Return the time of the last inserted update of this Sone.
	 *
	 * @return The time of the update (in milliseconds since Jan 1, 1970 UTC)
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets the time of the last inserted update of this Sone.
	 *
	 * @param time
	 *            The time of the update (in milliseconds since Jan 1, 1970 UTC)
	 * @return This Sone (for method chaining)
	 */
	public Sone setTime(long time) {
		this.time = time;
		return this;
	}

	/**
	 * Returns a copy of the profile. If you want to update values in the
	 * profile of this Sone, update the values in the returned {@link Profile}
	 * and use {@link #setProfile(Profile)} to change the profile in this Sone.
	 *
	 * @return A copy of the profile
	 */
	public synchronized Profile getProfile() {
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
	}

	/**
	 * Returns the client used by this Sone.
	 *
	 * @return The client used by this Sone, or {@code null}
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * Sets the client used by this Sone.
	 *
	 * @param client
	 *            The client used by this Sone, or {@code null}
	 * @return This Sone (for method chaining)
	 */
	public Sone setClient(Client client) {
		this.client = client;
		return this;
	}

	/**
	 * Returns all friend Sones of this Sone.
	 *
	 * @return The friend Sones of this Sone
	 */
	public List<String> getFriends() {
		List<String> friends = new ArrayList<String>(friendSones);
		return friends;
	}

	/**
	 * Sets all friends of this Sone at once.
	 *
	 * @param friends
	 *            The new (and only) friends of this Sone
	 * @return This Sone (for method chaining)
	 */
	public Sone setFriends(Collection<String> friends) {
		friendSones.clear();
		friendSones.addAll(friends);
		return this;
	}

	/**
	 * Returns whether this Sone has the given Sone as a friend Sone.
	 *
	 * @param friendSoneId
	 *            The ID of the Sone to check for
	 * @return {@code true} if this Sone has the given Sone as a friend,
	 *         {@code false} otherwise
	 */
	public boolean hasFriend(String friendSoneId) {
		return friendSones.contains(friendSoneId);
	}

	/**
	 * Adds the given Sone as a friend Sone.
	 *
	 * @param friendSone
	 *            The friend Sone to add
	 * @return This Sone (for method chaining)
	 */
	public Sone addFriend(String friendSone) {
		if (!friendSone.equals(id)) {
			friendSones.add(friendSone);
		}
		return this;
	}

	/**
	 * Removes the given Sone as a friend Sone.
	 *
	 * @param friendSoneId
	 *            The ID of the friend Sone to remove
	 * @return This Sone (for method chaining)
	 */
	public Sone removeFriend(String friendSoneId) {
		friendSones.remove(friendSoneId);
		return this;
	}

	/**
	 * Returns the list of posts of this Sone, sorted by time, newest first.
	 *
	 * @return All posts of this Sone
	 */
	public List<Post> getPosts() {
		List<Post> sortedPosts;
		synchronized (this) {
			sortedPosts = new ArrayList<Post>(posts);
		}
		Collections.sort(sortedPosts, Post.TIME_COMPARATOR);
		return sortedPosts;
	}

	/**
	 * Sets all posts of this Sone at once.
	 *
	 * @param posts
	 *            The new (and only) posts of this Sone
	 * @return This Sone (for method chaining)
	 */
	public synchronized Sone setPosts(Collection<Post> posts) {
		this.posts.clear();
		this.posts.addAll(posts);
		return this;
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
			logger.log(Level.FINEST, "Adding %s to “%s”.", new Object[] { post, getName() });
		}
	}

	/**
	 * Removes the given post from this Sone.
	 *
	 * @param post
	 *            The post to remove
	 */
	public synchronized void removePost(Post post) {
		if (post.getSone().equals(this)) {
			posts.remove(post);
		}
	}

	/**
	 * Returns all replies this Sone made.
	 *
	 * @return All replies this Sone made
	 */
	public synchronized Set<Reply> getReplies() {
		return Collections.unmodifiableSet(replies);
	}

	/**
	 * Sets all replies of this Sone at once.
	 *
	 * @param replies
	 *            The new (and only) replies of this Sone
	 * @return This Sone (for method chaining)
	 */
	public synchronized Sone setReplies(Collection<Reply> replies) {
		this.replies.clear();
		this.replies.addAll(replies);
		return this;
	}

	/**
	 * Adds a reply to this Sone. If the given reply was not made by this Sone,
	 * nothing is added to this Sone.
	 *
	 * @param reply
	 *            The reply to add
	 */
	public synchronized void addReply(Reply reply) {
		if (reply.getSone().equals(this)) {
			replies.add(reply);
		}
	}

	/**
	 * Removes a reply from this Sone.
	 *
	 * @param reply
	 *            The reply to remove
	 */
	public synchronized void removeReply(Reply reply) {
		if (reply.getSone().equals(this)) {
			replies.remove(reply);
		}
	}

	/**
	 * Returns the IDs of all liked posts.
	 *
	 * @return All liked posts’ IDs
	 */
	public Set<String> getLikedPostIds() {
		return Collections.unmodifiableSet(likedPostIds);
	}

	/**
	 * Sets the IDs of all liked posts.
	 *
	 * @param likedPostIds
	 *            All liked posts’ IDs
	 * @return This Sone (for method chaining)
	 */
	public synchronized Sone setLikePostIds(Set<String> likedPostIds) {
		this.likedPostIds.clear();
		this.likedPostIds.addAll(likedPostIds);
		return this;
	}

	/**
	 * Checks whether the given post ID is liked by this Sone.
	 *
	 * @param postId
	 *            The ID of the post
	 * @return {@code true} if this Sone likes the given post, {@code false}
	 *         otherwise
	 */
	public boolean isLikedPostId(String postId) {
		return likedPostIds.contains(postId);
	}

	/**
	 * Adds the given post ID to the list of posts this Sone likes.
	 *
	 * @param postId
	 *            The ID of the post
	 * @return This Sone (for method chaining)
	 */
	public synchronized Sone addLikedPostId(String postId) {
		likedPostIds.add(postId);
		return this;
	}

	/**
	 * Removes the given post ID from the list of posts this Sone likes.
	 *
	 * @param postId
	 *            The ID of the post
	 * @return This Sone (for method chaining)
	 */
	public synchronized Sone removeLikedPostId(String postId) {
		likedPostIds.remove(postId);
		return this;
	}

	/**
	 * Returns the IDs of all liked replies.
	 *
	 * @return All liked replies’ IDs
	 */
	public Set<String> getLikedReplyIds() {
		return Collections.unmodifiableSet(likedReplyIds);
	}

	/**
	 * Sets the IDs of all liked replies.
	 *
	 * @param likedReplyIds
	 *            All liked replies’ IDs
	 * @return This Sone (for method chaining)
	 */
	public synchronized Sone setLikeReplyIds(Set<String> likedReplyIds) {
		this.likedReplyIds.clear();
		this.likedReplyIds.addAll(likedReplyIds);
		return this;
	}

	/**
	 * Checks whether the given reply ID is liked by this Sone.
	 *
	 * @param replyId
	 *            The ID of the reply
	 * @return {@code true} if this Sone likes the given reply, {@code false}
	 *         otherwise
	 */
	public boolean isLikedReplyId(String replyId) {
		return likedReplyIds.contains(replyId);
	}

	/**
	 * Adds the given reply ID to the list of replies this Sone likes.
	 *
	 * @param replyId
	 *            The ID of the reply
	 * @return This Sone (for method chaining)
	 */
	public synchronized Sone addLikedReplyId(String replyId) {
		likedReplyIds.add(replyId);
		return this;
	}

	/**
	 * Removes the given post ID from the list of replies this Sone likes.
	 *
	 * @param replyId
	 *            The ID of the reply
	 * @return This Sone (for method chaining)
	 */
	public synchronized Sone removeLikedReplyId(String replyId) {
		likedReplyIds.remove(replyId);
		return this;
	}

	/**
	 * Returns the albums of this Sone.
	 *
	 * @return The albums of this Sone
	 */
	public List<Album> getAlbums() {
		return Collections.unmodifiableList(albums);
	}

	/**
	 * Adds an album to this Sone.
	 *
	 * @param album
	 *            The album to add
	 */
	public synchronized void addAlbum(Album album) {
		Validation.begin().isNotNull("Album", album).check().isEqual("Album Owner", album.getSone(), this).check();
		albums.add(album);
	}

	/**
	 * Sets the albums of this Sone.
	 *
	 * @param albums
	 *            The albums of this Sone
	 */
	public synchronized void setAlbums(Collection<? extends Album> albums) {
		Validation.begin().isNotNull("Albums", albums).check();
		this.albums.clear();
		for (Album album : albums) {
			addAlbum(album);
		}
	}

	/**
	 * Removes an album from this Sone.
	 *
	 * @param album
	 *            The album to remove
	 */
	public synchronized void removeAlbum(Album album) {
		Validation.begin().isNotNull("Album", album).check().isEqual("Album Owner", album.getSone(), this).check();
		albums.remove(album);
	}

	//
	// FINGERPRINTABLE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized String getFingerprint() {
		StringBuilder fingerprint = new StringBuilder();
		fingerprint.append(profile.getFingerprint());

		fingerprint.append("Posts(");
		for (Post post : getPosts()) {
			fingerprint.append("Post(").append(post.getId()).append(')');
		}
		fingerprint.append(")");

		List<Reply> replies = new ArrayList<Reply>(getReplies());
		Collections.sort(replies, Reply.TIME_COMPARATOR);
		fingerprint.append("Replies(");
		for (Reply reply : replies) {
			fingerprint.append("Reply(").append(reply.getId()).append(')');
		}
		fingerprint.append(')');

		List<String> likedPostIds = new ArrayList<String>(getLikedPostIds());
		Collections.sort(likedPostIds);
		fingerprint.append("LikedPosts(");
		for (String likedPostId : likedPostIds) {
			fingerprint.append("Post(").append(likedPostId).append(')');
		}
		fingerprint.append(')');

		List<String> likedReplyIds = new ArrayList<String>(getLikedReplyIds());
		Collections.sort(likedReplyIds);
		fingerprint.append("LikedReplies(");
		for (String likedReplyId : likedReplyIds) {
			fingerprint.append("Reply(").append(likedReplyId).append(')');
		}
		fingerprint.append(')');

//		fingerprint.append("Albums(");
//		for (Album album : albums) {
//			fingerprint.append(album.getFingerprint());
//		}
//		fingerprint.append(')');

		return fingerprint.toString();
	}

	//
	// INTERFACE Comparable<Sone>
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Sone sone) {
		return NICE_NAME_COMPARATOR.compare(this, sone);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[identity=" + identity + ",requestUri=" + requestUri + ",insertUri(" + String.valueOf(insertUri).length() + "),friends(" + friendSones.size() + "),posts(" + posts.size() + "),replies(" + replies.size() + ")]";
	}

}
