package net.pterodactylus.sone.freenet.plugin

import freenet.pluginmanager.*
import freenet.support.*
import freenet.support.api.*
import freenet.support.io.*
import net.pterodactylus.sone.freenet.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.mockito.ArgumentMatchers.*
import kotlin.test.*

/**
 * Unit test for [FredPluginRespiratorFacade] and [FredPluginTalkerFacade].
 */
@Suppress("DEPRECATION")
class PluginRespiratorFacadeTest {

	@Test
	fun `respirator facade creates correct plugin talker facade`() {
		val pluginTalkerSendParameters = mutableListOf<PluginTalkerSendParameters>()
		val originalPluginTalker = mock<PluginTalker>().apply {
			whenever(send(any(), any())).then { invocation ->
				pluginTalkerSendParameters += PluginTalkerSendParameters(invocation.getArgument(0), invocation.getArgument(1))
				Unit
			}
		}
		val fredPluginTalker = FredPluginTalker { _, _, _, _ -> }
		val pluginRespirator = mock<PluginRespirator>().apply {
			whenever(getPluginTalker(fredPluginTalker, "test.plugin", "test-request-1")).thenReturn(originalPluginTalker)
		}
		val pluginRespiratorFacade = FredPluginRespiratorFacade(pluginRespirator)
		val pluginTalker = pluginRespiratorFacade.getPluginTalker(fredPluginTalker, "test.plugin", "test-request-1")
		pluginTalker.send(fields, data)
		assertThat(pluginTalkerSendParameters, hasSize(1))
		assertThat(pluginTalkerSendParameters[0].parameter, equalTo(fields))
		assertThat(pluginTalkerSendParameters[0].data, equalTo(data))
	}

}

private val fields = SimpleFieldSetBuilder().put("foo", "bar").get()
private val data: Bucket? = ArrayBucket(byteArrayOf(1, 2))

private data class PluginTalkerSendParameters(val parameter: SimpleFieldSet, val data: Bucket?)
