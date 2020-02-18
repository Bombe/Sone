/*
 * Sone - MemoryDatabase.kt - Copyright © 2013–2020 David Roden
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

package net.pterodactylus.sone.database.memory

import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.TreeMultimap
import com.google.common.util.concurrent.*
import com.google.inject.Inject
import com.google.inject.Singleton
import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Reply.TIME_COMPARATOR
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.Sone.toAllAlbums
import net.pterodactylus.sone.data.allImages
import net.pterodactylus.sone.data.impl.AlbumBuilderImpl
import net.pterodactylus.sone.data.impl.ImageBuilderImpl
import net.pterodactylus.sone.database.AlbumBuilder
import net.pterodactylus.sone.database.Database
import net.pterodactylus.sone.database.DatabaseException
import net.pterodactylus.sone.database.ImageBuilder
import net.pterodactylus.sone.database.PostBuilder
import net.pterodactylus.sone.database.PostDatabase
import net.pterodactylus.sone.database.PostReplyBuilder
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.config.Configuration
import net.pterodactylus.util.config.ConfigurationException
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

/**
 * Memory-based [PostDatabase] implementation.
 */
@Singleton
class MemoryDatabase @Inject constructor(private val configuration: Configuration) : AbstractService(), Database {

	private val lock = ReentrantReadWriteLock()
	private val readLock by lazy { lock.readLock()!! }
	private val writeLock by lazy { lock.writeLock()!! }
	private val configurationLoader = ConfigurationLoader(configuration)
	private val allSones = mutableMapOf<String, Sone>()
	private val allPosts = mutableMapOf<String, Post>()
	private val sonePosts: Multimap<String, Post> = HashMultimap.create<String, Post>()
	private val knownPosts = mutableSetOf<String>()
	private val allPostReplies = mutableMapOf<String, PostReply>()
	private val sonePostReplies: Multimap<String, PostReply> = TreeMultimap.create<String, PostReply>(Comparator { leftString, rightString -> leftString.compareTo(rightString) }, TIME_COMPARATOR)
	private val knownPostReplies = mutableSetOf<String>()
	private val allAlbums = mutableMapOf<String, Album>()
	private val soneAlbums: Multimap<String, Album> = HashMultimap.create<String, Album>()
	private val allImages = mutableMapOf<String, Image>()
	private val soneImages: Multimap<String, Image> = HashMultimap.create<String, Image>()
	private val memoryBookmarkDatabase = MemoryBookmarkDatabase(this, configurationLoader)
	private val memoryFriendDatabase = MemoryFriendDatabase(configurationLoader)
	private val saveRateLimiter: RateLimiter = RateLimiter.create(1.0)
	private val saveKnownPostsRateLimiter: RateLimiter = RateLimiter.create(1.0)
	private val saveKnownPostRepliesRateLimiter: RateLimiter = RateLimiter.create(1.0)

	override val soneLoader get() = this::getSone

	override val sones get() = readLock.withLock { allSones.values.toSet() }

	override val localSones get() = readLock.withLock { allSones.values.filter(Sone::isLocal) }

	override val remoteSones get() = readLock.withLock { allSones.values.filterNot(Sone::isLocal) }

	override val bookmarkedPosts get() = memoryBookmarkDatabase.bookmarkedPosts

	override fun save() {
		if (saveRateLimiter.tryAcquire()) {
			saveKnownPosts()
			saveKnownPostReplies()
		}
	}

	override fun doStart() {
		memoryBookmarkDatabase.start()
		loadKnownPosts()
		loadKnownPostReplies()
		notifyStarted()
	}

	override fun doStop() {
		try {
			memoryBookmarkDatabase.stop()
			save()
			notifyStopped()
		} catch (de1: DatabaseException) {
			notifyFailed(de1)
		}
	}

	override fun newSoneBuilder() = MemorySoneBuilder(this)

