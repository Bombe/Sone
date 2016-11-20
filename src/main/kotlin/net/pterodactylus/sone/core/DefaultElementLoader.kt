package net.pterodactylus.sone.core

import com.google.common.base.Ticker
import com.google.common.cache.CacheBuilder
import freenet.keys.FreenetURI
import java.io.ByteArrayInputStream
import java.net.URLDecoder
import java.text.Normalizer
import java.util.concurrent.TimeUnit.MINUTES
import javax.imageio.ImageIO
import javax.inject.Inject

/**
 * [ElementLoader] implementation that uses a simple Guava [com.google.common.cache.Cache].
 */
class DefaultElementLoader(private val freenetInterface: FreenetInterface, ticker: Ticker) : ElementLoader {

	@Inject constructor(freenetInterface: FreenetInterface) : this(freenetInterface, Ticker.systemTicker())

	private val loadingLinks = CacheBuilder.newBuilder().build<String, Boolean>()
	private val failureCache = CacheBuilder.newBuilder().ticker(ticker).expireAfterWrite(30, MINUTES).build<String, Boolean>()
	private val imageCache = CacheBuilder.newBuilder().build<String, LinkedElement>()
	private val callback = object : FreenetInterface.BackgroundFetchCallback {
		override fun cancelForMimeType(uri: FreenetURI, mimeType: String): Boolean {
			println("Got MIME Type “$mimeType” for $uri.")
			return !mimeType.startsWith("image/")
		}

		override fun loaded(uri: FreenetURI, mimeType: String, data: ByteArray) {
			println("Got ${data.size} Bytes with MIME Type $mimeType for $uri.")
			if (!mimeType.startsWith("image/")) {
				return
			}
			ByteArrayInputStream(data).use {
				ImageIO.read(it)
			}?.let {
				println("Successfully parsed images from $uri.")
				imageCache.get(uri.toString().decode().normalize()) { LinkedElement(uri.toString()) }
			}
			removeLoadingLink(uri)
		}

		override fun failed(uri: FreenetURI) {
			println("Failed to load $uri.")
			failureCache.put(uri.toString().decode().normalize(), true)
			removeLoadingLink(uri)
		}

		private fun removeLoadingLink(uri: FreenetURI) {
			println("Not loading anymore: $uri.")
			synchronized(loadingLinks) {
				loadingLinks.invalidate(uri.toString().decode().normalize())
			}
		}
	}

	override fun loadElement(link: String): LinkedElement {
		val normalizedLink = link.decode().normalize()
		println("Checking for $normalizedLink...")
		synchronized(loadingLinks) {
			imageCache.getIfPresent(normalizedLink)?.run {
				println("In the Image Cache: $normalizedLink")
				return this
			}
			failureCache.getIfPresent(normalizedLink)?.run {
				println("In the Failure Cache: $normalizedLink")
				return LinkedElement(link, failed = true)
			}
			if (loadingLinks.getIfPresent(normalizedLink) == null) {
				println("Not loading: $normalizedLink")
				loadingLinks.put(normalizedLink, true)
				freenetInterface.startFetch(FreenetURI(link), callback)
			}
		}
		println("Returning loading element: $normalizedLink")
		return LinkedElement(link, loading = true)
	}

	private fun String.decode() = URLDecoder.decode(this, "UTF-8")
	private fun String.normalize() = Normalizer.normalize(this, Normalizer.Form.NFC)

}
