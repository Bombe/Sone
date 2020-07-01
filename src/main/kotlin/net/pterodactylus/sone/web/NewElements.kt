/**
 * Sone - NewElements.kt - Copyright © 2020 David ‘Bombe’ Roden
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
package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.notify.ListNotification
import net.pterodactylus.sone.notify.PostVisibilityFilter
import net.pterodactylus.sone.notify.ReplyVisibilityFilter
import javax.inject.Inject
import javax.inject.Named

/**
 * Container for new elements that should be shown in the web interface.
 *
 * This is just a wrapper around the notifications that store the new elements.
 */
class NewElements @Inject constructor(
		@Named("newRemotePost") private val newPostNotification: ListNotification<Post>,
		@Named("newRemotePostReply") private val newReplyNotification: ListNotification<PostReply>,
		@Named("localPost") private val localPostNotification: ListNotification<Post>,
		@Named("localReply") private val localReplyNotification: ListNotification<PostReply>,
		private val postVisibilityFilter: PostVisibilityFilter,
		private val replyVisibilityFilter: ReplyVisibilityFilter
) {

	val newPosts: Collection<Post>
		get() = listOf(newPostNotification, localPostNotification)
				.flatMap(ListNotification<Post>::elements)
				.filter { postVisibilityFilter.isPostVisible(null, it) }

	val newReplies: Collection<PostReply>
		get() = listOf(newReplyNotification, localReplyNotification)
				.flatMap(ListNotification<PostReply>::elements)
				.filter { replyVisibilityFilter.isReplyVisible(null, it) }
}

private fun <R> Collection<*>.cast(): List<R> = map { it as R }
