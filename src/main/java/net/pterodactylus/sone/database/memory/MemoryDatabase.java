/*
 * Sone - MemoryDatabase.java - Copyright © 2013 David Roden
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
import static net.pterodactylus.sone.data.Reply.TIME_COMPARATOR;

import java.util.ArrayList;
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
import net.pterodactylus.sone.database.SoneProvider;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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

	/** All posts by their ID. */
	private final Map<String, Post> allPosts = new HashMap<String, Post>();

	/** All posts by their Sones. */
	private final Multimap<String, Post> sonePosts = HashMultimap.create();

	/** All posts by their recipient. */
	private final Multimap<String, Post> recipientPosts = HashMultimap.create();

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

	/** Replies by post. */
	private final SortedSetMultimap<String, PostReply> postReplies = TreeMultimap.create(new Comparator<String>() {

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
		loadKnownPosts();
		loadKnownPostReplies();
		notifyStarted();
	}

	/** {@inheritDocs} */
	@Override
	protected void doStop() {
		try {
			save();
			notifyStopped();
		} catch (DatabaseException de1) {
			notifyFailed(de1);
		}
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
	public Collection<Post> getDirectedPosts(String recipientId) {
		lock.readLock().lock();
		try {
			Collection<Post> posts = recipientPosts.get(recipientId);
			return (posts == null) ? Collections.<Post>emptySet() : new HashSet<Post>(posts);
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
			if (post.getRecipientId().isPresent()) {
				getPostsTo(post.getRecipientId().get()).add(post);
			}
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
			if (post.getRecipientId().isPresent()) {
				getPostsTo(post.getRecipientId().get()).remove(post);
			}
			post.getSone().removePost(post);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/** {@inheritDocs} */
	@Override
	public void storePosts(Sone sone, Collection<Post> posts) throws IllegalArgumentException {
		checkNotNull(sone, "sone must not be null");
		/* verify that all posts are from the same Sone. */
		for (Post post : posts) {
			if (!sone.equals(post.getSone())) {
				throw new IllegalArgumentException(String.format("Post from different Sone found: %s", post));
			}
		}

		lock.writeLock().lock();
		try {
			/* remove all posts by the Sone. */
			Collection<Post> oldPosts = getPostsFrom(sone.getId());
			for (Post post : oldPosts) {
				allPosts.remove(post.getId());
				if (post.getRecipientId().isPresent()) {
					getPostsTo(post.getRecipientId().get()).remove(post);
				}
			}

			/* add new posts. */
			getPostsFrom(sone.getId()).addAll(posts);
			for (Post post : posts) {
				allPosts.put(post.getId(), post);
				if (post.getRecipientId().isPresent()) {
					getPostsTo(post.getRecipientId().get()).add(post);
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/** {@inheritDocs} */
	@Override
	public void removePosts(Sone sone) {
		checkNotNull(sone, "sone must not be null");
		lock.writeLock().lock();
		try {
			/* remove all posts by the Sone. */
			getPostsFrom(sone.getId()).clear();
			for (Post post : sone.getPosts()) {
				allPosts.remove(post.getId());
				if (post.getRecipientId().isPresent()) {
					getPostsTo(post.getRecipientId().get()).remove(post);
				}
			}
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
	public List<PostReply> getReplies(String postId) {
		lock.readLock().lock();
		try {
			if (!postReplies.containsKey(postId)) {
				return Collections.emptyList();
			}
			return new ArrayList<PostReply>(postReplies.get(postId));
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
			postReplies.put(postReply.getPostId(), postReply);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/** {@inheritDocs} */
	@Override
	public void storePostReplies(Sone sone, Collection<PostReply> postReplies) {
		checkNotNull(sone, "sone must not be null");
		/* verify that all posts are from the same Sone. */
		for (PostReply postReply : postReplies) {
			if (!sone.equals(postReply.getSone())) {
				throw new IllegalArgumentException(String.format("PostReply from different Sone found: %s", postReply));
			}
		}

		lock.writeLock().lock();
		try {
			/* remove all post replies of the Sone. */
			for (PostReply postReply : getRepliesFrom(sone.getId())) {
				removePostReply(postReply);
			}
			for (PostReply postReply : postReplies) {
				allPostReplies.put(postReply.getId(), postReply);
				sonePostReplies.put(postReply.getSone().getId(), postReply);
				this.postReplies.put(postReply.getPostId(), postReply);
			}
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
			if (postReplies.containsKey(postReply.getPostId())) {
				postReplies.get(postReply.getPostId()).remove(postReply);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/** {@inheritDocs} */
	@Override
	public void removePostReplies(Sone sone) {
		checkNotNull(sone, "sone must not be null");

		lock.writeLock().lock();
		try {
			for (PostReply postReply : sone.getReplies()) {
				removePostReply(postReply);
			}
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

	/**
	 * Gets all posts that are directed the given Sone, creating a new collection
	 * if there is none yet.
	 *
	 * @param recipientId
	 * 		The ID of the Sone to get the posts for
	 * @return All posts
	 */
	private Collection<Post> getPostsTo(String recipientId) {
		lock.readLock().lock();
		try {
			return recipientPosts.get(recipientId);
		} finally {
			lock.readLock().unlock();
		}
	}

	/** Loads the known posts. */
	private void loadKnownPosts() {
		lock.writeLock().lock();
		try {
			int postCounter = 0;
			while (true) {
				String knownPostId = configuration.getStringValue("KnownPosts/" + postCounter++ + "/ID").getValue(null);
				if (knownPostId == null) {
					break;
				}
				knownPosts.add(knownPostId);
			}
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

	/**
	 * Returns all replies by the given Sone.
	 *
	 * @param id
	 * 		The ID of the Sone
	 * @return The post replies of the Sone, sorted by time (newest first)
	 */
	private Collection<PostReply> getRepliesFrom(String id) {
		lock.readLock().lock();
		try {
			return Collections.unmodifiableCollection(sonePostReplies.get(id));
		} finally {
			lock.readLock().unlock();
		}
	}

	/** Loads the known post replies. */
	private void loadKnownPostReplies() {
		lock.writeLock().lock();
		try {
			int replyCounter = 0;
			while (true) {
				String knownReplyId = configuration.getStringValue("KnownReplies/" + replyCounter++ + "/ID").getValue(null);
				if (knownReplyId == null) {
					break;
				}
				knownPostReplies.add(knownReplyId);
			}
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
