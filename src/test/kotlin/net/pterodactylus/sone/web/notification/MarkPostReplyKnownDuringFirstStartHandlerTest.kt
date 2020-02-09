/**
 * Sone - MarkPostReplyKnownDuringFirstStartHandlerTest.kt - Copyright © 2020 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import java.util.function.*
import kotlin.test.*

/**
 * Unit test for [MarkPostReplyKnownDuringFirstStartHandler].
 */
class MarkPostReplyKnownDuringFirstStartHandlerTest {

	private val markedAsKnown = mutableListOf<PostReply>()
	private val notificationTester = NotificationHandlerTester { MarkPostReplyKnownDuringFirstStartHandler(it, Consumer { markedAsKnown += it }) }
	private val postReply = emptyPostReply()

	@Test
	fun `post reply is marked as known on new reply during first start`() {
		notificationTester.firstStart()
		notificationTester.sendEvent(NewPostReplyFoundEvent(postReply))
		assertThat(markedAsKnown, contains(postReply))
	}

	@Test
	fun `post reply is not marked as known on new reply if not during first start`() {
		notificationTester.sendEvent(NewPostReplyFoundEvent(postReply))
		assertThat(markedAsKnown, not(hasItem(postReply)))
	}

}
