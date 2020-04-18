/*
 * Sone - Preferences.kt - Copyright © 2013–2020 David Roden
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

import com.google.common.eventbus.EventBus
import net.pterodactylus.sone.core.event.InsertionDelayChangedEvent
import net.pterodactylus.sone.core.event.StrictFilteringActivatedEvent
import net.pterodactylus.sone.core.event.StrictFilteringDeactivatedEvent
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.ALWAYS
import net.pterodactylus.sone.fcp.event.FcpInterfaceActivatedEvent
import net.pterodactylus.sone.fcp.event.FcpInterfaceDeactivatedEvent
import net.pterodactylus.sone.fcp.event.FullAccessRequiredChanged
import net.pterodactylus.sone.utils.DefaultOption
import net.pterodactylus.util.config.Configuration
import net.pterodactylus.util.config.ConfigurationException
import java.lang.Integer.MAX_VALUE

/**
 * Convenience interface for external classes that want to access the core’s
 * configuration.
 */
class Preferences(private val eventBus: EventBus) {

	private val _insertionDelay = DefaultOption(60) { it in 0..MAX_VALUE }
	val insertionDelay: Int get() = _insertionDelay.get()
	var newInsertionDelay: Int?
		get() = unsupported
		set(value) {
			_insertionDelay.set(value)
			eventBus.post(InsertionDelayChangedEvent(insertionDelay))
			eventBus.post(PreferenceChangedEvent("InsertionDelay", insertionDelay))
		}

	private val _postsPerPage = DefaultOption(10) { it in 1..MAX_VALUE }
	val postsPerPage: Int get() = _postsPerPage.get()
	var newPostsPerPage: Int?
		get() = unsupported
		set(value) {
			_postsPerPage.set(value)
			eventBus.post(PreferenceChangedEvent("PostsPerPage", postsPerPage))
		}

	private val _imagesPerPage = DefaultOption(9) { it in 1..MAX_VALUE }
	val imagesPerPage: Int get() = _imagesPerPage.get()
	var newImagesPerPage: Int?
		get() = unsupported
		set(value: Int?) = _imagesPerPage.set(value)

	private val _charactersPerPost = DefaultOption(400) { it == -1 || it in 50..MAX_VALUE }
	val charactersPerPost: Int get() = _charactersPerPost.get()
	var newCharactersPerPost: Int?
		get() = unsupported
		set(value) = _charactersPerPost.set(value)

	private val _postCutOffLength = DefaultOption(200) { it in 50..MAX_VALUE }
	val postCutOffLength: Int get() = _postCutOffLength.get()
	var newPostCutOffLength: Int?
		get() = unsupported
		set(value) = _postCutOffLength.set(value)

	private val _requireFullAccess = DefaultOption(false)
	val requireFullAccess: Boolean get() = _requireFullAccess.get()
	var newRequireFullAccess: Boolean?
		get() = unsupported
		set(value) = _requireFullAccess.set(value)

	private val _fcpInterfaceActive = DefaultOption(false)
	val fcpInterfaceActive: Boolean get() = _fcpInterfaceActive.get()
	var newFcpInterfaceActive: Boolean?
		get() = unsupported
		set(value) {
			_fcpInterfaceActive.set(value)
			when (value) {
				true -> eventBus.post(FcpInterfaceActivatedEvent())
				else -> eventBus.post(FcpInterfaceDeactivatedEvent())
			}
		}

	private val _fcpFullAccessRequired = DefaultOption(ALWAYS)
	val fcpFullAccessRequired: FullAccessRequired get() = _fcpFullAccessRequired.get()
	var newFcpFullAccessRequired: FullAccessRequired?
		get() = unsupported
		set(value) {
			_fcpFullAccessRequired.set(value)
			eventBus.post(FullAccessRequiredChanged(fcpFullAccessRequired))
		}

	private val _strictFiltering = DefaultOption(false)
	val strictFiltering: Boolean get() = _strictFiltering.get()
	var newStrictFiltering: Boolean? = false
		set(value) {
			_strictFiltering.set(value)
			when (strictFiltering) {
				true -> eventBus.post(StrictFilteringActivatedEvent())
				else -> eventBus.post(StrictFilteringDeactivatedEvent())
			}
		}

	@Throws(ConfigurationException::class)
	fun saveTo(configuration: Configuration) {
		configuration.getIntValue("Option/ConfigurationVersion").value = 0
		configuration.getIntValue("Option/InsertionDelay").value = _insertionDelay.real
		configuration.getIntValue("Option/PostsPerPage").value = _postsPerPage.real
		configuration.getIntValue("Option/ImagesPerPage").value = _imagesPerPage.real
		configuration.getIntValue("Option/CharactersPerPost").value = _charactersPerPost.real
		configuration.getIntValue("Option/PostCutOffLength").value = _postCutOffLength.real
		configuration.getBooleanValue("Option/RequireFullAccess").value = _requireFullAccess.real
		configuration.getBooleanValue("Option/ActivateFcpInterface").value = _fcpInterfaceActive.real
		configuration.getIntValue("Option/FcpFullAccessRequired").value = toInt(_fcpFullAccessRequired.real)
		configuration.getBooleanValue("Option/StrictFiltering").value = _strictFiltering.real
	}

	private fun toInt(fullAccessRequired: FullAccessRequired?): Int? {
		return fullAccessRequired?.ordinal
	}

}

private val unsupported: Nothing get() = throw UnsupportedOperationException()
