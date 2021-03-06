/**
 * Sone - NotificationHandlerModule.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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
import com.google.inject.binder.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.notify.*
import net.pterodactylus.sone.text.*
import net.pterodactylus.util.notify.*
import java.util.concurrent.*
import java.util.concurrent.TimeUnit.*
import java.util.function.*
import javax.inject.*
import javax.inject.Singleton

/**
 * Guice module for creating all notification handlers.
 */
class NotificationHandlerModule : AbstractModule() {

	override fun configure() {
		bind(NotificationHandler::class.java).`in`(Singleton::class.java)
		bind<MarkPostKnownDuringFirstStartHandler>().asSingleton()
		bind<MarkPostReplyKnownDuringFirstStartHandler>().asSingleton()
		bind<SoneLockedOnStartupHandler>().asSingleton()
		bind<NewSoneHandler>().asSingleton()
		bind<NewRemotePostHandler>().asSingleton()
		bind<RemotePostReplyHandler>().asSingleton()
		bind<SoneLockedHandler>().asSingleton()
		bind<LocalPostHandler>().asSingleton()
		bind<LocalReplyHandler>().asSingleton()
		bind<NewVersionHandler>().asSingleton()
		bind<ImageInsertHandler>().asSingleton()
		bind<FirstStartHandler>().asSingleton()
		bind<ConfigNotReadHandler>().asSingleton()
		bind<StartupHandler>().asSingleton()
		bind<WebOfTrustHandler>().asSingleton()
		bind<SoneMentionDetector>().asSingleton()
		bind<SoneMentionedHandler>().asSingleton()
		bind<SoneInsertHandler>().asSingleton()
	}

	@Provides
	fun getMarkPostKnownHandler(core: Core): Consumer<Post> = Consumer { core.markPostKnown(it) }

	@Provides
	fun getMarkPostReplyKnownHandler(core: Core): Consumer<PostReply> = Consumer { core.markReplyKnown(it) }

	@Provides
	@Singleton
	@Named("soneLockedOnStartup")
	fun getSoneLockedOnStartupNotification(loaders: Loaders) =
			ListNotification<Sone>("sone-locked-on-startup", "sones", loaders.loadTemplate("/templates/notify/soneLockedOnStartupNotification.html"))

	@Provides
	@Named("newSone")
	fun getNewSoneNotification(loaders: Loaders) =
			ListNotification<Sone>("new-sone-notification", "sones", loaders.loadTemplate("/templates/notify/newSoneNotification.html"), dismissable = false)

	@Provides
	@Singleton
	@Named("newRemotePost")
	fun getNewPostNotification(loaders: Loaders) =
			ListNotification<Post>("new-post-notification", "posts", loaders.loadTemplate("/templates/notify/newPostNotification.html"), dismissable = false)

	@Provides
	@Singleton
	@Named("newRemotePostReply")
	fun getNewRemotePostReplyNotification(loaders: Loaders) =
			ListNotification<PostReply>("new-reply-notification", "replies", loaders.loadTemplate("/templates/notify/newReplyNotification.html"), dismissable = false)

	@Provides
	@Singleton
	@Named("soneLocked")
	fun getSoneLockedNotification(loaders: Loaders) =
			ListNotification<Sone>("sones-locked-notification", "sones", loaders.loadTemplate("/templates/notify/lockedSonesNotification.html"), dismissable = true)

	@Provides
	@Singleton
	@Named("localPost")
	fun getLocalPostNotification(loaders: Loaders) =
			ListNotification<Post>("local-post-notification", "posts", loaders.loadTemplate("/templates/notify/newPostNotification.html"), dismissable = false)

	@Provides
	@Singleton
	@Named("localReply")
	fun getLocalReplyNotification(loaders: Loaders) =
			ListNotification<PostReply>("local-reply-notification", "replies", loaders.loadTemplate("/templates/notify/newReplyNotification.html"), dismissable = false)

	@Provides
	@Singleton
	@Named("newVersion")
	fun getNewVersionNotification(loaders: Loaders) =
			TemplateNotification("new-version-notification", loaders.loadTemplate("/templates/notify/newVersionNotification.html"))

	@Provides
	@Singleton
	@Named("imageInserting")
	fun getImageInsertingNotification(loaders: Loaders) =
			ListNotification<Image>("inserting-images-notification", "images", loaders.loadTemplate("/templates/notify/inserting-images-notification.html"), dismissable = true)

	@Provides
	@Singleton
	@Named("imageFailed")
	fun getImageInsertingFailedNotification(loaders: Loaders) =
			ListNotification<Image>("image-insert-failed-notification", "images", loaders.loadTemplate("/templates/notify/image-insert-failed-notification.html"), dismissable = true)

	@Provides
	@Singleton
	@Named("imageInserted")
	fun getImageInsertedNotification(loaders: Loaders) =
			ListNotification<Image>("inserted-images-notification", "images", loaders.loadTemplate("/templates/notify/inserted-images-notification.html"), dismissable = true)

	@Provides
	@Singleton
	@Named("firstStart")
	fun getFirstStartNotification(loaders: Loaders) =
			TemplateNotification("first-start-notification", loaders.loadTemplate("/templates/notify/firstStartNotification.html"))

	@Provides
	@Singleton
	@Named("configNotRead")
	fun getConfigNotReadNotification(loaders: Loaders) =
			TemplateNotification("config-not-read-notification", loaders.loadTemplate("/templates/notify/configNotReadNotification.html"))

	@Provides
	@Singleton
	@Named("startup")
	fun getStartupNotification(loaders: Loaders) =
			TemplateNotification("startup-notification", loaders.loadTemplate("/templates/notify/startupNotification.html"))

	@Provides
	@Singleton
	@Named("webOfTrust")
	fun getWebOfTrustNotification(loaders: Loaders) =
			TemplateNotification("wot-missing-notification", loaders.loadTemplate("/templates/notify/wotMissingNotification.html"))

	@Provides
	@Singleton
	@Named("webOfTrustReacher")
	fun getWebOfTrustReacher(webOfTrustConnector: WebOfTrustConnector): Runnable =
			Runnable { webOfTrustConnector.ping() }

	@Provides
	@Singleton
	@Named("webOfTrustReschedule")
	fun getWebOfTrustReschedule(@Named("notification") ticker: ScheduledExecutorService) =
			Consumer<Runnable> { ticker.schedule(it, 15, SECONDS) }

	@Provides
	@Singleton
	@Named("soneMentioned")
	fun getSoneMentionedNotification(loaders: Loaders) =
			ListNotification<Post>("mention-notification", "posts", loaders.loadTemplate("/templates/notify/mentionNotification.html"), dismissable = false)

	@Provides
	@Singleton
	fun getSoneNotificationSupplier(loaders: Loaders): SoneInsertNotificationSupplier =
			mutableMapOf<Sone, TemplateNotification>()
					.let { cache ->
						{ sone ->
							cache.computeIfAbsent(sone) {
								loaders.loadTemplate("/templates/notify/soneInsertNotification.html")
										.let(::TemplateNotification)
										.also { it["insertSone"] = sone }
							}
						}
					}

	private inline fun <reified T> bind(): AnnotatedBindingBuilder<T> = bind(T::class.java)
	private fun ScopedBindingBuilder.asSingleton() = `in`(Singleton::class.java)

}
