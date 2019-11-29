/**
 * Sone - SoneLockedOnStartupNotificationTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.notify.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.notify.*
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
	private val notification by lazy { manager.notifications.single() as ListNotification<*> }

	init {
		SoneLockedOnStartupHandler(manager, template).also(eventBus::register)
		eventBus.post(SoneLockedOnStartup(sone))
	}

	@Test
	fun `notification has correct id`() {
		assertThat(notification.id, equalTo("sone-locked-on-startup"))
	}

	@Test
	fun `handler adds sone to notification when event is posted`() {
		assertThat(notification.elements, contains<Any>(sone))
	}

	@Test
	fun `handler creates notification with correct key`() {
		assertThat(notification.render(), equalTo(listOf(sone).toString()))
	}

}

private val sone = IdOnlySone("sone-id")
private val template = "<% sones>".asTemplate()
