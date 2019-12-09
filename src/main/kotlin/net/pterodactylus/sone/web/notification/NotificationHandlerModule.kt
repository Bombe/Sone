/**
 * Sone - NotificationHandlerModuleTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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

import com.google.inject.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.notify.*
import net.pterodactylus.util.notify.*
import javax.inject.*
import javax.inject.Singleton

/**
 * Guice module for creating all notification handlers.
 */
class NotificationHandlerModule : AbstractModule() {

	override fun configure() {
		bind(NotificationHandler::class.java).`in`(Singleton::class.java)
	}

	@Provides
	@Singleton
	fun getMarkPostKnownDuringFirstStartHandler(core: Core, notificationManager: NotificationManager) =
			MarkPostKnownDuringFirstStartHandler(notificationManager, core::markPostKnown)

	@Provides
	@Singleton
	fun getSoneLockedOnStartupHandler(notificationManager: NotificationManager, @Named("soneLockedOnStartup") notification: ListNotification<Sone>) =
			SoneLockedOnStartupHandler(notificationManager, notification)

	@Provides
	@Singleton
	@Named("soneLockedOnStartup")
	fun getSoneLockedOnStartupNotification(loaders: Loaders) =
			ListNotification<Sone>("sone-locked-on-startup", "sones", loaders.loadTemplate("/templates/notify/soneLockedOnStartupNotification.html"))

	@Provides
	@Singleton
	fun getNewSoneHandler(notificationManager: NotificationManager, @Named("newSone") notification: ListNotification<Sone>) =
			NewSoneHandler(notificationManager, notification)

	@Provides
	@Named("newSone")
	fun getNewSoneNotification(loaders: Loaders) =
			ListNotification<Sone>("new-sone-notification", "sones", loaders.loadTemplate("/templates/notify/newSoneNotification.html"), dismissable = false)

	@Provides
	@Singleton
	fun getNewRemotePostHandler(notificationManager: NotificationManager, @Named("newRemotePost") newPostNotification: ListNotification<Post>) =
			NewRemotePostHandler(notificationManager, newPostNotification)

	@Provides
	@Singleton
	@Named("newRemotePost")
	fun getNewPostNotification(loaders: Loaders) =
			ListNotification<Post>("new-post-notification", "posts", loaders.loadTemplate("/templates/notify/newPostNotification.html"), dismissable = false)

}
