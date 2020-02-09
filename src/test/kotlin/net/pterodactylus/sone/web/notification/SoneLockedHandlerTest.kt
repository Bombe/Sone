/**
 * Sone - SoneLockedHandlerTest.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import java.util.concurrent.*
import kotlin.test.*

/**
 * Unit test for [SoneLockedHandler].
 */
@Suppress("UnstableApiUsage")
class SoneLockedHandlerTest {

	private val eventBus = EventBus()
	private val notificationManager = NotificationManager()
	private val notification = ListNotification<Sone>("", "", Template())
	private val executor = TestScheduledThreadPoolExecutor()

	init {
		SoneLockedHandler(notificationManager, notification, executor).also(eventBus::register)
	}

	@AfterTest
	fun shutdownExecutor() = executor.shutdown()

	@Test
	fun `notification is not added before the command is run`() {
		eventBus.post(SoneLockedEvent(sone))
		assertThat(notificationManager.notifications, emptyIterable())
	}

	@Test
	fun `sone is added to notification immediately`() {
		eventBus.post(SoneLockedEvent(sone))
		assertThat(notification.elements, contains(sone))
	}

	@Test
	fun `notification is added to notification manager from command`() {
		eventBus.post(SoneLockedEvent(sone))
		executor.scheduleds.single().command()
		assertThat(notificationManager.notifications, contains<Any>(notification))
	}

	@Test
	fun `command is registered with a delay of five minutes`() {
		eventBus.post(SoneLockedEvent(sone))
		with(executor.scheduleds.single()) {
			assertThat(timeUnit.toNanos(delay), equalTo(TimeUnit.MINUTES.toNanos(5)))
		}
	}

	@Test
	fun `unlocking sone after locking will cancel the future`() {
		eventBus.post(SoneLockedEvent(sone))
		eventBus.post(SoneUnlockedEvent(sone))
		assertThat(executor.scheduleds.first().future.isCancelled, equalTo(true))
	}

	@Test
	fun `unlocking sone after locking will remove the sone from the notification`() {
		eventBus.post(SoneLockedEvent(sone))
		eventBus.post(SoneUnlockedEvent(sone))
		assertThat(notification.elements, emptyIterable())
	}

	@Test
	fun `unlocking sone after showing the notification will remove the sone from the notification`() {
		eventBus.post(SoneLockedEvent(sone))
		executor.scheduleds.single().command()
		eventBus.post(SoneUnlockedEvent(sone))
		assertThat(notification.elements, emptyIterable())
	}

	@Test
	fun `locking two sones will cancel the first command`() {
		eventBus.post(SoneLockedEvent(sone))
		eventBus.post(SoneLockedEvent(sone))
		assertThat(executor.scheduleds.first().future.isCancelled, equalTo(true))
	}

	@Test
	fun `locking two sones will schedule a second command`() {
		eventBus.post(SoneLockedEvent(sone))
		eventBus.post(SoneLockedEvent(sone))
		assertThat(executor.scheduleds[1], notNullValue())
	}

}

private val sone: Sone = IdOnlySone("sone")
