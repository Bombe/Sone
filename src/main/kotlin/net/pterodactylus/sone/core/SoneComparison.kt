package net.pterodactylus.sone.core

import net.pterodactylus.sone.data.*

class SoneComparison(private val oldSone: Sone, private val newSone: Sone) {

	val newPosts: Collection<Post> get() = newSone.posts - oldSone.posts
	val removedPosts: Collection<Post> get() = oldSone.posts - newSone.posts
	val newPostReplies: Collection<PostReply> get() = newSone.replies - oldSone.replies
	val removedPostReplies: Collection<PostReply> get() = oldSone.replies - newSone.replies

}
