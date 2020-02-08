/**
 * Sone - SoneInsertHandlerTest.kt - Copyright © 2020 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for [SoneInsertHandler].
 */
class SoneInsertHandlerTest {

	private val localSone = createLocalSone()
	private val notification1 = TemplateNotification(Template())
	private val notification2 = TemplateNotification(Template())
	private val soneInsertHandlerTester = NotificationHandlerTester {
		SoneInsertHandler(it) { sone ->
			if (sone == localSone) notification1 else notification2
		}
	}

	@Test
	fun `handler adds notification to manager when sone insert starts`() {
		localSone.options.isSoneInsertNotificationEnabled = true
		soneInsertHandlerTester.sendEvent(SoneInsertingEvent(localSone))
		assertThat(soneInsertHandlerTester.notifications, hasItem(notification1))
	}

	@Test
	fun `handler sets sone status in notification when sone insert starts`() {
		localSone.options.isSoneInsertNotificationEnabled = true
		soneInsertHandlerTester.sendEvent(SoneInsertingEvent(localSone))
		assertThat(notification1.get("soneStatus"), equalTo<Any>("inserting"))
	}

	@Test
	fun `handler does not add notification to manager if option is disabled`() {
		localSone.options.isSoneInsertNotificationEnabled = false
		soneInsertHandlerTester.sendEvent(SoneInsertingEvent(localSone))
		assertThat(soneInsertHandlerTester.notifications, not(hasItem(notification1)))
	}

	@Test
	fun `handler adds notification to manager when sone insert finishes`() {
		localSone.options.isSoneInsertNotificationEnabled = true
		soneInsertHandlerTester.sendEvent(SoneInsertedEvent(localSone, 123456, ""))
		assertThat(soneInsertHandlerTester.notifications, hasItem(notification1))
	}

	@Test
	fun `handler sets sone status in notification when sone insert finishes`() {
		localSone.options.isSoneInsertNotificationEnabled = true
		soneInsertHandlerTester.sendEvent(SoneInsertedEvent(localSone, 123456, ""))
		assertThat(notification1.get("soneStatus"), equalTo<Any>("inserted"))
	}

	@Test
	fun `handler sets insert duration in notification when sone insert finishes`() {
		localSone.options.isSoneInsertNotificationEnabled = true
		soneInsertHandlerTester.sendEvent(SoneInsertedEvent(localSone, 123456, ""))
		assertThat(notification1.get("insertDuration"), equalTo<Any>(123L))
	}

	@Test
	fun `handler does not add notification for finished insert to manager if option is disabled`() {
		localSone.options.isSoneInsertNotificationEnabled = false
		soneInsertHandlerTester.sendEvent(SoneInsertedEvent(localSone, 123456, ""))
		assertThat(soneInsertHandlerTester.notifications, not(hasItem(notification1)))
	}

	@Test
	fun `handler adds notification to manager when sone insert aborts`() {
		localSone.options.isSoneInsertNotificationEnabled = true
		soneInsertHandlerTester.sendEvent(SoneInsertAbortedEvent(localSone, Exception()))
		assertThat(soneInsertHandlerTester.notifications, hasItem(notification1))
	}

	@Test
	fun `handler sets sone status in notification when sone insert aborts`() {
		localSone.options.isSoneInsertNotificationEnabled = true
		soneInsertHandlerTester.sendEvent(SoneInsertAbortedEvent(localSone, Exception()))
		assertThat(notification1.get("soneStatus"), equalTo<Any>("insert-aborted"))
	}

	@Test
	fun `handler does not add notification for aborted insert to manager if option is disabled`() {
		localSone.options.isSoneInsertNotificationEnabled = false
		soneInsertHandlerTester.sendEvent(SoneInsertAbortedEvent(localSone, Exception()))
		assertThat(soneInsertHandlerTester.notifications, not(hasItem(notification1)))
	}

}
