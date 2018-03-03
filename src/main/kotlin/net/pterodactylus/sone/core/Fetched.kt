package net.pterodactylus.sone.core

import freenet.client.FetchResult
import freenet.keys.FreenetURI

/**
 * Container for a fetched URI and the [FetchResult].
 */
data class Fetched(val freenetUri: FreenetURI, val fetchResult: FetchResult)
