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
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.notify.*
import net.pterodactylus.util.notify.*
import javax.inject.*

/**
 * Notification handler for the various image-insert-related events.
 *
 * @see ImageInsertStartedEvent
 * @see ImageInsertAbortedEvent
 * @see ImageInsertFailedEvent
 * @see ImageInsertFinishedEvent
 */
class ImageInsertHandler @Inject constructor(
		private val notificationManager: NotificationManager,
		@Named("imageInserting") private val imageInsertingNotification: ListNotification<Image>,
		@Named("imageFailed") private val imageFailedNotification: ListNotification<Image>,
		@Named("imageInserted") private val imageInsertedNotification: ListNotification<Image>) {

	@Subscribe
	fun imageInsertStarted(imageInsertStartedEvent: ImageInsertStartedEvent) {
		imageInsertingNotification.add(imageInsertStartedEvent.image)
		notificationManager.addNotification(imageInsertingNotification)
	}

	@Subscribe
	fun imageInsertAborted(imageInsertAbortedEvent: ImageInsertAbortedEvent) {
		imageInsertingNotification.remove(imageInsertAbortedEvent.image)
	}

	@Subscribe
	fun imageInsertFailed(imageInsertFailedEvent: ImageInsertFailedEvent) {
		imageInsertingNotification.remove(imageInsertFailedEvent.image)
		imageFailedNotification.add(imageInsertFailedEvent.image)
		notificationManager.addNotification(imageFailedNotification)
	}

	@Subscribe
	fun imageInsertFinished(imageInsertFinishedEvent: ImageInsertFinishedEvent) {
		imageInsertingNotification.remove(imageInsertFinishedEvent.image)
		imageInsertedNotification.add(imageInsertFinishedEvent.image)
		notificationManager.addNotification(imageInsertedNotification)
	}

}
