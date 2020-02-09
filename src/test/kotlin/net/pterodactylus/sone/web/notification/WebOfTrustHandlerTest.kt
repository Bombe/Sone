/**
 * Sone - WebOfTrustHandlerTest.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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
import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for [WebOfTrustHandler].
 */
class WebOfTrustHandlerTest {

	private val eventBus = EventBus()
	private val notificationManager = NotificationManager()
	private val notification = TemplateNotification("", Template())

	init {
		eventBus.register(WebOfTrustHandler(notificationManager, notification))
	}

	@Test
	fun `handler adds notification if wot goes down`() {
		eventBus.post(WebOfTrustDisappeared())
		assertThat(notificationManager.notifications, contains<Notification>(notification))
	}

	@Test
	fun `handler removes notification if wot appears`() {
		notificationManager.addNotification(notification)
		eventBus.post(WebOfTrustAppeared())
		assertThat(notificationManager.notifications, emptyIterable())
	}

}