	override fun storeSone(sone: Sone) {
		writeLock.withLock {
			removeSone(sone)

			allSones[sone.id] = sone
			sonePosts.putAll(sone.id, sone.posts)
			for (post in sone.posts) {
				allPosts[post.id] = post
			}
			sonePostReplies.putAll(sone.id, sone.replies)
			for (postReply in sone.replies) {
				allPostReplies[postReply.id] = postReply
			}
			soneAlbums.putAll(sone.id, toAllAlbums.apply(sone)!!)
			for (album in toAllAlbums.apply(sone)!!) {
				allAlbums[album.id] = album
			}
			sone.rootAlbum.allImages.let { images ->
				soneImages.putAll(sone.id, images)
				images.forEach { image -> allImages[image.id] = image }
			}
		}
	}

	override fun removeSone(sone: Sone) {
		writeLock.withLock {
			allSones.remove(sone.id)
			val removedPosts = sonePosts.removeAll(sone.id)
			for (removedPost in removedPosts) {
				allPosts.remove(removedPost.id)
			}
			val removedPostReplies = sonePostReplies.removeAll(sone.id)
			for (removedPostReply in removedPostReplies) {
				allPostReplies.remove(removedPostReply.id)
			}
			val removedAlbums = soneAlbums.removeAll(sone.id)
			for (removedAlbum in removedAlbums) {
				allAlbums.remove(removedAlbum.id)
			}
			val removedImages = soneImages.removeAll(sone.id)
			for (removedImage in removedImages) {
				allImages.remove(removedImage.id)
			}
		}
	}

	override fun getSone(soneId: String) = readLock.withLock { allSones[soneId] }

	override fun getFriends(localSone: Sone): Collection<String> =
			if (!localSone.isLocal) {
				emptySet()
			} else {
				memoryFriendDatabase.getFriends(localSone.id)
			}

	override fun isFriend(localSone: Sone, friendSoneId: String) =
			if (!localSone.isLocal) {
				false
			} else {
				memoryFriendDatabase.isFriend(localSone.id, friendSoneId)
			}

	override fun addFriend(localSone: Sone, friendSoneId: String) {
		if (!localSone.isLocal) {
			return
		}
		memoryFriendDatabase.addFriend(localSone.id, friendSoneId)
	}

	override fun removeFriend(localSone: Sone, friendSoneId: String) {
		if (!localSone.isLocal) {
			return
		}
		memoryFriendDatabase.removeFriend(localSone.id, friendSoneId)
	}

	override fun getFollowingTime(friendSoneId: String) =
			memoryFriendDatabase.getFollowingTime(friendSoneId)

	override fun getPost(postId: String) =
			readLock.withLock { allPosts[postId] }

	override fun getPosts(soneId: String): Collection<Post> =
			sonePosts[soneId].toSet()

	override fun getDirectedPosts(recipientId: String) =
			readLock.withLock {
				allPosts.values.filter {
					it.recipientId.orNull() == recipientId
				}
			}

	override fun newPostBuilder(): PostBuilder = MemoryPostBuilder(this, this)

	override fun storePost(post: Post) {
		checkNotNull(post, "post must not be null")
		writeLock.withLock {
			allPosts[post.id] = post
			sonePosts[post.sone.id].add(post)
		}
	}

	override fun removePost(post: Post) {
		checkNotNull(post, "post must not be null")
		writeLock.withLock {
			allPosts.remove(post.id)
			sonePosts[post.sone.id].remove(post)
			post.sone.removePost(post)
		}
	}

	override fun getPostReply(id: String) = readLock.withLock { allPostReplies[id] }

	override fun getReplies(postId: String) =
			readLock.withLock {
				allPostReplies.values
						.filter { it.postId == postId }
						.sortedWith(TIME_COMPARATOR)
			}

	override fun newPostReplyBuilder(): PostReplyBuilder =
			MemoryPostReplyBuilder(this, this)

