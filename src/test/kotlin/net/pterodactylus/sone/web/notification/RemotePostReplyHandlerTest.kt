/**
 * Sone - RemotePostReplyHandlerTest.kt - Copyright © 2020 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.notify.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for [RemotePostReplyHandler].
 */
class RemotePostReplyHandlerTest {

	private val notification = ListNotification<PostReply>("", "", Template())
	private val notificationHandlerTester = NotificationHandlerTester { RemotePostReplyHandler(it, notification) }
	private val postReply = emptyPostReply()

	@Test
	fun `reply is added to notification on new reply`() {
		notificationHandlerTester.sendEvent(NewPostReplyFoundEvent(postReply))
		assertThat(notification.elements, hasItem<PostReply>(postReply))
	}

	@Test
	fun `notification is added to manager on new reply`() {
		notificationHandlerTester.sendEvent(NewPostReplyFoundEvent(postReply))
		assertThat(notificationHandlerTester.notifications, hasItem<Notification>(notification))
	}

	@Test
	fun `reply is not added to notification on new reply during first start`() {
		notificationHandlerTester.firstStart()
		notificationHandlerTester.sendEvent(NewPostReplyFoundEvent(postReply))
		assertThat(notification.elements, not(hasItem<PostReply>(postReply)))
	}

	@Test
	fun `notification is not added to manager on new reply during first start`() {
		notificationHandlerTester.firstStart()
		notificationHandlerTester.sendEvent(NewPostReplyFoundEvent(postReply))
		assertThat(notificationHandlerTester.notifications, not(hasItem<Notification>(notification)))
	}

	@Test
	fun `reply is not added to notification on new local reply`() {
		val postReply = emptyPostReply(sone = localSone1)
		notificationHandlerTester.sendEvent(NewPostReplyFoundEvent(postReply))
		assertThat(notification.elements, not(hasItem<PostReply>(postReply)))
	}

	@Test
	fun `notification is not added to manager on new local reply`() {
		val postReply = emptyPostReply(sone = localSone1)
		notificationHandlerTester.sendEvent(NewPostReplyFoundEvent(postReply))
		assertThat(notificationHandlerTester.notifications, not(hasItem<Notification>(notification)))
	}

	@Test
	fun `reply is removed from notification when removed`() {
		notification.add(postReply)
		notificationHandlerTester.sendEvent(PostReplyRemovedEvent(postReply))
		assertThat(notification.elements, not(hasItem<PostReply>(postReply)))
	}

	@Test
	fun `reply is removed from notification when marked as known`() {
		notification.add(postReply)
		notificationHandlerTester.sendEvent(MarkPostReplyKnownEvent(postReply))
		assertThat(notification.elements, not(hasItem<PostReply>(postReply)))
	}

}
