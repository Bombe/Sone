/**
 * Sone - LocalReplyHandlerTest.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for [LocalReplyHandler].
 */
class LocalReplyHandlerTest {

	private val notification = ListNotification<PostReply>("", "", Template())
	private val localReplyHandlerTester = NotificationHandlerTester { LocalReplyHandler(it, notification) }

	@Test
	fun `handler does not add reply to notification`() {
		localReplyHandlerTester.sendEvent(NewPostReplyFoundEvent(remoteReply))
		assertThat(notification.elements, emptyIterable())
	}

	@Test
	fun `handler does add local reply to notification`() {
		localReplyHandlerTester.sendEvent(NewPostReplyFoundEvent(localReply))
		assertThat(notification.elements, contains(localReply))
	}

	@Test
	fun `handler adds notification to manager`() {
		localReplyHandlerTester.sendEvent(NewPostReplyFoundEvent(localReply))
		assertThat(localReplyHandlerTester.notifications, hasItem(notification))
	}

	@Test
	fun `handler does not add notification to manager for remote reply`() {
		localReplyHandlerTester.sendEvent(NewPostReplyFoundEvent(remoteReply))
		assertThat(localReplyHandlerTester.notifications, not(hasItem(notification)))
	}

	@Test
	fun `handler does not add notification to manager during first start`() {
		localReplyHandlerTester.firstStart()
		localReplyHandlerTester.sendEvent(NewPostReplyFoundEvent(localReply))
		assertThat(localReplyHandlerTester.notifications, not(hasItem(notification)))
	}

	@Test
	fun `handler removes reply from notification if reply is removed`() {
		notification.add(localReply)
		localReplyHandlerTester.sendEvent(PostReplyRemovedEvent(localReply))
		assertThat(notification.elements, not(hasItem(localReply)))
	}

	@Test
	fun `handler removes reply from notification if reply is marked as known`() {
		notification.add(localReply)
		localReplyHandlerTester.sendEvent(MarkPostReplyKnownEvent(localReply))
		assertThat(notification.elements, not(hasItem(localReply)))
	}

}

private val localReply = emptyPostReply(sone = localSone1)
private val remoteReply = emptyPostReply()
