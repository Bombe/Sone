/**
 * Sone - FirstStartHandlerTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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
 * Unit test for [FirstStartHandler].
 */
@Suppress("UnstableApiUsage")
class FirstStartHandlerTest {

	private val eventBus = EventBus()
	private val notificationManager = NotificationManager()
	private val notification = TemplateNotification(Template())

	init {
		eventBus.register(FirstStartHandler(notificationManager, notification))
	}

	@Test
	fun `handler can be created`() {
		FirstStartHandler(notificationManager, notification)
	}

	@Test
	fun `handler adds notification to manager on first start event`() {
		eventBus.post(FirstStart())
		assertThat(notificationManager.notifications, contains<Notification>(notification))
	}

}
