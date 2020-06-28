/*
 * Sone - ListNotificationFilter.kt - Copyright © 2010–2020 David Roden
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
package net.pterodactylus.sone.notify

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.ifTrue
import net.pterodactylus.util.notify.Notification
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Filter for [ListNotification]s.
 */
@Singleton
class ListNotificationFilter @Inject constructor(private val postVisibilityFilter: PostVisibilityFilter, private val replyVisibilityFilter: ReplyVisibilityFilter) {

	/**
	 * Filters new-post and new-reply notifications in the given list of
	 * notifications. If `currentSone` is `null`, new-post and
	 * new-reply notifications are removed completely. If `currentSone` is
	 * not `null`, only posts that are posted by a friend Sone or the Sone
	 * itself, and replies that are replies to posts of friend Sones or the Sone
	 * itself will be retained in the notifications.
	 *
	 * @param notifications
	 * The notifications to filter
	 * @param currentSone
	 * The current Sone, or `null` if not logged in
	 * @return The filtered notifications
	 */
	fun filterNotifications(notifications: Collection<Notification>, currentSone: Sone?) =
			notifications.mapNotNull { it.filtered(currentSone) }

	@Suppress("UNCHECKED_CAST")
	private fun Notification.filtered(currentSone: Sone?): Notification? = when {
		isNewSoneNotification -> {
			takeIf { currentSone == null || currentSone.options.isShowNewSoneNotifications }
		}
		isNewPostNotification -> {
			(currentSone != null && currentSone.options.isShowNewPostNotifications).ifTrue {
				(this as ListNotification<Post>).filterNotification { postVisibilityFilter.isPostVisible(currentSone, it) }
			}
		}
		isNewReplyNotification -> {
			(currentSone != null && currentSone.options.isShowNewReplyNotifications).ifTrue {
				(this as ListNotification<PostReply>).filterNotification { replyVisibilityFilter.isReplyVisible(currentSone, it) }
			}
		}
		isMentionNotification -> {
			(this as ListNotification<Post>).filterNotification { postVisibilityFilter.isPostVisible(currentSone, it) }
		}
		else -> this
	}

}

/**
 * Filters the elements of this list notification.
 *
 * @param filter The filter for the notification’s elements
 * @return A list notification containing the filtered elements, or `null`
 * if the notification does not have any elements after filtering
 */
private fun <T> ListNotification<T>.filterNotification(filter: (T) -> Boolean) =
		elements.filter(filter).let { filteredElements ->
			when (filteredElements.size) {
				0 -> null
				elements.size -> this
				else -> ListNotification(this).apply {
					setElements(filteredElements)
					setLastUpdateTime(lastUpdatedTime)
				}
			}
		}
