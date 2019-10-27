/* EventBus and Subscribe are marked @Beta, ignore that. And Fred stuff is
 * often marked as deprecated even though there is no replacement. */
@file:Suppress("UnstableApiUsage", "DEPRECATION")

package net.pterodactylus.sone.freenet.plugin

import com.google.common.eventbus.*
import freenet.pluginmanager.*
import freenet.support.*
import freenet.support.api.*
import freenet.support.io.*
import net.pterodactylus.sone.freenet.*
import net.pterodactylus.sone.freenet.plugin.event.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.rules.*

/**
 * Unit test for [PluginConnector].
 */
class PluginConnectorTest {

	@Rule
	@JvmField
	val expectedException = ExpectedException.none()!!

	private val eventBus = EventBus()
	private val pluginRespirator = object : PluginRespiratorFacade {
		val call1Parameters = mutableListOf<Call1Parameters>()
		val call2Parameters = mutableListOf<Call2Parameters>()
		override fun getPluginTalker(pluginTalker: FredPluginTalker, pluginName: String, identifier: String) =
				if ("wrong" in pluginName) {
					throw PluginNotFoundException()
				} else {
					object : PluginTalkerFacade {
						override fun send(pluginParameters: SimpleFieldSet, data: Bucket?) = Unit
								.also { call2Parameters += Call2Parameters(pluginParameters, data) }
					}.also { call1Parameters += Call1Parameters(pluginTalker, pluginName, identifier) }
				}
	}
	private val pluginConnector = FredPluginConnector(eventBus, pluginRespirator)

	@Test
	fun `sending request calls correct method on plugin respirator`() {
		pluginConnector.sendRequest("test.plugin", "test-request-1", fields)
		assertThat(pluginRespirator.call1Parameters, hasSize(1))
		assertThat(pluginRespirator.call1Parameters[0].pluginTalker, sameInstance<FredPluginTalker>(pluginConnector))
		assertThat(pluginRespirator.call1Parameters[0].pluginName, equalTo("test.plugin"))
		assertThat(pluginRespirator.call1Parameters[0].identifier, equalTo("test-request-1"))
	}

	@Test
	fun `sending request with bucket calls correct method on plugin respirator`() {
		pluginConnector.sendRequest("test.plugin", "test-request-1", fields, data)
		assertThat(pluginRespirator.call1Parameters, hasSize(1))
		assertThat(pluginRespirator.call1Parameters[0].pluginTalker, sameInstance<FredPluginTalker>(pluginConnector))
		assertThat(pluginRespirator.call1Parameters[0].pluginName, equalTo("test.plugin"))
		assertThat(pluginRespirator.call1Parameters[0].identifier, equalTo("test-request-1"))
	}

	@Test
	fun `sending request to incorrect plugin translates exception correctly`() {
		expectedException.expect(PluginException::class.java)
		pluginConnector.sendRequest("wrong.plugin", "test-request-1", fields)
	}

	@Test
	fun `sending request with bucket to incorrect plugin translates exception correctly`() {
		expectedException.expect(PluginException::class.java)
		pluginConnector.sendRequest("wrong.plugin", "test-request-1", fields, data)
	}

	@Test
	fun `sending request calls correct method on plugin talker`() {
		pluginConnector.sendRequest("test.plugin", "test-request-1", fields)
		assertThat(pluginRespirator.call2Parameters, hasSize(1))
		assertThat(pluginRespirator.call2Parameters[0].pluginParameters, equalTo(fields))
		assertThat(pluginRespirator.call2Parameters[0].data, nullValue())
	}

	@Test
	fun `sending request with bucket calls correct method on plugin talker`() {
		pluginConnector.sendRequest("test.plugin", "test-request-1", fields, data)
		assertThat(pluginRespirator.call2Parameters, hasSize(1))
		assertThat(pluginRespirator.call2Parameters[0].pluginParameters, equalTo(fields))
		assertThat(pluginRespirator.call2Parameters[0].data, equalTo<Bucket?>(data))
	}

	@Test
	fun `reply is sent to event bus correctly`() {
		val listener = object {
			val receivedReplyEvents = mutableListOf<ReceivedReplyEvent>()
			@Subscribe
			fun onReply(receivedReplyEvent: ReceivedReplyEvent) = Unit.also { receivedReplyEvents += receivedReplyEvent }
		}
		eventBus.register(listener)
		pluginConnector.onReply("test.plugin", "test-request-1", fields, data)
		assertThat(listener.receivedReplyEvents, hasSize(1))
		assertThat(listener.receivedReplyEvents[0].pluginName(), equalTo("test.plugin"))
		assertThat(listener.receivedReplyEvents[0].identifier(), equalTo("test-request-1"))
		assertThat(listener.receivedReplyEvents[0].fieldSet(), equalTo(fields))
		assertThat(listener.receivedReplyEvents[0].data(), equalTo<Bucket?>(data))
	}

}

private val fields = SimpleFieldSetBuilder().put("foo", "bar").get()
private val data = ArrayBucket(byteArrayOf(1, 2))

private data class Call1Parameters(val pluginTalker: FredPluginTalker, val pluginName: String, val identifier: String)
private data class Call2Parameters(val pluginParameters: SimpleFieldSet, val data: Bucket?)
