/**
 * Sone - SoneMentionedHandlerTest.kt - Copyright © 2020 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.data.Post.*
import net.pterodactylus.sone.notify.*
import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for [SoneMentionedHandler].
 */
@Suppress("UnstableApiUsage")
class SoneMentionedHandlerTest {

	private val notificationManager = NotificationManager()
	private val notification = ListNotification<Post>("", "", Template())
	private val eventBus = EventBus()

	init {
		eventBus.register(SoneMentionedHandler(notificationManager, notification))
	}

	@Test
	fun `handler adds notification to manager on event`() {
		eventBus.post(MentionOfLocalSoneFoundEvent(post))
		assertThat(notificationManager.notifications, contains<Notification>(notification))
	}

	@Test
	fun `handler adds post to notification on event`() {
		eventBus.post(MentionOfLocalSoneFoundEvent(post))
		assertThat(notification.elements, contains<Post>(post))
	}

	@Test
	fun `handler does not add notification during first start`() {
		notificationManager.firstStart()
		eventBus.post(MentionOfLocalSoneFoundEvent(post))
		assertThat(notificationManager.notifications, not(hasItem<Notification>(notification)))
	}

	@Test
	fun `handler does not add post to notification during first start`() {
		notificationManager.firstStart()
		eventBus.post(MentionOfLocalSoneFoundEvent(post))
		assertThat(notification.elements, not(hasItem<Post>(post)))
	}

	@Test
	fun `handler removes post from notification`() {
		notification.add(post)
		eventBus.post(MentionOfLocalSoneRemovedEvent(post))
		assertThat(notification.elements, not(hasItem(post)))
	}

	@Test
	fun `handler removes notification from manager`() {
		notificationManager.addNotification(notification)
		eventBus.post(MentionOfLocalSoneRemovedEvent(post))
		assertThat(notificationManager.notifications, not(hasItem<Notification>(notification)))
	}

}

private val post = EmptyPost("")
