/**
 * Sone - DebugPageTest.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.WebTestUtils.*
import net.pterodactylus.sone.web.page.FreenetTemplatePage.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.mockito.Mockito.*
import kotlin.test.*
import org.junit.Assert.assertThrows

class DebugPageTest : WebPageTest(::DebugPage) {

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("debug"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<DebugPage>(), notNullValue())
	}

	@Test
	fun `get request activates debug mode`() {
		try {
			page.handleRequest(soneRequest, templateContext)
		} catch (_: RedirectException) {
		}
		verify(core).setDebug()
	}

	@Test
	fun `get request redirects to index`() {
		val redirect = assertThrows(RedirectException::class.java) {
			page.handleRequest(soneRequest, templateContext)
		}
		assertThat(redirect, redirectsTo("./"))
	}

}
