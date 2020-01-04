/**
 * Sone - NewLocalPostHandlerTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.notify.*
import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for [LocalPostHandler].
 */
class LocalPostHandlerTest {

	private val eventBus = EventBus()
	private val notificationManager = NotificationManager()
	private val notification = ListNotification<Post>("", "", Template())

	init {
		eventBus.register(LocalPostHandler(notificationManager, notification))
	}

	@Test
	fun `handler adds post by local sone to notification`() {
		eventBus.post(NewPostFoundEvent(localPost))
		assertThat(notification.elements, contains<Post>(localPost))
	}

	@Test
	fun `handler does not add post by remote sone to notification`() {
		eventBus.post(NewPostFoundEvent(remotePost))
		assertThat(notification.elements, emptyIterable())
	}

	@Test
	fun `handler adds notification to manager`() {
		eventBus.post(NewPostFoundEvent(remotePost))
		assertThat(notificationManager.notifications, contains<Notification>(notification))
	}

	@Test
	fun `handler does not add notification during first start`() {
		notificationManager.firstStart()
		eventBus.post(NewPostFoundEvent(remotePost))
		assertThat(notificationManager.notifications, not(hasItem<Notification>(notification)))
	}

	@Test
	fun `handler removes post from notification when post is removed`() {
		notification.add(localPost)
		notificationManager.addNotification(notification)
		eventBus.post(PostRemovedEvent(localPost))
		assertThat(notification.elements, emptyIterable())
	}

	@Test
	fun `handler does not remove remote post from notification when post is removed`() {
		notification.add(remotePost)
		notificationManager.addNotification(notification)
		eventBus.post(PostRemovedEvent(remotePost))
		assertThat(notification.elements, contains(remotePost))
	}

	@Test
	fun `handler removes post from notification when post is marked as known`() {
		notification.add(localPost)
		notificationManager.addNotification(notification)
		eventBus.post(MarkPostKnownEvent(localPost))
		assertThat(notification.elements, emptyIterable())
	}

	@Test
	fun `handler does not remove remote post from notification when post is marked as known`() {
		notification.add(remotePost)
		notificationManager.addNotification(notification)
		eventBus.post(MarkPostKnownEvent(remotePost))
		assertThat(notification.elements, contains(remotePost))
	}

}

private val localSone: Sone = object : IdOnlySone("local-sone") {
	override fun isLocal() = true
}
private val localPost: Post = object : Post.EmptyPost("local-post") {
	override fun getSone() = localSone
}
private val remoteSone: Sone = IdOnlySone("remote-sone")
private val remotePost: Post = object : Post.EmptyPost("remote-post") {
	override fun getSone() = remoteSone
}
