package net.pterodactylus.sone.freenet

import freenet.client.*
import freenet.keys.*

/**
 * Facade for Freenet’s [freenet.client.HighLevelSimpleClient] to allow testing.
 */
interface FreenetClient {

	fun fetch(freenetKey: FreenetURI): FetchResult

}

class DefaultFreenetClient(private val highLevelSimpleClient: HighLevelSimpleClient) : FreenetClient {

	override fun fetch(freenetKey: FreenetURI): FetchResult =
			highLevelSimpleClient.fetch(freenetKey)

}
