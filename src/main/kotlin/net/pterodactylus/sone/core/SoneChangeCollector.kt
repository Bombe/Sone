package net.pterodactylus.sone.core

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone

/**
 * Wrapper around a [SoneChangeDetector] that can turn changed elements into
 * different elements which are then being returned. This can be used to turn
 * changed elements into events for further processing.
 */
class SoneChangeCollector(private val oldSone: Sone) {

	private val newPostEventCreators = mutableListOf<(Post) -> Any?>()
	private val removedPostEventCreators = mutableListOf<(Post) -> Any?>()
	private val newPostReplyEventCreators = mutableListOf<(PostReply) -> Any?>()
	private val removedPostReplyEventCreators = mutableListOf<(PostReply) -> Any?>()

	fun onNewPost(postProcessor: (Post) -> Unit) =
			newPostEventCreators.add { postProcessor(it).let { null } }.let { this }

	fun newPostEvent(postEventCreator: (Post) -> Any?) =
			newPostEventCreators.add(postEventCreator).let { this }

	fun onNewPostReply(postReplyProcessor: (PostReply) -> Unit) =
			newPostReplyEventCreators.add { postReplyProcessor(it).let { null } }.let { this }

	fun newPostReplyEvent(postReplyEventCreator: (PostReply) -> Any?) =
			newPostReplyEventCreators.add(postReplyEventCreator).let { this }

	fun removedPostEvent(postEventCreator: (Post) -> Any?) =
			removedPostEventCreators.add(postEventCreator).let { this }

	fun onRemovedPostReply(postReplyEventCreator: (PostReply) -> Any?) =
			removedPostReplyEventCreators.add(postReplyEventCreator).let { this }

	fun detectChanges(newSone: Sone): List<Any> {
		val events = mutableListOf<Any>()
		SoneChangeDetector(oldSone).apply {
			onNewPosts { post -> newPostEventCreators.mapNotNull { it(post) }.forEach { events.add(it) } }
			onRemovedPosts { post -> removedPostEventCreators.mapNotNull { it(post) }.forEach { events.add(it) } }
			onNewPostReplies { reply -> newPostReplyEventCreators.mapNotNull { it(reply) }.forEach { events.add(it) } }
			onRemovedPostReplies { reply -> removedPostReplyEventCreators.mapNotNull { it(reply) }.forEach { events.add(it) } }
			detectChanges(newSone)
		}
		return events
	}

}
