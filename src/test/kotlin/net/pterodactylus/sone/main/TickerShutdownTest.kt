/**
 * Sone - TickerShutdownTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.main

import com.google.common.eventbus.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.test.*
import org.mockito.Mockito.*
import java.util.concurrent.*
import kotlin.test.*

/**
 * Unit test for [TickerShutdown].
 */
@Suppress("UnstableApiUsage")
class TickerShutdownTest {

	private val eventBus = EventBus()
	private val notificationTicker = mock<ScheduledExecutorService>()

	init {
		eventBus.register(TickerShutdown(notificationTicker))
	}

	@Test
	fun `ticker is shutdown on shutdown`() {
		eventBus.post(Shutdown())
		verify(notificationTicker).shutdown()
	}

}
