/**
 * Sone - SoneMentionDetector.kt - Copyright © 2019 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.utils.*
import javax.inject.*

/**
 * Listens to [NewPostFoundEvent]s and [NewPostReplyFoundEvent], parses the
 * texts and emits a [LocalSoneMentionedInPostEvent] if a [SoneTextParser]
 * finds a [SonePart] that points to a local [Sone].
 */
class SoneMentionDetector @Inject constructor(private val eventBus: EventBus, private val soneTextParser: SoneTextParser) {

	@Subscribe
	fun onNewPost(newPostFoundEvent: NewPostFoundEvent) {
		newPostFoundEvent.post.let { post ->
			post.sone.isLocal.onFalse {
				val parts = soneTextParser.parse(post.text, null)
				if (parts.filterIsInstance<SonePart>().any { it.sone.isLocal }) {
					eventBus.post(LocalSoneMentionedInPostEvent(post))
				}
			}
		}
	}

	@Subscribe
	fun onNewPostReply(event: NewPostReplyFoundEvent) {
		event.postReply.let { postReply ->
			postReply.sone.isLocal.onFalse {
				if (soneTextParser.parse(postReply.text, null).filterIsInstance<SonePart>().any { it.sone.isLocal }) {
					postReply.post.let(::LocalSoneMentionedInPostEvent).also(eventBus::post)
				}
			}
		}
	}

}
