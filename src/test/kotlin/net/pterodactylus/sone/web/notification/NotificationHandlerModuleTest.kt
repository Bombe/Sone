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
import com.google.inject.Guice.*
import com.google.inject.name.Names.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.Post.*
import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.notify.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.text.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.notify.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.mockito.*
import org.mockito.Mockito.*
import java.util.concurrent.*
import java.util.concurrent.TimeUnit.*
import java.util.function.*
import kotlin.test.*

/**
 * Unit test for [NotificationHandlerModule].
 */
class NotificationHandlerModuleTest {

	private val core = mock<Core>()
	private val webOfTrustConnector = mock<WebOfTrustConnector>()
	private val ticker = mock<ScheduledExecutorService>()
	private val notificationManager = NotificationManager()
	private val loaders = TestLoaders()
	private val injector: Injector = createInjector(
			Core::class.isProvidedBy(core),
			NotificationManager::class.isProvidedBy(notificationManager),
			Loaders::class.isProvidedBy(loaders),
			WebOfTrustConnector::class.isProvidedBy(webOfTrustConnector),
			ScheduledExecutorService::class.withNameIsProvidedBy(ticker, "notification"),
			SoneTextParser::class.isProvidedByMock(),
			PostReplyProvider::class.isProvidedByMock(),
			NotificationHandlerModule()
	)

	@Test
	fun `notification handler is created as singleton`() {
		injector.verifySingletonInstance<NotificationHandler>()
	}

	@Test
	fun `mark-post-known-during-first-start handler is created as singleton`() {
		injector.verifySingletonInstance<MarkPostKnownDuringFirstStartHandler>()
	}

	@Test
	fun `mark-post-known-during-first-start handler is created with correct action`() {
		notificationManager.firstStart()
		val handler = injector.getInstance<MarkPostKnownDuringFirstStartHandler>()
		val post = mock<Post>()
		handler.newPostFound(NewPostFoundEvent(post))
		verify(core).markPostKnown(post)
	}

	@Test
	fun `sone-locked-on-startup handler is created as singleton`() {
		injector.verifySingletonInstance<SoneLockedOnStartupHandler>()
	}

	@Test
	fun `module can create sone-locked-on-startup notification with correct id`() {
		val notification = injector.getInstance<ListNotification<Sone>>(named("soneLockedOnStartup"))
		assertThat(notification.id, equalTo("sone-locked-on-startup"))
	}

	@Test
	fun `sone-locked-on-startup notification is created as singleton`() {
		injector.verifySingletonInstance<ListNotification<Sone>>(named("soneLockedOnStartup"))
	}

	@Test
	fun `module can create sone-locked-on-startup notification with correct template and key`() {
		loaders.templates += "/templates/notify/soneLockedOnStartupNotification.html" to "<% sones>".asTemplate()
		val notification = injector.getInstance<ListNotification<Sone>>(named("soneLockedOnStartup"))
		val sone1 = IdOnlySone("sone1")
		val sone2 = IdOnlySone("sone2")
		notification.add(sone1)
		notification.add(sone2)
		assertThat(notification.render(), equalTo(listOf(sone1, sone2).toString()))
	}

	@Test
	fun `sone-locked-on-startup notification is dismissable`() {
		assertThat(injector.getInstance<ListNotification<Sone>>(named("soneLockedOnStartup")).isDismissable, equalTo(true))
	}

	@Test
	fun `new-sone handler is created as singleton`() {
		injector.verifySingletonInstance<NewSoneHandler>()
	}

	@Test
	fun `new-sone notification has correct ID`() {
		assertThat(injector.getInstance<ListNotification<Sone>>(named("newSone")).id, equalTo("new-sone-notification"))
	}

	@Test
	fun `new-sone notification has correct key and template`() {
		loaders.templates += "/templates/notify/newSoneNotification.html" to "<% sones>".asTemplate()
		val notification = injector.getInstance<ListNotification<Sone>>(named("newSone"))
		val sones = listOf(IdOnlySone("sone1"), IdOnlySone("sone2"))
		sones.forEach(notification::add)
		assertThat(notification.render(), equalTo(sones.toString()))
	}

	@Test
	fun `new-sone notification is not dismissable`() {
		assertThat(injector.getInstance<ListNotification<Sone>>(named("newSone")).isDismissable, equalTo(false))
	}

	@Test
	fun `new-remote-post handler is created as singleton`() {
		injector.verifySingletonInstance<NewRemotePostHandler>()
	}

