package net.pterodactylus.sone.main

import freenet.client.async.USKManager
import freenet.l10n.BaseL10n.LANGUAGE.ENGLISH
import freenet.node.Node
import freenet.node.NodeClientCore
import freenet.pluginmanager.PluginRespirator
import net.pterodactylus.sone.test.TestUtil
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.whenever
import org.junit.Test

/**
 * Unit test for [SonePlugin].
 */
class SonePluginTest {

	private val sonePlugin = SonePlugin()
	private val pluginRespirator = deepMock<PluginRespirator>()
	private val node = deepMock<Node>()
	private val clientCore = deepMock<NodeClientCore>()
	private val uskManager = deepMock<USKManager>()

	init {
		TestUtil.setFinalField(node, "clientCore", clientCore)
		whenever(pluginRespirator.node).thenReturn(node)
		TestUtil.setFinalField(clientCore, "uskManager", uskManager);
	}

	@Test
	fun `sone plugin can be started`() {
		sonePlugin.setLanguage(ENGLISH)
		sonePlugin.runPlugin(pluginRespirator)
	}

}
