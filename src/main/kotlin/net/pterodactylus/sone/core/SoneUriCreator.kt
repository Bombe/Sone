package net.pterodactylus.sone.core

import freenet.keys.FreenetURI
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.wot.OwnIdentity

/**
 * Injectable helper class that can create request and insert URIs for [Sones][Sone].
 */
open class SoneUriCreator {

	fun getRequestUri(sone: Sone): FreenetURI = sone.identity.requestUri
			.let(::FreenetURI)
			.sonify(sone.latestEdition)

	open fun getInsertUri(sone: Sone): FreenetURI? = (sone.identity as? OwnIdentity)?.insertUri
			?.let(::FreenetURI)
			?.sonify(sone.latestEdition)

}

private fun FreenetURI.sonify(edition: Long): FreenetURI =
		setKeyType("USK")
				.setDocName("Sone")
				.setMetaString(emptyArray())
				.setSuggestedEdition(edition)