	@Test
	fun `new-remote-post notification is created as singleton`() {
		injector.verifySingletonInstance<ListNotification<Post>>(named("newRemotePost"))
	}

	@Test
	fun `new-remote-post notification has correct ID`() {
		assertThat(injector.getInstance<ListNotification<Post>>(named("newRemotePost")).id, equalTo("new-post-notification"))
	}

	@Test
	fun `new-remote-post notification is not dismissable`() {
		assertThat(injector.getInstance<ListNotification<Post>>(named("newRemotePost")).isDismissable, equalTo(false))
	}

	@Test
	fun `new-remote-post notification has correct key and template`() {
		loaders.templates += "/templates/notify/newPostNotification.html" to "<% posts>".asTemplate()
		val notification = injector.getInstance<ListNotification<Post>>(named("newRemotePost"))
		val posts = listOf(EmptyPost("post1"), EmptyPost("post2"))
		posts.forEach(notification::add)
		assertThat(notification.render(), equalTo(posts.toString()))
	}

	@Test
	fun `sone-locked notification is created as singleton`() {
		injector.verifySingletonInstance<ListNotification<Sone>>(named("soneLocked"))
	}

	@Test
	fun `sone-locked notification is dismissable`() {
		assertThat(injector.getInstance<ListNotification<Sone>>(named("soneLocked")).isDismissable, equalTo(true))
	}

	@Test
	fun `sone-locked notification has correct ID`() {
		assertThat(injector.getInstance<ListNotification<Sone>>(named("soneLocked")).id, equalTo("sones-locked-notification"))
	}

	@Test
	fun `sone-locked notification has correct key and template`() {
		loaders.templates += "/templates/notify/lockedSonesNotification.html" to "<% sones>".asTemplate()
		val notification = injector.getInstance<ListNotification<Sone>>(named("soneLocked"))
		val sones = listOf(IdOnlySone("sone1"), IdOnlySone("sone2"))
		sones.forEach(notification::add)
		assertThat(notification.render(), equalTo(sones.toString()))
	}

	@Test
	fun `sone-locked handler is created as singleton`() {
		injector.verifySingletonInstance<SoneLockedHandler>()
	}

	@Test
	fun `local-post notification is not dismissable`() {
		assertThat(injector.getInstance<ListNotification<Post>>(named("localPost")).isDismissable, equalTo(false))
	}

	@Test
	fun `local-post notification has correct ID`() {
		assertThat(injector.getInstance<ListNotification<Post>>(named("localPost")).id, equalTo("local-post-notification"))
	}

	@Test
	fun `local-post notification has correct key and template`() {
		loaders.templates += "/templates/notify/newPostNotification.html" to "<% posts>".asTemplate()
		val notification = injector.getInstance<ListNotification<Post>>(named("localPost"))
		val posts = listOf(EmptyPost("post1"), EmptyPost("post2"))
		posts.forEach(notification::add)
		assertThat(notification.render(), equalTo(posts.toString()))
	}

	@Test
	fun `local-post notification is created as singleton`() {
		injector.verifySingletonInstance<ListNotification<Post>>(named("localPost"))
	}

	@Test
	fun `local-post handler is created as singleton`() {
		injector.verifySingletonInstance<LocalPostHandler>()
	}

	@Test
	fun `new-version notification is created as singleton`() {
		injector.verifySingletonInstance<TemplateNotification>(named("newVersion"))
	}

	@Test
	fun `new-version notification has correct ID`() {
		assertThat(injector.getInstance<TemplateNotification>(named("newVersion")).id, equalTo("new-version-notification"))
	}

	@Test
	fun `new-version notification is dismissable`() {
		assertThat(injector.getInstance<TemplateNotification>(named("newVersion")).isDismissable, equalTo(true))
	}

	@Test
	fun `new-version notification loads correct template`() {
		loaders.templates += "/templates/notify/newVersionNotification.html" to "1".asTemplate()
		val notification = injector.getInstance<TemplateNotification>(named("newVersion"))
		assertThat(notification.render(), equalTo("1"))
	}

	@Test
	fun `new-version handler is created as singleton`() {
		injector.verifySingletonInstance<NewVersionHandler>()
	}

	@Test
	fun `inserting-image notification is created as singleton`() {
		injector.verifySingletonInstance<ListNotification<Image>>(named("imageInserting"))
	}

	@Test
	fun `inserting-image notification has correct ID`() {
		assertThat(injector.getInstance<ListNotification<Image>>(named("imageInserting")).id, equalTo("inserting-images-notification"))
	}

