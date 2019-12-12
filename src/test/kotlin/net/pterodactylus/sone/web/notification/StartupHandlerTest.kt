/**
 * Sone - StartupHandlerTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import java.util.concurrent.TimeUnit.*
import kotlin.test.*

/**
 * Unit test for [StartupHandler].
 */
class StartupHandlerTest {

	private val eventBus = EventBus()
	private val notificationManager = NotificationManager()
	private val notification = TemplateNotification("", Template())
	private val executor = TestScheduledThreadPoolExecutor()

	init {
		eventBus.register(StartupHandler(notificationManager, notification, executor))
	}

	@AfterTest
	fun shutdownExecutor() = executor.shutdown()

	@Test
	fun `handler adds notification to manager on startup`() {
		eventBus.post(Startup())
		assertThat(notificationManager.notifications, contains<Notification>(notification))
	}

	@Test
	fun `handler registers command on with 2-minute delay`() {
		eventBus.post(Startup())
		assertThat(with(executor.scheduleds.single()) { timeUnit.toNanos(delay) }, equalTo(MINUTES.toNanos(2)))
	}

	@Test
	fun `registered command removes notification from manager`() {
		eventBus.post(Startup())
		executor.scheduleds.single().command()
		assertThat(notificationManager.notifications, emptyIterable())
	}

}
