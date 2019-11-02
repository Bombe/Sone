/**
 * Sone - FredPluginConnectorTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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

/* Fred-based plugin stuff is mostly deprecated. ¯\_(ツ)_/¯ */
@file:Suppress("DEPRECATION")

package net.pterodactylus.sone.freenet.plugin

import freenet.pluginmanager.*
import freenet.support.*
import freenet.support.api.*
import freenet.support.io.*
import kotlinx.coroutines.*
import net.pterodactylus.sone.freenet.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.rules.*
import kotlin.concurrent.*

class FredPluginConnectorTest {

	@Rule
	@JvmField
	val expectedException = ExpectedException.none()!!

	@Test
	fun `connector throws exception if plugin can not be found`() = runBlocking {
		val pluginConnector = FredPluginConnector(pluginRespiratorFacade)
		expectedException.expect(PluginException::class.java)
		pluginConnector.sendRequest("wrong.plugin", requestFields, requestData)
		Unit
	}

	@Test
	fun `connector returns correct fields and data`() = runBlocking {
		val pluginConnector = FredPluginConnector(pluginRespiratorFacade)
		val reply = pluginConnector.sendRequest("test.plugin", requestFields, requestData)
		assertThat(reply.fields, equalTo(responseFields))
		assertThat(reply.data, equalTo(responseData))
	}

}

private val requestFields = SimpleFieldSetBuilder().put("foo", "bar").get()
private val requestData: Bucket? = ArrayBucket(byteArrayOf(1, 2))
private val responseFields = SimpleFieldSetBuilder().put("baz", "quo").get()
private val responseData: Bucket? = ArrayBucket(byteArrayOf(3, 4))

private val pluginRespiratorFacade = object : PluginRespiratorFacade {
	override fun getPluginTalker(pluginTalker: FredPluginTalker, pluginName: String, identifier: String) =
			if (pluginName == "test.plugin") {
				object : PluginTalkerFacade {
					override fun send(pluginParameters: SimpleFieldSet, data: Bucket?) {
						if ((pluginParameters == requestFields) && (data == requestData)) {
							thread { pluginTalker.onReply(pluginName, identifier, responseFields, responseData) }
						}
					}
				}
			} else {
				throw PluginNotFoundException()
			}
}
