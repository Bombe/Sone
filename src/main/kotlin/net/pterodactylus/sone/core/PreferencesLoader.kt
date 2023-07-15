package net.pterodactylus.sone.core

import net.pterodactylus.sone.fcp.FcpInterface.*
import net.pterodactylus.util.config.*

/**
 * Loads preferences stored in a [Configuration] into a [Preferences] object.
 */
class PreferencesLoader(private val preferences: Preferences) {

	fun loadFrom(configuration: Configuration) {
		loadInsertionDelay(configuration)
		loadDownloadBackwardsLimit(configuration)
		loadDownloadCountLimit(configuration)
		loadPostsPerPage(configuration)
		loadImagesPerPage(configuration)
		loadCharactersPerPost(configuration)
		loadPostCutOffLength(configuration)
		loadRequireFullAccess(configuration)
		loadFcpInterfaceActive(configuration)
		loadFcpFullAccessRequired(configuration)
		loadStrictFiltering(configuration)
	}

	private fun loadInsertionDelay(configuration: Configuration) {
		preferences.newInsertionDelay = configuration.getIntValue("Option/InsertionDelay").getValue(null)
	}

	private fun loadDownloadBackwardsLimit(configuration: Configuration) {
		preferences.newDownloadBackwardsLimit = configuration.getIntValue("Option/DownloadBackwardsLimit").getValue(null)
	}

	private fun loadDownloadCountLimit(configuration: Configuration) {
		preferences.newDownloadCountLimit = configuration.getIntValue("Option/DownloadCountLimit").getValue(null)
	}

	private fun loadPostsPerPage(configuration: Configuration) {
		preferences.newPostsPerPage = configuration.getIntValue("Option/PostsPerPage").getValue(null)
	}

	private fun loadImagesPerPage(configuration: Configuration) {
		preferences.newImagesPerPage = configuration.getIntValue("Option/ImagesPerPage").getValue(null)
	}

	private fun loadCharactersPerPost(configuration: Configuration) {
		preferences.newCharactersPerPost = configuration.getIntValue("Option/CharactersPerPost").getValue(null)
	}

	private fun loadPostCutOffLength(configuration: Configuration) {
		try {
			preferences.newPostCutOffLength = configuration.getIntValue("Option/PostCutOffLength").getValue(null)
		} catch (iae1: IllegalArgumentException) { /* previous versions allowed -1, ignore and use default. */
		}
	}

	private fun loadRequireFullAccess(configuration: Configuration) {
		preferences.newRequireFullAccess = configuration.getBooleanValue("Option/RequireFullAccess").getValue(null)
	}

	private fun loadFcpInterfaceActive(configuration: Configuration) {
		preferences.newFcpInterfaceActive = configuration.getBooleanValue("Option/ActivateFcpInterface").getValue(null)
	}

	private fun loadFcpFullAccessRequired(configuration: Configuration) {
		val fullAccessRequiredInteger = configuration.getIntValue("Option/FcpFullAccessRequired").getValue(null)
		preferences.newFcpFullAccessRequired = fullAccessRequiredInteger?.let { FullAccessRequired.values()[it] }
	}

	private fun loadStrictFiltering(configuration: Configuration) {
		preferences.newStrictFiltering = configuration.getBooleanValue("Option/StrictFiltering").getValue(null)
	}

}
