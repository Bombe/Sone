/*
 * Sone - MemoryDatabase.java - Copyright © 2013–2016 David Roden
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

package net.pterodactylus.sone.database.memory;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static net.pterodactylus.sone.data.Reply.TIME_COMPARATOR;
import static net.pterodactylus.sone.data.Sone.LOCAL_SONE_FILTER;
import static net.pterodactylus.sone.data.Sone.toAllAlbums;
import static net.pterodactylus.sone.data.Sone.toAllImages;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.impl.AlbumBuilderImpl;
import net.pterodactylus.sone.data.impl.ImageBuilderImpl;
import net.pterodactylus.sone.database.AlbumBuilder;
import net.pterodactylus.sone.database.Database;
import net.pterodactylus.sone.database.DatabaseException;
import net.pterodactylus.sone.database.ImageBuilder;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostDatabase;
import net.pterodactylus.sone.database.PostReplyBuilder;
import net.pterodactylus.sone.database.SoneBuilder;
import net.pterodactylus.sone.database.SoneProvider;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import kotlin.jvm.functions.Function1;

/**
 * Memory-based {@link PostDatabase} implementation.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
@Singleton
public class MemoryDatabase extends AbstractService implements Database {

	/** The lock. */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	/** The Sone provider. */
	private final SoneProvider soneProvider;

	/** The configuration. */
	private final Configuration configuration;
	private final ConfigurationLoader configurationLoader;

	private final Map<String, Sone> allSones = new HashMap<String, Sone>();

	/** All posts by their ID. */
	private final Map<String, Post> allPosts = new HashMap<String, Post>();

	/** All posts by their Sones. */
	private final Multimap<String, Post> sonePosts = HashMultimap.create();

	/** Whether posts are known. */
	private final Set<String> knownPosts = new HashSet<String>();

	/** All post replies by their ID. */
	private final Map<String, PostReply> allPostReplies = new HashMap<String, PostReply>();

	/** Replies sorted by Sone. */
	private final SortedSetMultimap<String, PostReply> sonePostReplies = TreeMultimap.create(new Comparator<String>() {

		@Override
		public int compare(String leftString, String rightString) {
			return leftString.compareTo(rightString);
		}
	}, TIME_COMPARATOR);

	/** Whether post replies are known. */
	private final Set<String> knownPostReplies = new HashSet<String>();

	private final Map<String, Album> allAlbums = new HashMap<String, Album>();
	private final Multimap<String, Album> soneAlbums = HashMultimap.create();

	private final Map<String, Image> allImages = new HashMap<String, Image>();
	private final Multimap<String, Image> soneImages = HashMultimap.create();

	private final MemoryBookmarkDatabase memoryBookmarkDatabase;
	private final MemoryFriendDatabase memoryFriendDatabase;

	/**
	 * Creates a new memory database.
	 *
	 * @param soneProvider
	 * 		The Sone provider
	 * @param configuration
	 * 		The configuration for loading and saving elements
	 */
	@Inject
	public MemoryDatabase(SoneProvider soneProvider, Configuration configuration) {
		this.soneProvider = soneProvider;
		this.configuration = configuration;
		this.configurationLoader = new ConfigurationLoader(configuration);
		memoryBookmarkDatabase =
				new MemoryBookmarkDatabase(this, configurationLoader);
		memoryFriendDatabase = new MemoryFriendDatabase(configurationLoader);
	}

	//
	// DATABASE METHODS
	//

	/**
	 * Saves the database.
	 *
	 * @throws DatabaseException
	 * 		if an error occurs while saving
	 */
	@Override
	public void save() throws DatabaseException {
		saveKnownPosts();
		saveKnownPostReplies();
	}

	//
	// SERVICE METHODS
	//

	/** {@inheritDocs} */
	@Override
	protected void doStart() {
		memoryBookmarkDatabase.start();
		loadKnownPosts();
		loadKnownPostReplies();
		notifyStarted();
	}

	/** {@inheritDocs} */
	@Override
	protected void doStop() {
		try {
			memoryBookmarkDatabase.stop();
			save();
			notifyStopped();
		} catch (DatabaseException de1) {
			notifyFailed(de1);
		}
	}

	@Override
	public SoneBuilder newSoneBuilder() {
		return new MemorySoneBuilder(this);
	}

	@Override
	public void storeSone(Sone sone) {
		lock.writeLock().lock();
		try {
			removeSone(sone);

			allSones.put(sone.getId(), sone);
			sonePosts.putAll(sone.getId(), sone.getPosts());
			for (Post post : sone.getPosts()) {
				allPosts.put(post.getId(), post);
			}
			sonePostReplies.putAll(sone.getId(), sone.getReplies());
			for (PostReply postReply : sone.getReplies()) {
				allPostReplies.put(postReply.getId(), postReply);
			}
			soneAlbums.putAll(sone.getId(), toAllAlbums.apply(sone));
			for (Album album : toAllAlbums.apply(sone)) {
				allAlbums.put(album.getId(), album);
			}
			soneImages.putAll(sone.getId(), toAllImages.apply(sone));
			for (Image image : toAllImages.apply(sone)) {
				allImages.put(image.getId(), image);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void removeSone(Sone sone) {
		lock.writeLock().lock();
		try {
			allSones.remove(sone.getId());
			Collection<Post> removedPosts = sonePosts.removeAll(sone.getId());
			for (Post removedPost : removedPosts) {
				allPosts.remove(removedPost.getId());
			}
			Collection<PostReply> removedPostReplies =
					sonePostReplies.removeAll(sone.getId());
			for (PostReply removedPostReply : removedPostReplies) {
				allPostReplies.remove(removedPostReply.getId());
			}
			Collection<Album> removedAlbums =
					soneAlbums.removeAll(sone.getId());
			for (Album removedAlbum : removedAlbums) {
				allAlbums.remove(removedAlbum.getId());
			}
			Collection<Image> removedImages =
					soneImages.removeAll(sone.getId());
			for (Image removedImage : removedImages) {
				allImages.remove(removedImage.getId());
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Nonnull
	@Override
	public Function1<String, Sone> getSoneLoader() {
		return new Function1<String, Sone>() {
			@Override
			public Sone invoke(String soneId) {
				return getSone(soneId);
			}
		};
	}

	@Override
	public Sone getSone(String soneId) {
		lock.readLock().lock();
		try {
			return allSones.get(soneId);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<Sone> getSones() {
		lock.readLock().lock();
		try {
			return new HashSet<Sone>(allSones.values());
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<Sone> getLocalSones() {
		lock.readLock().lock();
		try {
			return from(allSones.values()).filter(LOCAL_SONE_FILTER).toSet();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<Sone> getRemoteSones() {
		lock.readLock().lock();
		try {
			return from(allSones.values())
					.filter(not(LOCAL_SONE_FILTER)) .toSet();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<String> getFriends(Sone localSone) {
		if (!localSone.isLocal()) {
			return Collections.emptySet();
		}
		return memoryFriendDatabase.getFriends(localSone.getId());
	}

	@Override
	public boolean isFriend(Sone localSone, String friendSoneId) {
		if (!localSone.isLocal()) {
			return false;
		}
		return memoryFriendDatabase.isFriend(localSone.getId(), friendSoneId);
	}

	@Override
	public void addFriend(Sone localSone, String friendSoneId) {
		if (!localSone.isLocal()) {
			return;
		}
		memoryFriendDatabase.addFriend(localSone.getId(), friendSoneId);
	}

	@Override
	public void removeFriend(Sone localSone, String friendSoneId) {
		if (!localSone.isLocal()) {
			return;
		}
		memoryFriendDatabase.removeFriend(localSone.getId(), friendSoneId);
	}

	//
	// POSTPROVIDER METHODS
	//

	/** {@inheritDocs} */
	@Override
	public Optional<Post> getPost(String postId) {
		lock.readLock().lock();
		try {
			return fromNullable(allPosts.get(postId));
		} finally {
			lock.readLock().unlock();
		}
	}

	/** {@inheritDocs} */
	@Override
	public Collection<Post> getPosts(String soneId) {
		return new HashSet<Post>(getPostsFrom(soneId));
	}

	/** {@inheritDocs} */
	@Override
	public Collection<Post> getDirectedPosts(final String recipientId) {
		lock.readLock().lock();
		try {
			return from(sonePosts.values()).filter(new Predicate<Post>() {
				@Override
				public boolean apply(Post post) {
					return post.getRecipientId().asSet().contains(recipientId);
				}
			}).toSet();
		} finally {
			lock.readLock().unlock();
		}
	}

	//
	// POSTBUILDERFACTORY METHODS
	//

	/** {@inheritDocs} */
	@Override
	public PostBuilder newPostBuilder() {
		return new MemoryPostBuilder(this, soneProvider);
	}

	//
	// POSTSTORE METHODS
	//

	/** {@inheritDocs} */
	@Override
	public void storePost(Post post) {
		checkNotNull(post, "post must not be null");
		lock.writeLock().lock();
		try {
			allPosts.put(post.getId(), post);
			getPostsFrom(post.getSone().getId()).add(post);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/** {@inheritDocs} */
	@Override
	public void removePost(Post post) {
		checkNotNull(post, "post must not be null");
		lock.writeLock().lock();
		try {
			allPosts.remove(post.getId());
			getPostsFrom(post.getSone().getId()).remove(post);
			post.getSone().removePost(post);
		} finally {
			lock.writeLock().unlock();
		}
	}

	//
	// POSTREPLYPROVIDER METHODS
	//

	/** {@inheritDocs} */
	@Override
	public Optional<PostReply> getPostReply(String id) {
		lock.readLock().lock();
		try {
			return fromNullable(allPostReplies.get(id));
		} finally {
			lock.readLock().unlock();
		}
	}

	/** {@inheritDocs} */
	@Override
	public List<PostReply> getReplies(final String postId) {
		lock.readLock().lock();
		try {
			return from(allPostReplies.values())
					.filter(new Predicate<PostReply>() {
						@Override
						public boolean apply(PostReply postReply) {
							return postReply.getPostId().equals(postId);
						}
					}).toSortedList(TIME_COMPARATOR);
		} finally {
			lock.readLock().unlock();
		}
	}

	//
	// POSTREPLYBUILDERFACTORY METHODS
	//

	/** {@inheritDocs} */
	@Override
	public PostReplyBuilder newPostReplyBuilder() {
		return new MemoryPostReplyBuilder(this, soneProvider);
	}

	//
	// POSTREPLYSTORE METHODS
	//

	/** {@inheritDocs} */
	@Override
	public void storePostReply(PostReply postReply) {
		lock.writeLock().lock();
		try {
			allPostReplies.put(postReply.getId(), postReply);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/** {@inheritDocs} */
	@Override
	public void removePostReply(PostReply postReply) {
		lock.writeLock().lock();
		try {
			allPostReplies.remove(postReply.getId());
		} finally {
			lock.writeLock().unlock();
		}
	}

	//
	// ALBUMPROVDER METHODS
	//

	@Override
	public Optional<Album> getAlbum(String albumId) {
		lock.readLock().lock();
		try {
			return fromNullable(allAlbums.get(albumId));
		} finally {
			lock.readLock().unlock();
		}
	}

	//
	// ALBUMBUILDERFACTORY METHODS
	//

	@Override
	public AlbumBuilder newAlbumBuilder() {
		return new AlbumBuilderImpl();
	}

	//
	// ALBUMSTORE METHODS
	//

	@Override
	public void storeAlbum(Album album) {
		lock.writeLock().lock();
		try {
			allAlbums.put(album.getId(), album);
			soneAlbums.put(album.getSone().getId(), album);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void removeAlbum(Album album) {
		lock.writeLock().lock();
		try {
			allAlbums.remove(album.getId());
			soneAlbums.remove(album.getSone().getId(), album);
		} finally {
			lock.writeLock().unlock();
		}
	}

	//
	// IMAGEPROVIDER METHODS
	//

	@Override
	public Optional<Image> getImage(String imageId) {
		lock.readLock().lock();
		try {
			return fromNullable(allImages.get(imageId));
		} finally {
			lock.readLock().unlock();
		}
	}

	//
	// IMAGEBUILDERFACTORY METHODS
	//

	@Override
	public ImageBuilder newImageBuilder() {
		return new ImageBuilderImpl();
	}

	//
	// IMAGESTORE METHODS
	//

	@Override
	public void storeImage(Image image) {
		lock.writeLock().lock();
		try {
			allImages.put(image.getId(), image);
			soneImages.put(image.getSone().getId(), image);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void removeImage(Image image) {
		lock.writeLock().lock();
		try {
			allImages.remove(image.getId());
			soneImages.remove(image.getSone().getId(), image);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void bookmarkPost(Post post) {
		memoryBookmarkDatabase.bookmarkPost(post);
	}

	@Override
	public void unbookmarkPost(Post post) {
		memoryBookmarkDatabase.unbookmarkPost(post);
	}

	@Override
	public boolean isPostBookmarked(Post post) {
		return memoryBookmarkDatabase.isPostBookmarked(post);
	}

	@Override
	public Set<Post> getBookmarkedPosts() {
		return memoryBookmarkDatabase.getBookmarkedPosts();
	}

	//
	// PACKAGE-PRIVATE METHODS
	//

	/**
	 * Returns whether the given post is known.
	 *
	 * @param post
	 * 		The post
	 * @return {@code true} if the post is known, {@code false} otherwise
	 */
	boolean isPostKnown(Post post) {
		lock.readLock().lock();
		try {
			return knownPosts.contains(post.getId());
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Sets whether the given post is known.
	 *
	 * @param post
	 * 		The post
	 * @param known
	 * 		{@code true} if the post is known, {@code false} otherwise
	 */
	void setPostKnown(Post post, boolean known) {
		lock.writeLock().lock();
		try {
			if (known) {
				knownPosts.add(post.getId());
			} else {
				knownPosts.remove(post.getId());
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Returns whether the given post reply is known.
	 *
	 * @param postReply
	 * 		The post reply
	 * @return {@code true} if the given post reply is known, {@code false}
	 *         otherwise
	 */
	boolean isPostReplyKnown(PostReply postReply) {
		lock.readLock().lock();
		try {
			return knownPostReplies.contains(postReply.getId());
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Sets whether the given post reply is known.
	 *
	 * @param postReply
	 * 		The post reply
	 * @param known
	 * 		{@code true} if the post reply is known, {@code false} otherwise
	 */
	void setPostReplyKnown(PostReply postReply, boolean known) {
		lock.writeLock().lock();
		try {
			if (known) {
				knownPostReplies.add(postReply.getId());
			} else {
				knownPostReplies.remove(postReply.getId());
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Gets all posts for the given Sone, creating a new collection if there is
	 * none yet.
	 *
	 * @param soneId
	 * 		The ID of the Sone to get the posts for
	 * @return All posts
	 */
	private Collection<Post> getPostsFrom(String soneId) {
		lock.readLock().lock();
		try {
			return sonePosts.get(soneId);
		} finally {
			lock.readLock().unlock();
		}
	}

	/** Loads the known posts. */
	private void loadKnownPosts() {
		Set<String> knownPosts = configurationLoader.loadKnownPosts();
		lock.writeLock().lock();
		try {
			this.knownPosts.clear();
			this.knownPosts.addAll(knownPosts);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Saves the known posts to the configuration.
	 *
	 * @throws DatabaseException
	 * 		if a configuration error occurs
	 */
	private void saveKnownPosts() throws DatabaseException {
		lock.readLock().lock();
		try {
			int postCounter = 0;
			for (String knownPostId : knownPosts) {
				configuration.getStringValue("KnownPosts/" + postCounter++ + "/ID").setValue(knownPostId);
			}
			configuration.getStringValue("KnownPosts/" + postCounter + "/ID").setValue(null);
		} catch (ConfigurationException ce1) {
			throw new DatabaseException("Could not save database.", ce1);
		} finally {
			lock.readLock().unlock();
		}
	}

	/** Loads the known post replies. */
	private void loadKnownPostReplies() {
		Set<String> knownPostReplies = configurationLoader.loadKnownPostReplies();
		lock.writeLock().lock();
		try {
			this.knownPostReplies.clear();
			this.knownPostReplies.addAll(knownPostReplies);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Saves the known post replies to the configuration.
	 *
	 * @throws DatabaseException
	 * 		if a configuration error occurs
	 */
	private void saveKnownPostReplies() throws DatabaseException {
		lock.readLock().lock();
		try {
			int replyCounter = 0;
			for (String knownReplyId : knownPostReplies) {
				configuration.getStringValue("KnownReplies/" + replyCounter++ + "/ID").setValue(knownReplyId);
			}
			configuration.getStringValue("KnownReplies/" + replyCounter + "/ID").setValue(null);
		} catch (ConfigurationException ce1) {
			throw new DatabaseException("Could not save database.", ce1);
		} finally {
			lock.readLock().unlock();
		}
	}

}
