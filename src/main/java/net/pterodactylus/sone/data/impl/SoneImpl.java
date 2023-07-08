/*
 * Sone - SoneImpl.java - Copyright © 2010–2020 David Roden
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

package net.pterodactylus.sone.data.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toList;
import static net.pterodactylus.sone.data.PostKt.newestPostFirst;
import static net.pterodactylus.sone.data.PostKt.noOldPost;
import static net.pterodactylus.sone.data.ReplyKt.newestReplyFirst;
import static net.pterodactylus.sone.data.ReplyKt.noOldReply;
import static net.pterodactylus.sone.data.SoneKt.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.AlbumKt;
import net.pterodactylus.sone.data.Client;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.SoneOptions;
import net.pterodactylus.sone.data.SoneOptions.DefaultSoneOptions;
import net.pterodactylus.sone.database.Database;
import net.pterodactylus.sone.freenet.wot.Identity;

import freenet.keys.FreenetURI;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * {@link Sone} implementation.
 * <p/>
 * Operations that modify the Sone need to synchronize on the Sone in question.
 */
public class SoneImpl implements Sone {

	/** The logger. */
	private static final Logger logger = getLogger(SoneImpl.class.getName());

	/** The database. */
	private final Database database;

	/** The ID of this Sone. */
	private final String id;

	/** Whether the Sone is local. */
	private final boolean local;

	/** The identity of this Sone. */
	private final Identity identity;

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

	/** All posts. */
	private final Set<Post> posts = new CopyOnWriteArraySet<>();

	/** All replies. */
	private final Set<PostReply> replies = new CopyOnWriteArraySet<>();

	/** The IDs of all liked posts. */
	private final Set<String> likedPostIds = new CopyOnWriteArraySet<>();

	/** The IDs of all liked replies. */
	private final Set<String> likedReplyIds = new CopyOnWriteArraySet<>();

	/** The root album containing all albums. */
	private final Album rootAlbum = new AlbumImpl(this);

	/** Sone-specific options. */
	private SoneOptions options = new DefaultSoneOptions();

