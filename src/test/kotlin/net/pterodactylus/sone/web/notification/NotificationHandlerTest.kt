/**
 * Sone - NotificationHandlerTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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
import com.google.inject.Guice.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import net.pterodactylus.util.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for [NotificationHandler].
 */
class NotificationHandlerTest {

	private val eventBus = TestEventBus()
	private val loaders = TestLoaders()
	private val notificationManager = NotificationManager()
	private val handler = NotificationHandler(eventBus, loaders, notificationManager)

	@Test
	fun `notification handler can be created by guice`() {
		val injector = createInjector(
				EventBus::class.isProvidedBy(eventBus),
				NotificationManager::class.isProvidedBy(notificationManager),
				Loaders::class.isProvidedBy(loaders)
		)
		assertThat(injector.getInstance<NotificationHandler>(), notNullValue())
	}

	@Test
	fun `notification handler registers handler for sone-locked event`() {
		handler.start()
		assertThat(eventBus.registeredObjects, hasItem<Any>(matches { it.javaClass == SoneLockedOnStartupHandler::class.java }))
	}

	@Test
	fun `notification handler loads sone-locked notification template`() {
		handler.start()
		assertThat(loaders.requestedTemplatePaths, hasItem("/templates/notify/soneLockedOnStartupNotification.html"))
	}

	@Test
	fun `notification handler registers handler for new sone events`() {
		handler.start()
		assertThat(eventBus.registeredObjects, hasItem<Any>(matches { it.javaClass == NewSoneHandler::class.java }))
	}

	@Test
	fun `notification handler loads new sone notification template`() {
		handler.start()
		assertThat(loaders.requestedTemplatePaths, hasItem("/templates/notify/newSoneNotification.html"))
	}

}

@Suppress("UnstableApiUsage")
private class TestEventBus : EventBus() {
	private val _registeredObjects = mutableListOf<Any>()
	val registeredObjects: List<Any>
		get() = _registeredObjects

	override fun register(`object`: Any) {
		super.register(`object`)
		_registeredObjects += `object`
	}

}

private class TestLoaders : Loaders {
	val requestedTemplatePaths = mutableListOf<String>()

	override fun loadTemplate(path: String) =
			Template().also { requestedTemplatePaths += path }

	override fun <REQ : Request> loadStaticPage(basePath: String, prefix: String, mimeType: String) = object : Page<REQ> {

		override fun getPath() = ""
		override fun isPrefixPage() = false
		override fun handleRequest(request: REQ, response: Response) = response

	}

	override fun getTemplateProvider() = TemplateProvider { _, _ -> Template() }

}
