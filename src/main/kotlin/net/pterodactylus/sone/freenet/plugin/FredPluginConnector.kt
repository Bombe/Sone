/* Fred’s plugin stuff is mostly deprecated. ¯\_(ツ)_/¯ */
@file:Suppress("DEPRECATION")

package net.pterodactylus.sone.freenet.plugin

import freenet.pluginmanager.*
import freenet.support.*
import freenet.support.api.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import javax.inject.*

/**
 * [PluginConnector] implementation that uses a [PluginRespiratorFacade] and coroutines to send
 * a request to another plugin and receive a reply.
 */
class FredPluginConnector @Inject constructor(private val pluginRespiratorFacade: PluginRespiratorFacade) : PluginConnector {

	override fun sendRequest(pluginName: String, identifier: String, fields: SimpleFieldSet, data: Bucket?): PluginReply {
		val receivedReply = Channel<PluginReply>()
		val responseReceiver = FredPluginTalker { _, _, responseFields, responseData ->
			GlobalScope.launch {
				receivedReply.send(PluginReply(responseFields, responseData))
			}
		}
		try {
			val pluginTalker = pluginRespiratorFacade.getPluginTalker(responseReceiver, pluginName, "")
			pluginTalker.send(fields, data)
			return runBlocking {
				receivedReply.receive()
			}
		} catch (e: PluginNotFoundException) {
			throw PluginException(e)
		}
	}

}
