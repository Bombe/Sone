/**
 * Sone - SoneMentionDetector.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.text

import com.google.common.eventbus.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.utils.*
import javax.inject.*

/**
 * Listens to [NewPostFoundEvent]s and [NewPostReplyFoundEvent], parses the
 * texts and emits a [MentionOfLocalSoneFoundEvent] if a [SoneTextParser]
 * finds a [SonePart] that points to a local [Sone].
 */
class SoneMentionDetector @Inject constructor(private val eventBus: EventBus, private val soneTextParser: SoneTextParser, private val postReplyProvider: PostReplyProvider) {

	@Subscribe
	fun onNewPost(newPostFoundEvent: NewPostFoundEvent) {
		newPostFoundEvent.post.let { post ->
			post.sone.isLocal.onFalse {
				if (post.text.hasLinksToLocalSones()) {
					mentionedPosts += post
					eventBus.post(MentionOfLocalSoneFoundEvent(post))
				}
			}
		}
	}

	@Subscribe
	fun onNewPostReply(event: NewPostReplyFoundEvent) {
		event.postReply.let { postReply ->
			postReply.sone.isLocal.onFalse {
				if (postReply.text.hasLinksToLocalSones()) {
					postReply.post
							.also { mentionedPosts += it }
							.let(::MentionOfLocalSoneFoundEvent)
							?.also(eventBus::post)
				}
			}
		}
	}

	@Subscribe
	fun onPostRemoved(event: PostRemovedEvent) {
		unmentionPost(event.post)
	}

	@Subscribe
	fun onPostMarkedKnown(event: MarkPostKnownEvent) {
		unmentionPost(event.post)
	}

	@Subscribe
	fun onReplyRemoved(event: PostReplyRemovedEvent) {
		event.postReply.post.let {
			if ((!it.text.hasLinksToLocalSones() || it.isKnown) && (it.replies.filterNot { it == event.postReply }.none { it.text.hasLinksToLocalSones() && !it.isKnown })) {
				unmentionPost(it)
			}
		}
	}

	private fun unmentionPost(post: Post) {
		if (post in mentionedPosts) {
			eventBus.post(MentionOfLocalSoneRemovedEvent(post))
			mentionedPosts -= post
		}
	}

	private val mentionedPosts = mutableSetOf<Post>()

	private fun String.hasLinksToLocalSones() = soneTextParser.parse(this, null)
			.filterIsInstance<SonePart>()
			.any { it.sone.isLocal }

	private val Post.replies get() = postReplyProvider.getReplies(id)

}
