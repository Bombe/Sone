package net.pterodactylus.sone.freenet

import freenet.client.*
import freenet.keys.*
import kotlinx.coroutines.*
import net.pterodactylus.sone.core.*

class AsyncFreenetInterface(private val freenetClient: FreenetClient) {

	suspend fun fetchUri(freenetUri: FreenetURI): Fetched {
		var currentUri = freenetUri
		var result: FetchResult? = null
		while (result == null) {
			try {
				result = withContext(Dispatchers.Default) { freenetClient.fetch(currentUri) }
			} catch (fetchException: FetchException) {
				if (fetchException.mode == FetchException.FetchExceptionMode.PERMANENT_REDIRECT) {
					currentUri = fetchException.newURI
					continue
				} else
					throw fetchException
			}
		}
		return Fetched(currentUri, result)
	}

}
