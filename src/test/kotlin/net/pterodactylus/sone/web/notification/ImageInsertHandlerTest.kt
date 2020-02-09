/**
 * Sone - ImageInsertHandler.kt - Copyright © 2019 David ‘Bombe’ Roden
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
import freenet.keys.FreenetURI.*
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
 * Unit test for [ImageInsertHandler].
 */
@Suppress("UnstableApiUsage")
class ImageInsertHandlerTest {

	private val eventBus = EventBus()
	private val notificationManager = NotificationManager()
	private val imageInsertingNotification = ListNotification<Image>("", "", Template())
	private val imageFailedNotification = ListNotification<Image>("", "", Template())
	private val imageInsertedNotification = ListNotification<Image>("", "", Template())

	init {
		eventBus.register(ImageInsertHandler(notificationManager, imageInsertingNotification, imageFailedNotification, imageInsertedNotification))
	}

	@Test
	fun `handler adds notification when image insert starts`() {
		eventBus.post(ImageInsertStartedEvent(image))
		assertThat(notificationManager.notifications, contains<Notification>(imageInsertingNotification))
	}

	@Test
	fun `handler adds image to notification when image insert starts`() {
		eventBus.post(ImageInsertStartedEvent(image))
		assertThat(imageInsertingNotification.elements, contains(image))
	}

	@Test
	fun `handler removes image from inserting notification when insert is aborted`() {
		eventBus.post(ImageInsertStartedEvent(image))
		eventBus.post(ImageInsertAbortedEvent(image))
		assertThat(imageInsertingNotification.elements, emptyIterable())
	}

	@Test
	fun `handler removes image from inserting notification when insert fails`() {
		eventBus.post(ImageInsertStartedEvent(image))
		eventBus.post(ImageInsertFailedEvent(image, Throwable()))
		assertThat(imageInsertingNotification.elements, emptyIterable())
	}

	@Test
	fun `handler adds image to insert-failed notification when insert fails`() {
		eventBus.post(ImageInsertFailedEvent(image, Throwable()))
		assertThat(imageFailedNotification.elements, contains(image))
	}

	@Test
	fun `handler adds insert-failed notification to manager when insert fails`() {
		eventBus.post(ImageInsertFailedEvent(image, Throwable()))
		assertThat(notificationManager.notifications, contains<Notification>(imageFailedNotification))
	}

	@Test
	fun `handler removes image from inserting notification when insert succeeds`() {
		eventBus.post(ImageInsertStartedEvent(image))
		eventBus.post(ImageInsertFinishedEvent(image, EMPTY_CHK_URI))
		assertThat(imageInsertingNotification.elements, emptyIterable())
	}

	@Test
	fun `handler adds image to inserted notification when insert succeeds`() {
		eventBus.post(ImageInsertFinishedEvent(image, EMPTY_CHK_URI))
		assertThat(imageInsertedNotification.elements, contains(image))
	}

	@Test
	fun `handler adds inserted notification to manager when insert succeeds`() {
		eventBus.post(ImageInsertFinishedEvent(image, EMPTY_CHK_URI))
		assertThat(notificationManager.notifications, contains<Notification>(imageInsertedNotification))
	}

}

private val image: Image = ImageImpl()