	@Test
	fun `inserting-image notification is dismissable`() {
		assertThat(injector.getInstance<ListNotification<Image>>(named("imageInserting")).isDismissable, equalTo(true))
	}

	@Test
	fun `inserting-image notification loads correct template`() {
		loaders.templates += "/templates/notify/inserting-images-notification.html" to "<% images>".asTemplate()
		val notification = injector.getInstance<ListNotification<Image>>(named("imageInserting"))
		val images = listOf(ImageImpl(), ImageImpl()).onEach(notification::add)
		assertThat(notification.render(), equalTo(images.toString()))
	}

	@Test
	fun `inserting-image-failed notification is created as singleton`() {
		injector.verifySingletonInstance<ListNotification<Image>>(named("imageFailed"))
	}

	@Test
	fun `inserting-image-failed notification has correct ID`() {
		assertThat(injector.getInstance<ListNotification<Image>>(named("imageFailed")).id, equalTo("image-insert-failed-notification"))
	}

	@Test
	fun `inserting-image-failed notification is dismissable`() {
		assertThat(injector.getInstance<ListNotification<Image>>(named("imageFailed")).isDismissable, equalTo(true))
	}

	@Test
	fun `inserting-image-failed notification loads correct template`() {
		loaders.templates += "/templates/notify/image-insert-failed-notification.html" to "<% images>".asTemplate()
		val notification = injector.getInstance<ListNotification<Image>>(named("imageFailed"))
		val images = listOf(ImageImpl(), ImageImpl()).onEach(notification::add)
		assertThat(notification.render(), equalTo(images.toString()))
	}

	@Test
	fun `inserted-image notification is created as singleton`() {
		injector.verifySingletonInstance<ListNotification<Image>>(named("imageInserted"))
	}

	@Test
	fun `inserted-image notification has correct ID`() {
		assertThat(injector.getInstance<ListNotification<Image>>(named("imageInserted")).id, equalTo("inserted-images-notification"))
	}

	@Test
	fun `inserted-image notification is dismissable`() {
		assertThat(injector.getInstance<ListNotification<Image>>(named("imageInserted")).isDismissable, equalTo(true))
	}

	@Test
	fun `inserted-image notification loads correct template`() {
		loaders.templates += "/templates/notify/inserted-images-notification.html" to "<% images>".asTemplate()
		val notification = injector.getInstance<ListNotification<Image>>(named("imageInserted"))
		val images = listOf(ImageImpl(), ImageImpl()).onEach(notification::add)
		assertThat(notification.render(), equalTo(images.toString()))
	}

	@Test
	fun `image insert handler is created as singleton`() {
		injector.verifySingletonInstance<ImageInsertHandler>()
	}

	@Test
	fun `first-start notification is created as singleton`() {
		injector.verifySingletonInstance<TemplateNotification>(named("firstStart"))
	}

	@Test
	fun `first-start notification has correct ID`() {
		assertThat(injector.getInstance<TemplateNotification>(named("firstStart")).id, equalTo("first-start-notification"))
	}

	@Test
	fun `first-start notification is dismissable`() {
		assertThat(injector.getInstance<TemplateNotification>(named("firstStart")).isDismissable, equalTo(true))
	}

	@Test
	fun `first-start notification loads correct template`() {
		loaders.templates += "/templates/notify/firstStartNotification.html" to "1".asTemplate()
		val notification = injector.getInstance<TemplateNotification>(named("firstStart"))
		assertThat(notification.render(), equalTo("1"))
	}

	@Test
	fun `first-start handler is created as singleton`() {
		injector.verifySingletonInstance<FirstStartHandler>()
	}

	@Test
	fun `config-not-read notification is created as singleton`() {
		injector.verifySingletonInstance<TemplateNotification>(named("configNotRead"))
	}

	@Test
	fun `config-not-read notification has correct ID `() {
		assertThat(injector.getInstance<TemplateNotification>(named("configNotRead")).id, equalTo("config-not-read-notification"))
	}

	@Test
	fun `config-not-read notification is dismissable`() {
		assertThat(injector.getInstance<TemplateNotification>(named("configNotRead")).isDismissable, equalTo(true))
	}

	@Test
	fun `config-not-read notification loads correct template`() {
		loaders.templates += "/templates/notify/configNotReadNotification.html" to "1".asTemplate()
		val notification = injector.getInstance<TemplateNotification>(named("configNotRead"))
		assertThat(notification.render(), equalTo("1"))
	}

