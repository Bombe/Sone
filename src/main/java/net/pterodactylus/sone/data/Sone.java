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

import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static net.pterodactylus.sone.data.Album.FLATTENER;
import static net.pterodactylus.sone.data.Album.IMAGES;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import net.pterodactylus.sone.core.Options;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.template.SoneAccessor;

import freenet.keys.FreenetURI;

import com.google.common.base.Predicate;
import com.google.common.primitives.Ints;

/**
 * A Sone defines everything about a user: her profile, her status updates, her
 * replies, her likes and dislikes, etc.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Sone extends Identified, Fingerprintable, Comparable<Sone> {

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
			return (sone != null) && (sone.getTime() != 0);
		}
	};

	/** Filter that matches all {@link Sone#isLocal() local Sones}. */
	public static final Predicate<Sone> LOCAL_SONE_FILTER = new Predicate<Sone>() {

		@Override
		public boolean apply(Sone sone) {
			return (sone != null) && (sone.getIdentity() instanceof OwnIdentity);
		}

	};

	/** Filter that matches Sones that have at least one album. */
	public static final Predicate<Sone> HAS_ALBUM_FILTER = new Predicate<Sone>() {

		@Override
		public boolean apply(Sone sone) {
			return (sone != null) && !sone.getRootAlbum().getAlbums().isEmpty();
		}
	};

	/**
	 * Returns the identity of this Sone.
	 *
	 * @return The identity of this Sone
	 */
	Identity getIdentity();

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
	Sone setIdentity(Identity identity) throws IllegalArgumentException;

	/**
	 * Returns the name of this Sone.
	 *
	 * @return The name of this Sone
	 */
	String getName();

	/**
	 * Returns whether this Sone is a local Sone.
	 *
	 * @return {@code true} if this Sone is a local Sone, {@code false} otherwise
	 */
	boolean isLocal();

	/**
	 * Returns the request URI of this Sone.
	 *
	 * @return The request URI of this Sone
	 */
	FreenetURI getRequestUri();

	/**
	 * Sets the request URI of this Sone.
	 *
	 * @param requestUri
	 * 		The request URI of this Sone
	 * @return This Sone (for method chaining)
	 */
	Sone setRequestUri(FreenetURI requestUri);

	/**
	 * Returns the insert URI of this Sone.
	 *
	 * @return The insert URI of this Sone
	 */
	FreenetURI getInsertUri();

	/**
	 * Sets the insert URI of this Sone.
	 *
	 * @param insertUri
	 * 		The insert URI of this Sone
	 * @return This Sone (for method chaining)
	 */
	Sone setInsertUri(FreenetURI insertUri);

	/**
	 * Returns the latest edition of this Sone.
	 *
	 * @return The latest edition of this Sone
	 */
	long getLatestEdition();

	/**
	 * Sets the latest edition of this Sone. If the given latest edition is not
	 * greater than the current latest edition, the latest edition of this Sone is
	 * not changed.
	 *
	 * @param latestEdition
	 * 		The latest edition of this Sone
	 */
	void setLatestEdition(long latestEdition);

	/**
	 * Return the time of the last inserted update of this Sone.
	 *
	 * @return The time of the update (in milliseconds since Jan 1, 1970 UTC)
	 */
	long getTime();

	/**
	 * Sets the time of the last inserted update of this Sone.
	 *
	 * @param time
	 * 		The time of the update (in milliseconds since Jan 1, 1970 UTC)
	 * @return This Sone (for method chaining)
	 */
	Sone setTime(long time);

	/**
	 * Returns the status of this Sone.
	 *
	 * @return The status of this Sone
	 */
	SoneStatus getStatus();

	/**
	 * Sets the new status of this Sone.
	 *
	 * @param status
	 * 		The new status of this Sone
	 * @return This Sone
	 * @throws IllegalArgumentException
	 * 		if {@code status} is {@code null}
	 */
	Sone setStatus(SoneStatus status);

	/**
	 * Returns a copy of the profile. If you want to update values in the profile
	 * of this Sone, update the values in the returned {@link Profile} and use
	 * {@link #setProfile(Profile)} to change the profile in this Sone.
	 *
	 * @return A copy of the profile
	 */
	Profile getProfile();

	/**
	 * Sets the profile of this Sone. A copy of the given profile is stored so that
	 * subsequent modifications of the given profile are not reflected in this
	 * Sone!
	 *
	 * @param profile
	 * 		The profile to set
	 */
	void setProfile(Profile profile);

	/**
	 * Returns the client used by this Sone.
	 *
	 * @return The client used by this Sone, or {@code null}
	 */
	Client getClient();

	/**
	 * Sets the client used by this Sone.
	 *
	 * @param client
	 * 		The client used by this Sone, or {@code null}
	 * @return This Sone (for method chaining)
	 */
	Sone setClient(Client client);

	/**
	 * Returns whether this Sone is known.
	 *
	 * @return {@code true} if this Sone is known, {@code false} otherwise
	 */
	boolean isKnown();

	/**
	 * Sets whether this Sone is known.
	 *
	 * @param known
	 * 		{@code true} if this Sone is known, {@code false} otherwise
	 * @return This Sone
	 */
	Sone setKnown(boolean known);

	/**
	 * Returns all friend Sones of this Sone.
	 *
	 * @return The friend Sones of this Sone
	 */
	List<String> getFriends();

	/**
	 * Returns whether this Sone has the given Sone as a friend Sone.
	 *
	 * @param friendSoneId
	 * 		The ID of the Sone to check for
	 * @return {@code true} if this Sone has the given Sone as a friend, {@code
	 *         false} otherwise
	 */
	boolean hasFriend(String friendSoneId);

	/**
	 * Adds the given Sone as a friend Sone.
	 *
	 * @param friendSone
	 * 		The friend Sone to add
	 * @return This Sone (for method chaining)
	 */
	Sone addFriend(String friendSone);

	/**
	 * Removes the given Sone as a friend Sone.
	 *
	 * @param friendSoneId
	 * 		The ID of the friend Sone to remove
	 * @return This Sone (for method chaining)
	 */
	Sone removeFriend(String friendSoneId);

	/**
	 * Returns the list of posts of this Sone, sorted by time, newest first.
	 *
	 * @return All posts of this Sone
	 */
	List<Post> getPosts();

	/**
	 * Sets all posts of this Sone at once.
	 *
	 * @param posts
	 * 		The new (and only) posts of this Sone
	 * @return This Sone (for method chaining)
	 */
	Sone setPosts(Collection<Post> posts);

	/**
	 * Adds the given post to this Sone. The post will not be added if its {@link
	 * Post#getSone() Sone} is not this Sone.
	 *
	 * @param post
	 * 		The post to add
	 */
	void addPost(Post post);

	/**
	 * Removes the given post from this Sone.
	 *
	 * @param post
	 * 		The post to remove
	 */
	void removePost(Post post);

	/**
	 * Returns all replies this Sone made.
	 *
	 * @return All replies this Sone made
	 */
	Set<PostReply> getReplies();

	/**
	 * Sets all replies of this Sone at once.
	 *
	 * @param replies
	 * 		The new (and only) replies of this Sone
	 * @return This Sone (for method chaining)
	 */
	Sone setReplies(Collection<PostReply> replies);

	/**
	 * Adds a reply to this Sone. If the given reply was not made by this Sone,
	 * nothing is added to this Sone.
	 *
	 * @param reply
	 * 		The reply to add
	 */
	void addReply(PostReply reply);

	/**
	 * Removes a reply from this Sone.
	 *
	 * @param reply
	 * 		The reply to remove
	 */
	void removeReply(PostReply reply);

	/**
	 * Returns the IDs of all liked posts.
	 *
	 * @return All liked posts’ IDs
	 */
	Set<String> getLikedPostIds();

	/**
	 * Sets the IDs of all liked posts.
	 *
	 * @param likedPostIds
	 * 		All liked posts’ IDs
	 * @return This Sone (for method chaining)
	 */
	Sone setLikePostIds(Set<String> likedPostIds);

	/**
	 * Checks whether the given post ID is liked by this Sone.
	 *
	 * @param postId
	 * 		The ID of the post
	 * @return {@code true} if this Sone likes the given post, {@code false}
	 *         otherwise
	 */
	boolean isLikedPostId(String postId);

	/**
	 * Adds the given post ID to the list of posts this Sone likes.
	 *
	 * @param postId
	 * 		The ID of the post
	 * @return This Sone (for method chaining)
	 */
	Sone addLikedPostId(String postId);

	/**
	 * Removes the given post ID from the list of posts this Sone likes.
	 *
	 * @param postId
	 * 		The ID of the post
	 * @return This Sone (for method chaining)
	 */
	Sone removeLikedPostId(String postId);

	/**
	 * Returns the IDs of all liked replies.
	 *
	 * @return All liked replies’ IDs
	 */
	Set<String> getLikedReplyIds();

	/**
	 * Sets the IDs of all liked replies.
	 *
	 * @param likedReplyIds
	 * 		All liked replies’ IDs
	 * @return This Sone (for method chaining)
	 */
	Sone setLikeReplyIds(Set<String> likedReplyIds);

	/**
	 * Checks whether the given reply ID is liked by this Sone.
	 *
	 * @param replyId
	 * 		The ID of the reply
	 * @return {@code true} if this Sone likes the given reply, {@code false}
	 *         otherwise
	 */
	boolean isLikedReplyId(String replyId);

	/**
	 * Adds the given reply ID to the list of replies this Sone likes.
	 *
	 * @param replyId
	 * 		The ID of the reply
	 * @return This Sone (for method chaining)
	 */
	Sone addLikedReplyId(String replyId);

	/**
	 * Removes the given post ID from the list of replies this Sone likes.
	 *
	 * @param replyId
	 * 		The ID of the reply
	 * @return This Sone (for method chaining)
	 */
	Sone removeLikedReplyId(String replyId);

	/**
	 * Returns the root album that contains all visible albums of this Sone.
	 *
	 * @return The root album of this Sone
	 */
	Album getRootAlbum();

	/**
	 * Returns Sone-specific options.
	 *
	 * @return The options of this Sone
	 */
	SoneOptions getOptions();

	/**
	 * Sets the options of this Sone.
	 *
	 * @param options
	 * 		The options of this Sone
	 */
	/* TODO - remove this method again, maybe add an option provider */
	void setOptions(SoneOptions options);

}
