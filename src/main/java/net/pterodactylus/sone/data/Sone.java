/*
 * Sone - Sone.java - Copyright © 2010–2013 David Roden
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

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static net.pterodactylus.sone.data.Album.FLATTENER;
import static net.pterodactylus.sone.data.Album.IMAGES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Options;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.util.logging.Logging;

import freenet.keys.FreenetURI;

import com.google.common.base.Predicate;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Ints;

/**
 * A Sone defines everything about a user: her profile, her status updates, her
 * replies, her likes and dislikes, etc.
 * <p/>
 * Operations that modify the Sone need to synchronize on the Sone in question.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Sone implements Identified, Fingerprintable, Comparable<Sone> {

	/**
	 * Enumeration for the possible states of a {@link Sone}.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public enum SoneStatus {

		/** The Sone is unknown, i.e. not yet downloaded. */
		unknown,

		/** The Sone is idle, i.e. not being downloaded or inserted. */
		idle,

		/** The Sone is currently being inserted. */
		inserting,

		/** The Sone is currently being downloaded. */
		downloading,
	}

	/**
	 * The possible values for the “show custom avatars” option.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static enum ShowCustomAvatars {

		/** Never show custom avatars. */
		NEVER,

		/** Only show custom avatars of followed Sones. */
		FOLLOWED,

		/** Only show custom avatars of Sones you manually trust. */
		MANUALLY_TRUSTED,

		/** Only show custom avatars of automatically trusted Sones. */
		TRUSTED,

		/** Always show custom avatars. */
		ALWAYS,

	}

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

	/** Comparator that sorts Sones by last activity (least recent active first). */
	public static final Comparator<Sone> LAST_ACTIVITY_COMPARATOR = new Comparator<Sone>() {

		@Override
		public int compare(Sone firstSone, Sone secondSone) {
			return (int) Math.min(Integer.MAX_VALUE, Math.max(Integer.MIN_VALUE, secondSone.getTime() - firstSone.getTime()));
		}
	};

	/** Comparator that sorts Sones by numbers of posts (descending). */
	public static final Comparator<Sone> POST_COUNT_COMPARATOR = new Comparator<Sone>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(Sone leftSone, Sone rightSone) {
			return (leftSone.getPosts().size() != rightSone.getPosts().size()) ? (rightSone.getPosts().size() - leftSone.getPosts().size()) : (rightSone.getReplies().size() - leftSone.getReplies().size());
		}
	};

	/** Comparator that sorts Sones by number of images (descending). */
	public static final Comparator<Sone> IMAGE_COUNT_COMPARATOR = new Comparator<Sone>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(Sone leftSone, Sone rightSone) {
			int rightSoneImageCount = from(asList(rightSone.getRootAlbum())).transformAndConcat(FLATTENER).transformAndConcat(IMAGES).size();
			int leftSoneImageCount = from(asList(leftSone.getRootAlbum())).transformAndConcat(FLATTENER).transformAndConcat(IMAGES).size();
			/* sort descending. */
			return Ints.compare(rightSoneImageCount, leftSoneImageCount);
		}
	};

	/** Filter to remove Sones that have not been downloaded. */
	public static final Predicate<Sone> EMPTY_SONE_FILTER = new Predicate<Sone>() {

		@Override
		public boolean apply(Sone sone) {
			return (sone == null) ? false : sone.getTime() != 0;
		}
	};

	/** Filter that matches all {@link Sone#isLocal() local Sones}. */
	public static final Predicate<Sone> LOCAL_SONE_FILTER = new Predicate<Sone>() {

		@Override
		public boolean apply(Sone sone) {
			return (sone == null) ? false : sone.getIdentity() instanceof OwnIdentity;
		}

	};

	/** Filter that matches Sones that have at least one album. */
	public static final Predicate<Sone> HAS_ALBUM_FILTER = new Predicate<Sone>() {

		@Override
		public boolean apply(Sone sone) {
			return (sone == null) ? false : !sone.getRootAlbum().getAlbums().isEmpty();
		}
	};

	/** The logger. */
	private static final Logger logger = Logging.getLogger(Sone.class);

	/** The ID of this Sone. */
	private final String id;

	/** Whether the Sone is local. */
	private final boolean local;

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

	/** The status of this Sone. */
	private volatile SoneStatus status = SoneStatus.unknown;

	/** The profile of this Sone. */
	private volatile Profile profile = new Profile(this);

	/** The client used by the Sone. */
	private volatile Client client;

	/** Whether this Sone is known. */
	private volatile boolean known;

	/** All friend Sones. */
	private final Set<String> friendSones = new CopyOnWriteArraySet<String>();

	/** All posts. */
	private final Set<Post> posts = new CopyOnWriteArraySet<Post>();

	/** All replies. */
	private final Set<PostReply> replies = new CopyOnWriteArraySet<PostReply>();

	/** The IDs of all liked posts. */
	private final Set<String> likedPostIds = new CopyOnWriteArraySet<String>();

	/** The IDs of all liked replies. */
	private final Set<String> likedReplyIds = new CopyOnWriteArraySet<String>();

	/** The root album containing all albums. */
	private final Album rootAlbum = new Album().setSone(this);

	/** Sone-specific options. */
	private Options options = new Options();

	/**
	 * Creates a new Sone.
	 *
	 * @param id
	 * 		The ID of the Sone
	 * @param local
	 * 		{@code true} if the Sone is a local Sone, {@code false} otherwise
	 */
	public Sone(String id, boolean local) {
		this.id = id;
		this.local = local;
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
	 * 		The identity of this Sone
	 * @return This Sone (for method chaining)
	 * @throws IllegalArgumentException
	 * 		if the ID of the identity does not match this Sone’s ID
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
	 * Returns whether this Sone is a local Sone.
	 *
	 * @return {@code true} if this Sone is a local Sone, {@code false} otherwise
	 */
	public boolean isLocal() {
		return local;
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
	 * 		The request URI of this Sone
	 * @return This Sone (for method chaining)
	 */
	public Sone setRequestUri(FreenetURI requestUri) {
		if (this.requestUri == null) {
			this.requestUri = requestUri.setKeyType("USK").setDocName("Sone").setMetaString(new String[0]);
			return this;
		}
		if (!this.requestUri.equalsKeypair(requestUri)) {
			logger.log(Level.WARNING, String.format("Request URI %s tried to overwrite %s!", requestUri, this.requestUri));
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
	 * 		The insert URI of this Sone
	 * @return This Sone (for method chaining)
	 */
	public Sone setInsertUri(FreenetURI insertUri) {
		if (this.insertUri == null) {
			this.insertUri = insertUri.setKeyType("USK").setDocName("Sone").setMetaString(new String[0]);
			return this;
		}
		if (!this.insertUri.equalsKeypair(insertUri)) {
			logger.log(Level.WARNING, String.format("Request URI %s tried to overwrite %s!", insertUri, this.insertUri));
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
	 * greater than the current latest edition, the latest edition of this Sone is
	 * not changed.
	 *
	 * @param latestEdition
	 * 		The latest edition of this Sone
	 */
	public void setLatestEdition(long latestEdition) {
		if (!(latestEdition > this.latestEdition)) {
			logger.log(Level.FINE, String.format("New latest edition %d is not greater than current latest edition %d!", latestEdition, this.latestEdition));
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
	 * 		The time of the update (in milliseconds since Jan 1, 1970 UTC)
	 * @return This Sone (for method chaining)
	 */
	public Sone setTime(long time) {
		this.time = time;
		return this;
	}

	/**
	 * Returns the status of this Sone.
	 *
	 * @return The status of this Sone
	 */
	public SoneStatus getStatus() {
		return status;
	}

	/**
	 * Sets the new status of this Sone.
	 *
	 * @param status
	 * 		The new status of this Sone
	 * @return This Sone
	 * @throws IllegalArgumentException
	 * 		if {@code status} is {@code null}
	 */
	public Sone setStatus(SoneStatus status) {
		this.status = checkNotNull(status, "status must not be null");
		return this;
	}

	/**
	 * Returns a copy of the profile. If you want to update values in the profile
	 * of this Sone, update the values in the returned {@link Profile} and use
	 * {@link #setProfile(Profile)} to change the profile in this Sone.
	 *
	 * @return A copy of the profile
	 */
	public Profile getProfile() {
		return new Profile(profile);
	}

	/**
	 * Sets the profile of this Sone. A copy of the given profile is stored so that
	 * subsequent modifications of the given profile are not reflected in this
	 * Sone!
	 *
	 * @param profile
	 * 		The profile to set
	 */
	public void setProfile(Profile profile) {
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
	 * 		The client used by this Sone, or {@code null}
	 * @return This Sone (for method chaining)
	 */
	public Sone setClient(Client client) {
		this.client = client;
		return this;
	}

	/**
	 * Returns whether this Sone is known.
	 *
	 * @return {@code true} if this Sone is known, {@code false} otherwise
	 */
	public boolean isKnown() {
		return known;
	}

	/**
	 * Sets whether this Sone is known.
	 *
	 * @param known
	 * 		{@code true} if this Sone is known, {@code false} otherwise
	 * @return This Sone
	 */
	public Sone setKnown(boolean known) {
		this.known = known;
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
	 * Returns whether this Sone has the given Sone as a friend Sone.
	 *
	 * @param friendSoneId
	 * 		The ID of the Sone to check for
	 * @return {@code true} if this Sone has the given Sone as a friend, {@code
	 *         false} otherwise
	 */
	public boolean hasFriend(String friendSoneId) {
		return friendSones.contains(friendSoneId);
	}

	/**
	 * Adds the given Sone as a friend Sone.
	 *
	 * @param friendSone
	 * 		The friend Sone to add
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
	 * 		The ID of the friend Sone to remove
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
	 * 		The new (and only) posts of this Sone
	 * @return This Sone (for method chaining)
	 */
	public Sone setPosts(Collection<Post> posts) {
		synchronized (this) {
			this.posts.clear();
			this.posts.addAll(posts);
		}
		return this;
	}

	/**
	 * Adds the given post to this Sone. The post will not be added if its {@link
	 * Post#getSone() Sone} is not this Sone.
	 *
	 * @param post
	 * 		The post to add
	 */
	public void addPost(Post post) {
		if (post.getSone().equals(this) && posts.add(post)) {
			logger.log(Level.FINEST, String.format("Adding %s to “%s”.", post, getName()));
		}
	}

	/**
	 * Removes the given post from this Sone.
	 *
	 * @param post
	 * 		The post to remove
	 */
	public void removePost(Post post) {
		if (post.getSone().equals(this)) {
			posts.remove(post);
		}
	}

	/**
	 * Returns all replies this Sone made.
	 *
	 * @return All replies this Sone made
	 */
	public Set<PostReply> getReplies() {
		return Collections.unmodifiableSet(replies);
	}

	/**
	 * Sets all replies of this Sone at once.
	 *
	 * @param replies
	 * 		The new (and only) replies of this Sone
	 * @return This Sone (for method chaining)
	 */
	public Sone setReplies(Collection<PostReply> replies) {
		this.replies.clear();
		this.replies.addAll(replies);
		return this;
	}

	/**
	 * Adds a reply to this Sone. If the given reply was not made by this Sone,
	 * nothing is added to this Sone.
	 *
	 * @param reply
	 * 		The reply to add
	 */
	public void addReply(PostReply reply) {
		if (reply.getSone().equals(this)) {
			replies.add(reply);
		}
	}

	/**
	 * Removes a reply from this Sone.
	 *
	 * @param reply
	 * 		The reply to remove
	 */
	public void removeReply(PostReply reply) {
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
	 * 		All liked posts’ IDs
	 * @return This Sone (for method chaining)
	 */
	public Sone setLikePostIds(Set<String> likedPostIds) {
		this.likedPostIds.clear();
		this.likedPostIds.addAll(likedPostIds);
		return this;
	}

	/**
	 * Checks whether the given post ID is liked by this Sone.
	 *
	 * @param postId
	 * 		The ID of the post
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
	 * 		The ID of the post
	 * @return This Sone (for method chaining)
	 */
	public Sone addLikedPostId(String postId) {
		likedPostIds.add(postId);
		return this;
	}

	/**
	 * Removes the given post ID from the list of posts this Sone likes.
	 *
	 * @param postId
	 * 		The ID of the post
	 * @return This Sone (for method chaining)
	 */
	public Sone removeLikedPostId(String postId) {
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
	 * 		All liked replies’ IDs
	 * @return This Sone (for method chaining)
	 */
	public Sone setLikeReplyIds(Set<String> likedReplyIds) {
		this.likedReplyIds.clear();
		this.likedReplyIds.addAll(likedReplyIds);
		return this;
	}

	/**
	 * Checks whether the given reply ID is liked by this Sone.
	 *
	 * @param replyId
	 * 		The ID of the reply
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
	 * 		The ID of the reply
	 * @return This Sone (for method chaining)
	 */
	public Sone addLikedReplyId(String replyId) {
		likedReplyIds.add(replyId);
		return this;
	}

	/**
	 * Removes the given post ID from the list of replies this Sone likes.
	 *
	 * @param replyId
	 * 		The ID of the reply
	 * @return This Sone (for method chaining)
	 */
	public Sone removeLikedReplyId(String replyId) {
		likedReplyIds.remove(replyId);
		return this;
	}

	/**
	 * Returns the root album that contains all visible albums of this Sone.
	 *
	 * @return The root album of this Sone
	 */
	public Album getRootAlbum() {
		return rootAlbum;
	}

	/**
	 * Returns Sone-specific options.
	 *
	 * @return The options of this Sone
	 */
	public Options getOptions() {
		return options;
	}

	/**
	 * Sets the options of this Sone.
	 *
	 * @param options
	 * 		The options of this Sone
	 */
	/* TODO - remove this method again, maybe add an option provider */
	public void setOptions(Options options) {
		this.options = options;
	}

	//
	// FINGERPRINTABLE METHODS
	//

	/** {@inheritDoc} */
	@Override
	public synchronized String getFingerprint() {
		Hasher hash = Hashing.sha256().newHasher();
		hash.putString(profile.getFingerprint());

		hash.putString("Posts(");
		for (Post post : getPosts()) {
			hash.putString("Post(").putString(post.getId()).putString(")");
		}
		hash.putString(")");

		List<PostReply> replies = new ArrayList<PostReply>(getReplies());
		Collections.sort(replies, Reply.TIME_COMPARATOR);
		hash.putString("Replies(");
		for (PostReply reply : replies) {
			hash.putString("Reply(").putString(reply.getId()).putString(")");
		}
		hash.putString(")");

		List<String> likedPostIds = new ArrayList<String>(getLikedPostIds());
		Collections.sort(likedPostIds);
		hash.putString("LikedPosts(");
		for (String likedPostId : likedPostIds) {
			hash.putString("Post(").putString(likedPostId).putString(")");
		}
		hash.putString(")");

		List<String> likedReplyIds = new ArrayList<String>(getLikedReplyIds());
		Collections.sort(likedReplyIds);
		hash.putString("LikedReplies(");
		for (String likedReplyId : likedReplyIds) {
			hash.putString("Reply(").putString(likedReplyId).putString(")");
		}
		hash.putString(")");

		hash.putString("Albums(");
		for (Album album : rootAlbum.getAlbums()) {
			if (!Album.NOT_EMPTY.apply(album)) {
				continue;
			}
			hash.putString(album.getFingerprint());
		}
		hash.putString(")");

		return hash.hash().toString();
	}

	//
	// INTERFACE Comparable<Sone>
	//

	/** {@inheritDoc} */
	@Override
	public int compareTo(Sone sone) {
		return NICE_NAME_COMPARATOR.compare(this, sone);
	}

	//
	// OBJECT METHODS
	//

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Sone)) {
			return false;
		}
		return ((Sone) object).id.equals(id);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getClass().getName() + "[identity=" + identity + ",requestUri=" + requestUri + ",insertUri(" + String.valueOf(insertUri).length() + "),friends(" + friendSones.size() + "),posts(" + posts.size() + "),replies(" + replies.size() + "),albums(" + getRootAlbum().getAlbums().size() + ")]";
	}

}
