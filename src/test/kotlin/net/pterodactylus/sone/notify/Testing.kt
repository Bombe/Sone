package net.pterodactylus.sone.notify

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone

fun matchThisPost(post: Post) = createPostVisibilityFilter { _, p -> p == post }
val showAllPosts = createPostVisibilityFilter { _, _ -> true }
val showNoPosts = createPostVisibilityFilter { _, _ -> false }

private fun createPostVisibilityFilter(visible: (Sone?, Post) -> Boolean) = object : PostVisibilityFilter {
	override fun isPostVisible(sone: Sone?, post: Post) = visible(sone, post)
}

fun matchThisReply(reply: PostReply) = createReplyVisibilityFilter(showAllPosts) { _, r -> r == reply }
val showAllReplies = createReplyVisibilityFilter(showAllPosts) { _, _ -> true }
val showNoReplies = createReplyVisibilityFilter(showAllPosts) { _, _ -> false }

private fun createReplyVisibilityFilter(postVisibilityFilter: PostVisibilityFilter, visible: (Sone?, PostReply) -> Boolean): ReplyVisibilityFilter = object : DefaultReplyVisibilityFilter(postVisibilityFilter) {
	override fun isReplyVisible(sone: Sone?, reply: PostReply) = visible(sone, reply)
}
