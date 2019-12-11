/**
 * Sone - NewVersionHandlerTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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
import net.pterodactylus.util.version.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for [NewVersionHandler].
 */
@Suppress("UnstableApiUsage")
class NewVersionHandlerTest {

	private val eventBus = EventBus()
	private val notificationManager = NotificationManager()
	private val notification = TemplateNotification(Template())

	init {
		eventBus.register(NewVersionHandler(notificationManager, notification))
		eventBus.post(UpdateFoundEvent(Version(1, 2, 3), 1000L, 2000L, true))
	}

	@Test
	fun `new-version handler adds notification to manager on new version`() {
		assertThat(notificationManager.notifications, contains<Notification>(notification))
	}

	@Test
	fun `handler sets version in notification`() {
		assertThat(notification.get("latestVersion"), equalTo<Any>(Version(1, 2, 3)))
	}

	@Test
	fun `handler sets release time in notification`() {
		assertThat(notification.get("releaseTime"), equalTo<Any>(1000L))
	}

	@Test
	fun `handler sets edition in notification`() {
		assertThat(notification.get("latestEdition"), equalTo<Any>(2000L))
	}

	@Test
	fun `handler sets disruptive flag in notification`() {
		assertThat(notification.get("disruptive"), equalTo<Any>(true))
	}

}
