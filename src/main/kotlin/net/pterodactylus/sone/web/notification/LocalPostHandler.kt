/**
 * Sone - NewLocalPostHandler.kt - Copyright © 2019 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.web.notification

import com.google.common.eventbus.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.notify.*
import net.pterodactylus.util.notify.*
import javax.inject.*

/**
 * Handler for local posts.
 */
class LocalPostHandler @Inject constructor(private val notificationManager: NotificationManager, @Named("localPost") private val notification: ListNotification<Post>) {

	@Subscribe
	fun newPostFound(newPostFoundEvent: NewPostFoundEvent) {
		newPostFoundEvent.post.onLocal { post ->
			notification.add(post)
			if (!notificationManager.hasFirstStartNotification()) {
				notificationManager.addNotification(notification)
			}
		}
	}

	@Subscribe
	fun postRemoved(postRemovedEvent: PostRemovedEvent) {
		postRemovedEvent.post.onLocal { post ->
			notification.remove(post)
		}
	}

	@Subscribe
	fun postMarkedAsKnown(markPostKnownEvent: MarkPostKnownEvent) {
		markPostKnownEvent.post.onLocal { post ->
			notification.remove(post)
		}
	}

}

private fun Post.onLocal(action: (Post) -> Unit) =
		if (sone.isLocal) action(this) else Unit