	/**
	 * Creates a new Sone.
	 *
	 * @param database The database
	 * @param identity
	 * 		The identity of the Sone
	 * @param local
	 * 		{@code true} if the Sone is a local Sone, {@code false} otherwise
	 */
	public SoneImpl(Database database, Identity identity, boolean local) {
		this.database = database;
		this.id = identity.getId();
		this.identity = identity;
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
	@Nonnull
	public String getId() {
		return id;
	}

	/**
	 * Returns the identity of this Sone.
	 *
	 * @return The identity of this Sone
	 */
	@Nonnull
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * Returns the name of this Sone.
	 *
	 * @return The name of this Sone
	 */
	@Nonnull
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
	@Nonnull
	public FreenetURI getRequestUri() {
		try {
			return new FreenetURI(getIdentity().getRequestUri())
					.setKeyType("USK")
					.setDocName("Sone")
					.setMetaString(new String[0])
					.setSuggestedEdition(latestEdition);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(
					format("Identity %s's request URI is incorrect.",
							getIdentity()), e);
		}
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
	@Nonnull
	public Sone setTime(long time) {
		this.time = time;
		return this;
	}

	/**
	 * Returns the status of this Sone.
	 *
	 * @return The status of this Sone
	 */
	@Nonnull
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
	@Nonnull
	public Sone setStatus(@Nonnull SoneStatus status) {
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
	@Nonnull
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
	public void setProfile(@Nonnull Profile profile) {
		this.profile = new Profile(profile);
	}

	/**
	 * Returns the client used by this Sone.
	 *
	 * @return The client used by this Sone, or {@code null}
	 */
	@Nullable
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
	@Nonnull
	public Sone setClient(@Nullable Client client) {
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
	@Nonnull
	public Sone setKnown(boolean known) {
		this.known = known;
		return this;
	}

	/**
	 * Returns all friend Sones of this Sone.
	 *
	 * @return The friend Sones of this Sone
	 */
	@Nonnull
	public Collection<String> getFriends() {
		return database.getFriends(this);
	}

	/**
	 * Returns whether this Sone has the given Sone as a friend Sone.
	 *
	 * @param friendSoneId
	 * 		The ID of the Sone to check for
	 * @return {@code true} if this Sone has the given Sone as a friend, {@code
	 *         false} otherwise
	 */
	public boolean hasFriend(@Nonnull String friendSoneId) {
		return database.isFriend(this, friendSoneId);
	}

	/**
	 * Returns the list of posts of this Sone, sorted by time, newest first.
	 *
	 * @return All posts of this Sone
	 */
	@Nonnull
	public List<Post> getPosts() {
		List<Post> sortedPosts;
		synchronized (this) {
			sortedPosts = new ArrayList<>(posts);
		}
		sortedPosts.sort(newestPostFirst());
        return sortedPosts;
	}

	/**
	 * Sets all posts of this Sone at once.
	 *
	 * @param posts
	 * 		The new (and only) posts of this Sone
	 * @return This Sone (for method chaining)
	 */
	@Nonnull
	public Sone setPosts(@Nonnull Collection<Post> posts) {
		List<Post> sortedPosts;
		synchronized (this) {
			sortedPosts = new ArrayList<>(posts);
		}
		sortedPosts.sort(newestPostFirst());
        List<Post> limitedPosts = this.local
            ? sortedPosts
            : filterRemotePosts(sortedPosts);
		synchronized (this) {
			this.posts.clear();
			this.posts.addAll(limitedPosts);
		}
		return this;
	}

	@NotNull
	public static List<Post> filterRemotePosts(List<Post> sortedPosts) {
		return sortedPosts.stream()
				.filter(noOldPost()::invoke)
				.limit(100)
				.collect(toList());
	}

	/**
	 * Adds the given post to this Sone. The post will not be added if its {@link
	 * Post#getSone() Sone} is not this Sone.
	 *
	 * @param post
	 * 		The post to add
	 */
	public void addPost(@Nonnull Post post) {
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
	public void removePost(@Nonnull Post post) {
		if (post.getSone().equals(this)) {
			posts.remove(post);
		}
	}

	/**
	 * Returns all replies this Sone made.
	 *
	 * @return All replies this Sone made
	 */
	@Nonnull
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
	@Nonnull
	public Sone setReplies(@Nonnull Collection<PostReply> replies) {
		List<PostReply> sortedReplies;
		synchronized (this) {
			sortedReplies = new ArrayList<>(replies);
		}
		sortedReplies.sort(newestReplyFirst());
        List<PostReply> limitedReplies = this.local
            ? sortedReplies
            : filterRemoteReplies(sortedReplies);
		this.replies.clear();
		this.replies.addAll(limitedReplies);
		return this;
	}

	@NotNull
	public static List<PostReply> filterRemoteReplies(List<PostReply> sortedReplies) {
		return sortedReplies.stream()
				.filter(noOldReply()::invoke)
				.limit(100)
				.collect(toList());
	}

	/**
	 * Adds a reply to this Sone. If the given reply was not made by this Sone,
	 * nothing is added to this Sone.
	 *
	 * @param reply
	 * 		The reply to add
	 */
	public void addReply(@Nonnull PostReply reply) {
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
	public void removeReply(@Nonnull PostReply reply) {
		if (reply.getSone().equals(this)) {
			replies.remove(reply);
		}
	}

	/**
	 * Returns the IDs of all liked posts.
	 *
	 * @return All liked posts’ IDs
	 */
	@Nonnull
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
	@Nonnull
	public Sone setLikePostIds(@Nonnull Set<String> likedPostIds) {
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
	public boolean isLikedPostId(@Nonnull String postId) {
		return likedPostIds.contains(postId);
	}

	/**
	 * Adds the given post ID to the list of posts this Sone likes.
	 *
	 * @param postId
	 * 		The ID of the post
	 * @return This Sone (for method chaining)
	 */
	@Nonnull
	public Sone addLikedPostId(@Nonnull String postId) {
		likedPostIds.add(postId);
		return this;
	}

	/**
	 * Removes the given post ID from the list of posts this Sone likes.
	 *
	 * @param postId
	 * 		The ID of the post
	 */
	public void removeLikedPostId(@Nonnull String postId) {
		likedPostIds.remove(postId);
	}

	/**
	 * Returns the IDs of all liked replies.
	 *
	 * @return All liked replies’ IDs
	 */
	@Nonnull
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
	@Nonnull
	public Sone setLikeReplyIds(@Nonnull Set<String> likedReplyIds) {
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
	public boolean isLikedReplyId(@Nonnull String replyId) {
		return likedReplyIds.contains(replyId);
	}

	/**
	 * Adds the given reply ID to the list of replies this Sone likes.
	 *
	 * @param replyId
	 * 		The ID of the reply
	 * @return This Sone (for method chaining)
	 */
	@Nonnull
	public Sone addLikedReplyId(@Nonnull String replyId) {
		likedReplyIds.add(replyId);
		return this;
	}

	/**
	 * Removes the given post ID from the list of replies this Sone likes.
	 *
	 * @param replyId
	 * 		The ID of the reply
	 */
	public void removeLikedReplyId(@Nonnull String replyId) {
		likedReplyIds.remove(replyId);
	}

	/**
	 * Returns the root album that contains all visible albums of this Sone.
	 *
	 * @return The root album of this Sone
	 */
	@Nonnull
	public Album getRootAlbum() {
		return rootAlbum;
	}

	/**
	 * Returns Sone-specific options.
	 *
	 * @return The options of this Sone
	 */
	@Nonnull
	public SoneOptions getOptions() {
		return options;
	}

	/**
	 * Sets the options of this Sone.
	 *
	 * @param options
	 * 		The options of this Sone
	 */
	/* TODO - remove this method again, maybe add an option provider */
	public void setOptions(@Nonnull SoneOptions options) {
		this.options = options;
	}

	//
	// FINGERPRINTABLE METHODS
	//

	/** {@inheritDoc} */
	@Override
	public synchronized String getFingerprint() {
		Hasher hash = Hashing.sha256().newHasher();
		hash.putString(profile.getFingerprint(), UTF_8);

		hash.putString("Posts(", UTF_8);
		for (Post post : getPosts()) {
			hash.putString("Post(", UTF_8).putString(post.getId(), UTF_8).putString(")", UTF_8);
		}
		hash.putString(")", UTF_8);

		List<PostReply> replies = new ArrayList<>(getReplies());
		replies.sort(newestReplyFirst().reversed());
		hash.putString("Replies(", UTF_8);
		for (PostReply reply : replies) {
			hash.putString("Reply(", UTF_8).putString(reply.getId(), UTF_8).putString(")", UTF_8);
		}
		hash.putString(")", UTF_8);

		List<String> likedPostIds = new ArrayList<>(getLikedPostIds());
		Collections.sort(likedPostIds);
		hash.putString("LikedPosts(", UTF_8);
		for (String likedPostId : likedPostIds) {
			hash.putString("Post(", UTF_8).putString(likedPostId, UTF_8).putString(")", UTF_8);
		}
		hash.putString(")", UTF_8);

		List<String> likedReplyIds = new ArrayList<>(getLikedReplyIds());
		Collections.sort(likedReplyIds);
		hash.putString("LikedReplies(", UTF_8);
		for (String likedReplyId : likedReplyIds) {
			hash.putString("Reply(", UTF_8).putString(likedReplyId, UTF_8).putString(")", UTF_8);
		}
		hash.putString(")", UTF_8);

		hash.putString("Albums(", UTF_8);
		for (Album album : rootAlbum.getAlbums()) {
			if (!AlbumKt.notEmpty().invoke(album)) {
				continue;
			}
			hash.putString(album.getFingerprint(), UTF_8);
		}
		hash.putString(")", UTF_8);

		return hash.hash().toString();
	}

	//
	// INTERFACE Comparable<Sone>
	//

	/** {@inheritDoc} */
	@Override
	public int compareTo(Sone sone) {
		return niceNameComparator().compare(this, sone);
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
		return ((Sone) object).getId().equals(id);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getClass().getName() + "[identity=" + identity + ",posts(" + posts.size() + "),replies(" + replies.size() + "),albums(" + getRootAlbum().getAlbums().size() + ")]";
	}

}
