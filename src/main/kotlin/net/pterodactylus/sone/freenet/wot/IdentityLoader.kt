/*
 * Sone - IdentityLoader.java - Copyright © 2013–2019 David Roden
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

package net.pterodactylus.sone.freenet.wot

import com.google.common.base.*
import com.google.inject.*
import net.pterodactylus.sone.freenet.plugin.*
import java.util.concurrent.TimeUnit.*
import java.util.logging.*

/**
 * Loads [OwnIdentity]s and the [Identity]s they trust.
 */
class IdentityLoader @Inject constructor(private val webOfTrustConnector: WebOfTrustConnector, private val context: Context? = null) {

	private val logger: Logger = Logger.getLogger(IdentityLoader::class.java.name)

	@Throws(WebOfTrustException::class)
	fun loadIdentities() =
			time({ stopwatch, identities -> "Loaded ${identities.size} own identities in ${stopwatch.elapsed(MILLISECONDS) / 1000.0}s." }) {
				webOfTrustConnector.loadAllOwnIdentities()
			}.let(this::loadTrustedIdentitiesForOwnIdentities)

	@Throws(PluginException::class)
	private fun loadTrustedIdentitiesForOwnIdentities(ownIdentities: Collection<OwnIdentity>) =
			ownIdentities
					.also { logger.fine { "Getting trusted identities for ${it.size} own identities..." } }
					.associateWith { ownIdentity ->
						logger.fine { "Getting trusted identities for $ownIdentity..." }
						if (ownIdentity.doesNotHaveCorrectContext()) {
							logger.fine { "Skipping $ownIdentity because of incorrect context." }
							emptySet()
						} else {
							logger.fine { "Loading trusted identities for $ownIdentity from WoT..." }
							time({ stopwatch, identities -> "Loaded ${identities.size} identities for ${ownIdentity.nickname} in ${stopwatch.elapsed(MILLISECONDS) / 1000.0}s." }) {
								try {
									webOfTrustConnector.loadTrustedIdentities(ownIdentity, context?.context)
								} catch (e: Exception) {
									logger.warning { "Caught $e on loading trusted identities for $ownIdentity."}
									throw e
								}
							}
						}
					}

	private fun OwnIdentity.doesNotHaveCorrectContext() =
			context?.let { it.context !in contexts } ?: false

	private fun <R> time(logMessage: (Stopwatch, R) -> String, loader: () -> R) =
			Stopwatch.createStarted().let { stopwatch ->
				loader().also { logger.fine(logMessage(stopwatch, it)) }
			}

}
