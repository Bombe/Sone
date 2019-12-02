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
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
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
	private val injector: Injector = createInjector(
			Core::class.isProvidedBy(core),
			NotificationManager::class.isProvidedBy(notificationManager),
			NotificationHandlerModule()
	)

	@Test
	fun `module can create notification handler`() {
		assertThat(injector.getInstance<NotificationHandler>(), notNullValue())
	}

	@Test
	fun `module can create mark-post-known-during-first-start handler`() {
		assertThat(injector.getInstance<MarkPostKnownDuringFirstStartHandler>(), notNullValue())
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

}
