package net.pterodactylus.sone.database

import net.pterodactylus.sone.data.Post

/**
 * Database interface for bookmark-related functionality.
 */
interface BookmarkDatabase {

	val bookmarkedPosts: Set<Post>

	fun bookmarkPost(post: Post)
	fun unbookmarkPost(post: Post)
	fun isPostBookmarked(post: Post): Boolean

}
