/**
 * Sone - WebOfTrustPinger.kt - Copyright © 2019 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.freenet.wot

import com.google.common.eventbus.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.freenet.plugin.*
import net.pterodactylus.sone.utils.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import java.util.concurrent.atomic.*
import kotlin.test.*

/**
 * Unit test for [WebOfTrustPinger].
 */
class WebOfTrustPingerTest {

	private val eventBus = EventBus()
	private val webOfTrustReachable = AtomicBoolean()
	private val webOfTrustReacher: () -> Unit = { webOfTrustReachable.get().onFalse { throw PluginException() } }
	private val rescheduled = AtomicBoolean()
	private val reschedule: () -> Unit = { rescheduled.set(true) }
	private val pinger = WebOfTrustPinger(eventBus, webOfTrustReacher, reschedule)

	@Test
	fun `pinger sends wot appeared event when run first and wot is reachable`() {
		webOfTrustReachable.set(true)
		val appearedReceived = AtomicBoolean()
		eventBus.register(WebOfTrustAppearedCatcher { appearedReceived.set(true) })
		pinger()
		assertThat(appearedReceived.get(), equalTo(true))
	}

	@Test
	fun `pinger reschedules when wot is reachable`() {
		webOfTrustReachable.set(true)
		pinger()
		assertThat(rescheduled.get(), equalTo(true))
	}

	@Test
	fun `pinger sends wot disappeared event when run first and wot is not reachable`() {
		val appearedReceived = AtomicBoolean()
		eventBus.register(WebOfTrustAppearedCatcher { appearedReceived.set(true) })
		pinger()
		assertThat(appearedReceived.get(), equalTo(false))
	}

	@Test
	fun `pinger reschedules when wot is not reachable`() {
		pinger()
		assertThat(rescheduled.get(), equalTo(true))
	}

	@Test
	fun `pinger sends wot disappeared event when run twice and wot is not reachable on second call`() {
		val disappearedReceived = AtomicBoolean()
		eventBus.register(WebOfTrustDisappearedCatcher { disappearedReceived.set(true) })
		webOfTrustReachable.set(true)
		pinger()
		webOfTrustReachable.set(false)
		pinger()
		assertThat(disappearedReceived.get(), equalTo(true))
	}

	@Test
	fun `pinger sends wot appeared event only once`() {
		webOfTrustReachable.set(true)
		val appearedReceived = AtomicBoolean()
		eventBus.register(WebOfTrustAppearedCatcher { appearedReceived.set(true) })
		pinger()
		appearedReceived.set(false)
		pinger()
		assertThat(appearedReceived.get(), equalTo(false))
	}

	@Test
	fun `pinger sends wot disappeared event only once`() {
		val disappearedReceived = AtomicBoolean()
		eventBus.register(WebOfTrustDisappearedCatcher { disappearedReceived.set(true) })
		pinger()
		disappearedReceived.set(false)
		pinger()
		assertThat(disappearedReceived.get(), equalTo(false))
	}

}

private class WebOfTrustAppearedCatcher(private val received: () -> Unit) {
	@Subscribe
	fun webOfTrustAppeared(webOfTrustAppeared: WebOfTrustAppeared) {
		received()
	}
}

private class WebOfTrustDisappearedCatcher(private val received: () -> Unit) {
	@Subscribe
	fun webOfTrustDisappeared(webOfTrustDisappeared: WebOfTrustDisappeared) {
		received()
	}
}
