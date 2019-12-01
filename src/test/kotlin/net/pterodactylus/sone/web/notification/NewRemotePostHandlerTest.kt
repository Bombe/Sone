/**
 * Sone - NewRemotePostHandlerTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import java.io.*
import kotlin.test.*

/**
 * Unit test for [NewRemotePostHandler].
 */
@Suppress("UnstableApiUsage")
class NewRemotePostHandlerTest {

	private val eventBus = EventBus()
	private val notificationManager = NotificationManager()
	private val notification = ListNotification<Post>("", "", Template())
	private val handler = NewRemotePostHandler(notificationManager, notification)

	init {
		eventBus.register(handler)
	}

	@Test
	fun `handler adds post to new-post notification`() {
		eventBus.post(NewPostFoundEvent(post))
		assertThat(notification.elements, contains(post))
	}

	@Test
	fun `handler adds notification to notification manager`() {
		eventBus.post(NewPostFoundEvent(post))
		assertThat(notificationManager.notifications, contains<Notification>(notification))
	}

	@Test
	fun `handler does not add notification to notification manager during first start`() {
		notificationManager.addNotification(object : AbstractNotification("first-start-notification") {
			override fun render(writer: Writer?) = Unit
		})
		eventBus.post(NewPostFoundEvent(post))
		assertThat(notificationManager.notifications, not(hasItem(notification)))
	}

}

private val post: Post = Post.EmptyPost("post")
