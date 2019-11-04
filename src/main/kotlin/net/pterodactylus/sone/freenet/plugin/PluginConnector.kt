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

import freenet.support.*
import freenet.support.api.*

/**
 * Interface for talking to other plugins. Other plugins are identified by their
 * name and a unique connection identifier.
 */
interface PluginConnector {

	/**
	 * Sends a message to another plugin running in the same node.
	 *
	 * @param pluginName The fully qualified name of the plugin
	 * @param fields The message being sent
	 * @param data Optional data
	 * @return The reply from the plugin
	 * @throws PluginException if the plugin identified by [pluginName] does not exist
	 */
	@Throws(PluginException::class)
	suspend fun sendRequest(pluginName: String, fields: SimpleFieldSet, data: Bucket? = null): PluginReply

}

data class PluginReply(val fields: SimpleFieldSet, val data: Bucket?)