	override fun storePostReply(postReply: PostReply) =
			writeLock.withLock {
				allPostReplies[postReply.id] = postReply
			}

	override fun removePostReply(postReply: PostReply) =
			writeLock.withLock {
				allPostReplies.remove(postReply.id)
			}.unit

	override fun getAlbum(albumId: String) = readLock.withLock { allAlbums[albumId] }

	override fun newAlbumBuilder(): AlbumBuilder = AlbumBuilderImpl()

	override fun storeAlbum(album: Album) =
			writeLock.withLock {
				allAlbums[album.id] = album
				soneAlbums.put(album.sone.id, album)
			}.unit

	override fun removeAlbum(album: Album) =
			writeLock.withLock {
				allAlbums.remove(album.id)
				soneAlbums.remove(album.sone.id, album)
			}.unit

	override fun getImage(imageId: String) = readLock.withLock { allImages[imageId] }

	override fun newImageBuilder(): ImageBuilder = ImageBuilderImpl()

	override fun storeImage(image: Image): Unit =
			writeLock.withLock {
				allImages[image.id] = image
				soneImages.put(image.sone.id, image)
			}

	override fun removeImage(image: Image): Unit =
			writeLock.withLock {
				allImages.remove(image.id)
				soneImages.remove(image.sone.id, image)
			}

	override fun bookmarkPost(post: Post) =
			memoryBookmarkDatabase.bookmarkPost(post)

	override fun unbookmarkPost(post: Post) =
			memoryBookmarkDatabase.unbookmarkPost(post)

	override fun isPostBookmarked(post: Post) =
			memoryBookmarkDatabase.isPostBookmarked(post)

	protected fun isPostKnown(post: Post) = readLock.withLock { post.id in knownPosts }

	fun setPostKnown(post: Post, known: Boolean): Unit =
			writeLock.withLock {
				if (known)
					knownPosts.add(post.id)
				else
					knownPosts.remove(post.id)
				saveKnownPosts()
			}

	protected fun isPostReplyKnown(postReply: PostReply) = readLock.withLock { postReply.id in knownPostReplies }

	fun setPostReplyKnown(postReply: PostReply, known: Boolean): Unit =
			writeLock.withLock {
				if (known)
					knownPostReplies.add(postReply.id)
				else
					knownPostReplies.remove(postReply.id)
				saveKnownPostReplies()
			}

	private fun loadKnownPosts() =
			configurationLoader.loadKnownPosts()
					.let {
						writeLock.withLock {
							knownPosts.clear()
							knownPosts.addAll(it)
						}
					}

	private fun saveKnownPosts() =
			saveKnownPostsRateLimiter.tryAcquire().ifTrue {
				try {
					readLock.withLock {
						knownPosts.forEachIndexed { index, knownPostId ->
							configuration.getStringValue("KnownPosts/$index/ID").value = knownPostId
						}
						configuration.getStringValue("KnownPosts/${knownPosts.size}/ID").value = null
					}
				} catch (ce1: ConfigurationException) {
					throw DatabaseException("Could not save database.", ce1)
				}
			}

	private fun loadKnownPostReplies(): Unit =
			configurationLoader.loadKnownPostReplies().let { knownPostReplies ->
				writeLock.withLock {
					this.knownPostReplies.clear()
					this.knownPostReplies.addAll(knownPostReplies)
				}
			}

	private fun saveKnownPostReplies() =
			saveKnownPostRepliesRateLimiter.tryAcquire().ifTrue {
				try {
					readLock.withLock {
						knownPostReplies.forEachIndexed { index, knownPostReply ->
							configuration.getStringValue("KnownReplies/$index/ID").value = knownPostReply
						}
						configuration.getStringValue("KnownReplies/${knownPostReplies.size}/ID").value = null
					}
				} catch (ce1: ConfigurationException) {
					throw DatabaseException("Could not save database.", ce1)
				}
			}

}
