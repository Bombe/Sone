/**
 * Sone - PluginRespiratorFacade.kt - Copyright © 2019 David ‘Bombe’ Roden
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

/* Yes, this handle Fred-based stuff that’s mostly deprecated. */
@file:Suppress("DEPRECATION")

package net.pterodactylus.sone.freenet.plugin

import freenet.pluginmanager.*
import freenet.support.*
import freenet.support.api.*
import javax.inject.*

/**
 * Facade for the only method of a [plugin respirator][PluginRespirator] that Sone actually uses,
 * for easier testing.
 */
interface PluginRespiratorFacade {

	@Throws(PluginNotFoundException::class)
	fun getPluginTalker(pluginTalker: FredPluginTalker, pluginName: String, identifier: String): PluginTalkerFacade

}

/**
 * Facade for a [plugin talker][PluginTalker], for easier testing.
 */
interface PluginTalkerFacade {

	fun send(pluginParameters: SimpleFieldSet, data: Bucket?)

}

/**
 * Fred-based [PluginRespiratorFacade] implementation that proxies the given real [PluginRespirator].
 */
class FredPluginRespiratorFacade @Inject constructor(private val pluginRespirator: PluginRespirator) : PluginRespiratorFacade {

	override fun getPluginTalker(pluginTalker: FredPluginTalker, pluginName: String, identifier: String) =
			FredPluginTalkerFacade(pluginRespirator.getPluginTalker(pluginTalker, pluginName, identifier))

}

/**
 * Fred-based [PluginTalkerFacade] implementation that proxies the given real [PluginTalker].
 */
class FredPluginTalkerFacade(private val pluginTalker: PluginTalker) : PluginTalkerFacade {

	override fun send(pluginParameters: SimpleFieldSet, data: Bucket?) =
			pluginTalker.send(pluginParameters, data)

}
