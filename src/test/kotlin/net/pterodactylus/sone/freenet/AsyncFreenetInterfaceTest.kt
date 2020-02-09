/**
 * Sone - AsyncFreenetInterfaceTest.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.freenet

import freenet.client.*
import freenet.keys.*
import freenet.support.io.*
import kotlinx.coroutines.*
import net.pterodactylus.sone.core.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import java.util.concurrent.atomic.*
import kotlin.test.*

class AsyncFreenetInterfaceTest {

	@Test
	fun `returned deferred is completed by success`() = runBlocking {
		val result = FetchResult(ClientMetadata(), NullBucket())
		val freenetClient = object : FreenetClient {
			override fun fetch(freenetKey: FreenetURI) = result
		}
		val freenetInterface = AsyncFreenetInterface(freenetClient)
		val fetched = async { freenetInterface.fetchUri(FreenetURI("KSK@GPL.txt")) }

		withTimeout(1000) {
			assertThat(fetched.await(), equalTo(Fetched(FreenetURI("KSK@GPL.txt"), result)))
		}
	}

	@Test
	fun `permanent redircts are being followed`() = runBlocking {
		val result = FetchResult(ClientMetadata(), NullBucket())
		val freenetClient = object : FreenetClient {
			val redirected = AtomicBoolean(false)
			override fun fetch(freenetKey: FreenetURI) =
					if (redirected.compareAndSet(false, true))
						throw FetchException(FetchException.FetchExceptionMode.PERMANENT_REDIRECT, FreenetURI("KSK@GPLv3.txt"))
					else result
		}
		val freenetInterface = AsyncFreenetInterface(freenetClient)
		val fetched = async { freenetInterface.fetchUri(FreenetURI("KSK@GPL.txt")) }

		withTimeout(1000) {
			assertThat(fetched.await(), equalTo(Fetched(FreenetURI("KSK@GPLv3.txt"), result)))
		}
	}

	@Test
	fun `fetch errors are being re-thrown`() = runBlocking<Unit> {
		val freenetClient = object : FreenetClient {
			override fun fetch(freenetKey: FreenetURI) =
					throw FetchException(FetchException.FetchExceptionMode.ALL_DATA_NOT_FOUND)
		}
		val freenetInterface = AsyncFreenetInterface(freenetClient)
		val fetched = supervisorScope { async { freenetInterface.fetchUri(FreenetURI("KSK@GPL.txt")) } }

		withTimeout(1000) {
			assertFailsWith(FetchException::class) {
				fetched.await()
			}
		}
	}

}
