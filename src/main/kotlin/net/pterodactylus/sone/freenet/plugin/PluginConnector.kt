/*
 * Sone - PluginConnector.kt - Copyright © 2010–2019 David Roden
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

package net.pterodactylus.sone.freenet.plugin

import com.google.common.eventbus.*
import com.google.inject.*
import freenet.pluginmanager.*
import freenet.support.*
import freenet.support.api.*
import net.pterodactylus.sone.freenet.plugin.event.*

/**
 * Interface for talking to other plugins. Other plugins are identified by their
 * name and a unique connection identifier.
 */
@Singleton
class PluginConnector @Inject constructor(
		private val eventBus: EventBus,
		private val pluginRespiratorFacade: PluginRespiratorFacade
) : FredPluginTalker {

	@Throws(PluginException::class)
	@JvmOverloads
	fun sendRequest(pluginName: String, identifier: String, fields: SimpleFieldSet, data: Bucket? = null) =
			getPluginTalker(pluginName, identifier).send(fields, data)

	private fun getPluginTalker(pluginName: String, identifier: String) =
			try {
				pluginRespiratorFacade.getPluginTalker(this, pluginName, identifier)
			} catch (pnfe1: PluginNotFoundException) {
				throw PluginException(pnfe1)
			}

	override fun onReply(pluginName: String, identifier: String, params: SimpleFieldSet, data: Bucket) =
			eventBus.post(ReceivedReplyEvent(this, pluginName, identifier, params, data))

}