	@Test
	fun `config-not-read handler is created as singleton`() {
		injector.verifySingletonInstance<ConfigNotReadHandler>()
	}

	@Test
	fun `startup notification can be created`() {
		injector.verifySingletonInstance<TemplateNotification>(named("startup"))
	}

	@Test
	fun `startup notification has correct ID`() {
		assertThat(injector.getInstance<TemplateNotification>(named("startup")).id, equalTo("startup-notification"))
	}

	@Test
	fun `startup notification is dismissable`() {
		assertThat(injector.getInstance<TemplateNotification>(named("startup")).isDismissable, equalTo(true))
	}

	@Test
	fun `startup notification loads correct template`() {
		loaders.templates += "/templates/notify/startupNotification.html" to "1".asTemplate()
		val notification = injector.getInstance<TemplateNotification>(named("startup"))
		assertThat(notification.render(), equalTo("1"))
	}

	@Test
	fun `startup handler is created as singleton`() {
		injector.verifySingletonInstance<StartupHandler>()
	}

	@Test
	fun `web-of-trust notification is created as singleton`() {
		injector.verifySingletonInstance<TemplateNotification>(named("webOfTrust"))
	}

	@Test
	fun `web-of-trust notification has correct ID`() {
		assertThat(injector.getInstance<TemplateNotification>(named("webOfTrust")).id, equalTo("wot-missing-notification"))
	}

	@Test
	fun `web-of-trust notification is dismissable`() {
		assertThat(injector.getInstance<TemplateNotification>(named("webOfTrust")).isDismissable, equalTo(true))
	}

	@Test
	fun `web-of-trust notification loads correct template`() {
		loaders.templates += "/templates/notify/wotMissingNotification.html" to "1".asTemplate()
		val notification = injector.getInstance<TemplateNotification>(named("webOfTrust"))
		assertThat(notification.render(), equalTo("1"))
	}

	@Test
	fun `web-of-trust handler is created as singleton`() {
		injector.verifySingletonInstance<TemplateNotification>(named("webOfTrust"))
	}

	@Test
	fun `web-of-trust reacher is created as singleton`() {
		injector.verifySingletonInstance<Runnable>(named("webOfTrustReacher"))
	}

	@Test
	fun `web-of-trust reacher access the wot connector`() {
		injector.getInstance<Runnable>(named("webOfTrustReacher")).run()
		verify(webOfTrustConnector).ping()
	}

	@Test
	fun `web-of-trust reschedule is created as singleton`() {
		injector.verifySingletonInstance<Consumer<Runnable>>(named("webOfTrustReschedule"))
	}

	@Test
	fun `web-of-trust reschedule schedules at the correct delay`() {
		val webOfTrustPinger = injector.getInstance<WebOfTrustPinger>()
		injector.getInstance<Consumer<Runnable>>(named("webOfTrustReschedule"))(webOfTrustPinger)
		verify(ticker).schedule(ArgumentMatchers.eq(webOfTrustPinger), ArgumentMatchers.eq(15L), ArgumentMatchers.eq(SECONDS))
	}

	@Test
	fun `sone mention detector is created as singleton`() {
		assertThat(injector.getInstance<SoneMentionDetector>(), notNullValue())
	}

	@Test
	fun `sone-mentioned notification is created as singleton`() {
		injector.verifySingletonInstance<ListNotification<Post>>(named("soneMentioned"))
	}

	@Test
	fun `sone-mentioned notification has correct ID`() {
		assertThat(injector.getInstance<ListNotification<Post>>(named("soneMentioned")).id, equalTo("mention-notification"))
	}

	@Test
	fun `sone-mentioned notification is not dismissable`() {
		assertThat(injector.getInstance<ListNotification<Post>>(named("soneMentioned")).isDismissable, equalTo(false))
	}

	@Test
	fun `sone-mentioned notification loads correct template`() {
		loaders.templates += "/templates/notify/mentionNotification.html" to "<% posts>".asTemplate()
		val notification = injector.getInstance<ListNotification<Post>>(named("soneMentioned"))
		val posts = listOf(EmptyPost("1"), EmptyPost("2")).onEach(notification::add)
		assertThat(notification.render(), equalTo(posts.toString()))
	}

	@Test
	fun `sone-mentioned handler is created as singleton`() {
		injector.verifySingletonInstance<SoneMentionedHandler>()
	}

}
