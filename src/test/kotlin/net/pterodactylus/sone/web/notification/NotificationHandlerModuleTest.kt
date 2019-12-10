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
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.notify.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.notify.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.mockito.Mockito.*
import java.io.*
import kotlin.test.*

/**
 * Unit test for [NotificationHandlerModule].
 */
class NotificationHandlerModuleTest {

	private val core = mock<Core>()
	private val notificationManager = NotificationManager()
	private val loaders = TestLoaders()
	private val injector: Injector = createInjector(
			Core::class.isProvidedBy(core),
			NotificationManager::class.isProvidedBy(notificationManager),
			Loaders::class.isProvidedBy(loaders),
			NotificationHandlerModule()
	)

	@Test
	fun `module can create notification handler`() {
		assertThat(injector.getInstance<NotificationHandler>(), notNullValue())
	}

	@Test
	fun `notification handler is created as singleton`() {
		injector.verifySingletonInstance<NotificationHandler>()
	}

	@Test
	fun `module can create mark-post-known-during-first-start handler`() {
		assertThat(injector.getInstance<MarkPostKnownDuringFirstStartHandler>(), notNullValue())
	}

	@Test
	fun `mark-post-known-during-first-start handler is created as singleton`() {
		injector.verifySingletonInstance<MarkPostKnownDuringFirstStartHandler>()
	}

	@Test
	fun `mark-post-known-during-first-start handler is created with correct action`() {
		notificationManager.addNotification(object : AbstractNotification("first-start-notification") {
			override fun render(writer: Writer?) = Unit
		})
		val handler = injector.getInstance<MarkPostKnownDuringFirstStartHandler>()
		val post = mock<Post>()
		handler.newPostFound(NewPostFoundEvent(post))
		verify(core).markPostKnown(post)
	}

	@Test
	fun `module can create sone-locked-on-startup handler`() {
		assertThat(injector.getInstance<SoneLockedOnStartupHandler>(), notNullValue())
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
	fun `new-sone handler can be created`() {
		assertThat(injector.getInstance<NewSoneHandler>(), notNullValue())
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
	fun `new-remote-post handler can be created`() {
		assertThat(injector.getInstance<NewRemotePostHandler>(), notNullValue())
	}

	@Test
	fun `new-remote-post handler is created as singleton`() {
		injector.verifySingletonInstance<NewRemotePostHandler>()
	}

	@Test
	fun `new-remote-post notification can be created`() {
		assertThat(injector.getInstance<ListNotification<Post>>(named("newRemotePost")), notNullValue())
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
	fun `sone-locked notification can be created`() {
		assertThat(injector.getInstance<ListNotification<Sone>>(named("soneLocked")), notNullValue())
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
	fun `sone-locked handler can be created`() {
		assertThat(injector.getInstance<SoneLockedHandler>(), notNullValue())
	}

	@Test
	fun `sone-locked handler is created as singleton`() {
		injector.verifySingletonInstance<SoneLockedHandler>()
	}

}
