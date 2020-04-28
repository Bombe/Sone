/**
 * Sone - FreenetSessionProviderTest.kt - Copyright © 2020 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.web

import com.google.inject.Guice
import freenet.clients.http.SessionManager
import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.impl.IdOnlySone
import net.pterodactylus.sone.database.SoneProvider
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.eq
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.isProvidedByMock
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.hamcrest.Matchers.sameInstance
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for FreenetSessionProviderTest.
 */
class FreenetSessionProviderTest {

	private var soneProvider: SoneProvider = DelegatingSoneProvider(mock())
	private val sessionManager: SessionManager = deepMock()
	private val provider by lazy { FreenetSessionProvider(soneProvider, sessionManager) }
	private val toadletContext = mock<ToadletContext>()

	@Test
	fun `provider returns null for current sone if no sone exists`() {
		assertThat(provider.getCurrentSone(toadletContext), nullValue())
	}

	@Test
	fun `provider returns singular sone if one sone exists`() {
		val localSone: Sone = IdOnlySone("local")
		soneProvider = object : DelegatingSoneProvider(mock()) {
			override val localSones: Collection<Sone> = listOf(localSone)
		}
		assertThat(provider.getCurrentSone(toadletContext), sameInstance(localSone))
	}

	@Test
	fun `provider returns null if more than one sones exist but none is stored in the session`() {
		soneProvider = object : DelegatingSoneProvider(mock()) {
			override val localSones: Collection<Sone> = listOf(IdOnlySone("1"), IdOnlySone("2"))
		}
		assertThat(provider.getCurrentSone(toadletContext), nullValue())
	}

	@Test
	fun `provider returns sone if more than one sones exist and one is stored in the session`() {
		val localSone = object : IdOnlySone("1") {
			override fun isLocal() = true
		}
		soneProvider = object : DelegatingSoneProvider(mock()) {
			override val localSones: Collection<Sone> = listOf(localSone, IdOnlySone("2"))
			override val soneLoader: (String) -> Sone? get() = { id -> localSone.takeIf { id == "1" } }
		}
		whenever(sessionManager.useSession(toadletContext).getAttribute("Sone.CurrentSone")).thenReturn("1")
		assertThat(provider.getCurrentSone(toadletContext), equalTo<Sone>(localSone))
	}

	@Test
	fun `provider sets sone ID in existing session`() {
		val localSone: Sone = IdOnlySone("local")
		provider.setCurrentSone(toadletContext, localSone)
		verify(sessionManager.useSession(toadletContext)).setAttribute("Sone.CurrentSone", "local")
	}

	@Test
	fun `provider sets sone ID in session it created`() {
		val localSone: Sone = IdOnlySone("local")
		whenever(sessionManager.useSession(toadletContext)).thenReturn(null)
		provider.setCurrentSone(toadletContext, localSone)
		verify(sessionManager.createSession(anyString(), eq(toadletContext))).setAttribute("Sone.CurrentSone", "local")
	}

	@Test
	fun `provider removes sone ID in existing session`() {
		provider.setCurrentSone(toadletContext, null)
		verify(sessionManager.useSession(toadletContext)).removeAttribute("Sone.CurrentSone")
	}

	@Test
	fun `provider does not create session if sone is to be removed and session does not exist`() {
		whenever(sessionManager.useSession(toadletContext)).thenReturn(null)
		provider.setCurrentSone(toadletContext, null)
		verify(sessionManager.createSession(anyString(), eq(toadletContext)), never()).removeAttribute(anyString())
	}

	@Test
	fun `provider can be created by guice`() {
		val injector = Guice.createInjector(
				SessionManager::class.isProvidedByMock(),
				SoneProvider::class.isProvidedByMock()
		)
		assertThat(injector.getInstance<FreenetSessionProvider>(), notNullValue())
	}

}

private open class DelegatingSoneProvider(private val soneProvider: SoneProvider) : SoneProvider by soneProvider
