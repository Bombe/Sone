/**
 * Sone - NewSoneHandlerTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.notify.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import java.io.*
import kotlin.test.*

class NewSoneHandlerTest {

	@Suppress("UnstableApiUsage")
	private val eventBus = EventBus()
	private val notificationManager = NotificationManager()
	private val handler = NewSoneHandler(notificationManager, "<% sones>".asTemplate())

	init {
		eventBus.register(handler)
	}

	@Test
	fun `handler adds notification if new sone event is fired`() {
		eventBus.post(NewSoneFoundEvent(sone))
		val notification = notificationManager.notifications.single() as ListNotification<*>
		assertThat(notification.id, equalTo("new-sone-notification"))
	}

	@Test
	fun `handler adds sone to notification`() {
		eventBus.post(NewSoneFoundEvent(sone))
		val notification = notificationManager.notifications.single() as ListNotification<*>
		assertThat(notification.elements, contains<Any>(sone))
	}

	@Test
	fun `handler sets correct key for sones`() {
		eventBus.post(NewSoneFoundEvent(sone))
		val notification = notificationManager.notifications.single() as ListNotification<*>
		assertThat(notification.render(), equalTo(listOf(sone).toString()))
	}

	@Test
	fun `handler creates non-dismissable notification`() {
		eventBus.post(NewSoneFoundEvent(sone))
		val notification = notificationManager.notifications.single() as ListNotification<*>
		assertThat(notification, matches("is not dismissable") { !it.isDismissable })
	}

	@Test
	fun `handler does not add notification on new sone event if first-start notification is present`() {
		notificationManager.addNotification(object : AbstractNotification("first-start-notification") {
			override fun render(writer: Writer) = Unit
		})
		eventBus.post(NewSoneFoundEvent(sone))
		val notification = notificationManager.notifications.single()
		assertThat(notification.id, equalTo("first-start-notification"))
	}

	@Test
	fun `handler removes sone from notification if sone is marked as known`() {
		eventBus.post(NewSoneFoundEvent(sone))
		val notification = notificationManager.notifications.single() as ListNotification<*>
		eventBus.post(MarkSoneKnownEvent(sone))
		assertThat(notification.elements, emptyIterable())
	}

	@Test
	fun `handler removes sone from notification if sone is removed`() {
		eventBus.post(NewSoneFoundEvent(sone))
		val notification = notificationManager.notifications.single() as ListNotification<*>
		eventBus.post(SoneRemovedEvent(sone))
		assertThat(notification.elements, emptyIterable())
	}

}

private val sone = IdOnlySone("sone-id")
