/*
 * Sone - MemoryPostDatabase.java - Copyright © 2013 David Roden
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.impl.AbstractPostBuilder;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostDatabase;
import net.pterodactylus.sone.database.SoneProvider;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;

import com.google.common.base.Optional;
import com.google.inject.Inject;

/**
 * Memory-based {@link PostDatabase} implementation.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MemoryPostDatabase implements PostDatabase {

	/** The lock. */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	/** The Sone provider. */
	private final SoneProvider soneProvider;

	/** All posts by their ID. */
	private final Map<String, Post> allPosts = new HashMap<String, Post>();

	/** All posts by their Sones. */
	private final Map<String, Collection<Post>> sonePosts = new HashMap<String, Collection<Post>>();

	/** All posts by their recipient. */
	private final Map<String, Collection<Post>> recipientPosts = new HashMap<String, Collection<Post>>();

	/** Whether posts are known. */
	private final Set<String> knownPosts = new HashSet<String>();

	/**
	 * Creates a new memory database.
	 *
	 * @param soneProvider
	 *            The Sone provider
	 */
	@Inject
	public MemoryPostDatabase(SoneProvider soneProvider) {
		this.soneProvider = soneProvider;
	}

	//
	// POSTPROVIDER METHODS
	//

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Optional<Post> getPost(String postId) {
		lock.readLock().lock();
		try {
			return Optional.fromNullable(allPosts.get(postId));
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Collection<Post> getPosts(String soneId) {
		return new HashSet<Post>(getPostsFrom(soneId));
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Collection<Post> getDirectedPosts(String recipientId) {
		lock.readLock().lock();
		try {
			Collection<Post> posts = recipientPosts.get(recipientId);
			return (posts == null) ? Collections.<Post> emptySet() : new HashSet<Post>(posts);
		} finally {
			lock.readLock().unlock();
		}
	}

	//
	// POSTBUILDERFACTORY METHODS
	//

	/**
	 * {@inheritDocs}
	 */
	@Override
	public PostBuilder newPostBuilder() {
		return new MemoryPostBuilder(soneProvider);
	}

	//
	// POSTSTORE METHODS
	//

	/**
	 * {@inheritDocs}
	 */
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

	/**
	 * {@inheritDocs}
	 */
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

	/**
	 * {@inheritDocs}
	 */
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
			getPostsFrom(sone.getId()).clear();
			for (Post post : posts) {
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

	/**
	 * {@inheritDocs}
	 */
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
	// POSTDATABASE METHODS
	//

	/**
	 * {@inheritDocs}
	 */
	@Override
	public void loadKnownPosts(Configuration configuration, String prefix) {
		lock.writeLock().lock();
		try {
			int postCounter = 0;
			while (true) {
				String knownPostId = configuration.getStringValue(prefix + postCounter++ + "/ID").getValue(null);
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
	 * {@inheritDocs}
	 */
	@Override
	public void saveKnownPosts(Configuration configuration, String prefix) throws ConfigurationException {
		lock.readLock().lock();
		try {
			int postCounter = 0;
			for (String knownPostId : knownPosts) {
				configuration.getStringValue(prefix + postCounter++ + "/ID").setValue(knownPostId);
			}
			configuration.getStringValue(prefix + postCounter + "/ID").setValue(null);
		} finally {
			lock.readLock().unlock();
		}
	}

	//
	// PACKAGE-PRIVATE METHODS
	//

	/**
	 * Returns whether the given post is known.
	 *
	 * @param post
	 *            The post
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
	 *            The post
	 * @param known
	 *            {@code true} if the post is known, {@code false} otherwise
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

	//
	// PRIVATE METHODS
	//

	/**
	 * Gets all posts for the given Sone, creating a new collection if there is
	 * none yet.
	 *
	 * @param soneId
	 *            The ID of the Sone to get the posts for
	 * @return All posts
	 */
	private Collection<Post> getPostsFrom(String soneId) {
		Collection<Post> posts = null;
		lock.readLock().lock();
		try {
			posts = sonePosts.get(soneId);
		} finally {
			lock.readLock().unlock();
		}
		if (posts != null) {
			return posts;
		}

		posts = new HashSet<Post>();
		lock.writeLock().lock();
		try {
			sonePosts.put(soneId, posts);
		} finally {
			lock.writeLock().unlock();
		}

		return posts;
	}

	/**
	 * Gets all posts that are directed the given Sone, creating a new
	 * collection if there is none yet.
	 *
	 * @param recipientId
	 *            The ID of the Sone to get the posts for
	 * @return All posts
	 */
	private Collection<Post> getPostsTo(String recipientId) {
		Collection<Post> posts = null;
		lock.readLock().lock();
		try {
			posts = recipientPosts.get(recipientId);
		} finally {
			lock.readLock().unlock();
		}
		if (posts != null) {
			return posts;
		}

		posts = new HashSet<Post>();
		lock.writeLock().lock();
		try {
			recipientPosts.put(recipientId, posts);
		} finally {
			lock.writeLock().unlock();
		}

		return posts;
	}

	/**
	 * {@link PostBuilder} implementation that creates a {@link MemoryPost}.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private class MemoryPostBuilder extends AbstractPostBuilder {

		/**
		 * Creates a new memory post builder.
		 *
		 * @param soneProvider
		 *            The Sone provider
		 */
		public MemoryPostBuilder(SoneProvider soneProvider) {
			super(soneProvider);
		}

		/**
		 * {@inheritDocs}
		 */
		@Override
		public Post build() throws IllegalStateException {
			validate();
			Post post = new MemoryPost(MemoryPostDatabase.this, soneProvider, randomId ? UUID.randomUUID().toString() : id, senderId, recipientId, currentTime ? System.currentTimeMillis() : time, text);
			post.setKnown(isPostKnown(post));
			return post;
		}

	}

}
