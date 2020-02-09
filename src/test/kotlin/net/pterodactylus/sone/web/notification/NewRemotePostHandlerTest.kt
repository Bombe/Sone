/**
 * Sone - NewRemotePostHandlerTest.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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

import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.Post.*
import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.notify.*
import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for [NewRemotePostHandler].
 */
@Suppress("UnstableApiUsage")
class NewRemotePostHandlerTest {

	private val notification = ListNotification<Post>("", "", Template())
	private val remotePostHandlerTest = NotificationHandlerTester { NewRemotePostHandler(it, notification) }

	@Test
	fun `handler adds remote post to new-post notification`() {
		remotePostHandlerTest.sendEvent(NewPostFoundEvent(remotePost))
		assertThat(notification.elements, contains(remotePost))
	}

	@Test
	fun `handler does not add local post to new-post notification`() {
		remotePostHandlerTest.sendEvent(NewPostFoundEvent(localPost))
		assertThat(notification.elements, emptyIterable())
	}

	@Test
	fun `handler adds notification for remote post to notification manager`() {
		remotePostHandlerTest.sendEvent(NewPostFoundEvent(remotePost))
		assertThat(remotePostHandlerTest.notifications, contains<Notification>(notification))
	}

	@Test
	fun `handler does not add notification for local post to notification manager`() {
		remotePostHandlerTest.sendEvent(NewPostFoundEvent(localPost))
		assertThat(remotePostHandlerTest.notifications, emptyIterable())
	}

	@Test
	fun `handler does not add notification to notification manager during first start`() {
		remotePostHandlerTest.firstStart()
		remotePostHandlerTest.sendEvent(NewPostFoundEvent(remotePost))
		assertThat(remotePostHandlerTest.notifications, not(hasItem(notification)))
	}

	@Test
	fun `handler removes post from notification if post is removed`() {
		notification.add(remotePost)
		remotePostHandlerTest.sendEvent(PostRemovedEvent(remotePost))
		assertThat(notification.elements, not(hasItem(remotePost)))
	}

	@Test
	fun `handler removes post from notification if post is marked as known`() {
		notification.add(remotePost)
		remotePostHandlerTest.sendEvent(MarkPostKnownEvent(remotePost))
		assertThat(notification.elements, not(hasItem(remotePost)))
	}

}

private val remoteSone: Sone = IdOnlySone("remote-sone")
private val remotePost: Post = object : EmptyPost("remote-post") {
	override fun getSone() = remoteSone
}

private val localSone: Sone = object : IdOnlySone("local-sone") {
	override fun isLocal() = true
}
private val localPost: Post = object : EmptyPost("local-post") {
	override fun getSone() = localSone
}
