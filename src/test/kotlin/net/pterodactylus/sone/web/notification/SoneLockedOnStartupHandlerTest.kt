/**
 * Sone - SoneLockedOnStartupHandlerTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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
 * Unit test for [SoneLockedOnStartupHandler].
 */
class SoneLockedOnStartupHandlerTest {

	@Suppress("UnstableApiUsage")
	private val eventBus = EventBus()
	private val manager = NotificationManager()
	private val notification = ListNotification<Sone>("", "", Template())

	init {
		SoneLockedOnStartupHandler(manager, notification).also(eventBus::register)
	}

	@Test
	fun `handler adds sone to notification when event is posted`() {
		eventBus.post(SoneLockedOnStartup(sone))
		assertThat(notification.elements, contains<Any>(sone))
	}

	@Test
	fun `handler adds notification to manager`() {
		eventBus.post(SoneLockedOnStartup(sone))
		assertThat(manager.notifications, contains<Notification>(notification))
	}

}

private val sone = IdOnlySone("sone-id")
