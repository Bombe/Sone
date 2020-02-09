/*
 * Sone - UpdateChecker.kt - Copyright © 2011–2019 David Roden
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

package net.pterodactylus.sone.core

import com.google.common.eventbus.*
import com.google.common.primitives.*
import com.google.inject.Inject
import freenet.keys.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.version.Version
import java.io.*
import java.util.*
import java.util.logging.*
import java.util.logging.Logger.*
import javax.inject.Singleton

/**
 * Watches the official Sone homepage for new releases.
 */
@Singleton
class UpdateChecker @Inject constructor(
		private val eventBus: EventBus,
		private val freenetInterface: FreenetInterface,
		private val currentRunningVersion: Version,
		pluginHomepage: PluginHomepage) {

	private val logger: Logger = getLogger(UpdateChecker::class.java.name)

	private val currentUri by lazy { FreenetURI(pluginHomepage.homepage) }

	var latestEdition = SonePlugin.getLatestEdition()
		private set

	var latestVersion: Version = currentRunningVersion
		private set

	var latestVersionDate: Long = 0
		private set

	fun hasLatestVersion() =
			latestVersion > currentRunningVersion

	fun start() {
		freenetInterface.registerUsk(currentUri) { uri, edition, newKnownGood, newSlot ->
			logger.log(Level.FINEST, String.format("Found update for %s: %d, %s, %s", uri, edition, newKnownGood, newSlot))
			if (newKnownGood || newSlot) {
				try {
					freenetInterface.fetchUri(uri.setMetaString(arrayOf("sone.properties")))
							?.onNull {
								logger.log(Level.WARNING, String.format("Could not fetch properties of latest homepage: %s", uri))
							}?.fetchResult
							?.asBucket()?.use { resultBucket ->
								resultBucket.inputStream
										.let { parseProperties(it) }
										.let { extractCurrentVersion(it) }
										.onNull { logger.log(Level.INFO, "Invalid data parsed from properties.") }
										?.takeIf { it.version > latestVersion }
										?.also { updateVersionInformation(it, edition) }
										?.also { logger.info { "Found new version: %s (%tc%s)".format(it.version, it.time, if (it.disruptive) ", disruptive" else "") } }
										?.also { eventBus.post(UpdateFoundEvent(it.version, it.time, edition, it.disruptive)) }
							}
				} catch (ioe1: IOException) {
					logger.log(Level.WARNING, String.format("Could not parse sone.properties of %s!", uri), ioe1)
				}
			}
		}
	}

	fun stop() {
		freenetInterface.unregisterUsk(currentUri)
	}

	private fun updateVersionInformation(versionInformation: VersionInformation, edition: Long) {
		latestVersion = versionInformation.version
		latestVersionDate = versionInformation.time
		latestEdition = edition
	}

	private fun parseProperties(propertiesInputStream: InputStream) =
			Properties().apply {
				InputStreamReader(propertiesInputStream, "UTF-8").use { inputStreamReader ->
					load(inputStreamReader)
				}
			}

	private fun extractCurrentVersion(properties: Properties) =
			properties.getProperty("CurrentVersion/Version")
					?.let { Version.parse(it) }
					?.let { version ->
						properties.getProperty("CurrentVersion/ReleaseTime")
								?.let { Longs.tryParse(it) }
								?.let { time ->
									VersionInformation(version, time, disruptiveVersionBetweenCurrentAndFound(properties))
								}
					}

	private fun disruptiveVersionBetweenCurrentAndFound(properties: Properties) =
			properties.stringPropertyNames()
					.filter { it.startsWith("DisruptiveVersion/") }
					.map { it.removePrefix("DisruptiveVersion/") }
					.map { Version.parse(it) }
					.any { it > currentRunningVersion }

}

private data class VersionInformation(val version: Version, val time: Long, val disruptive: Boolean)
