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
		override fun shouldCancel(uri: FreenetURI, mimeType: String, size: Long): Boolean {
			return !mimeType.startsWith("image/") || (size > 2097152)
		}

		override fun loaded(uri: FreenetURI, mimeType: String, data: ByteArray) {
			if (!mimeType.startsWith("image/")) {
				return
			}
			ByteArrayInputStream(data).use {
				ImageIO.read(it)
			}?.let {
				imageCache.get(uri.toString().decode().normalize()) { LinkedElement(uri.toString()) }
			}
			removeLoadingLink(uri)
		}

		override fun failed(uri: FreenetURI) {
			failureCache.put(uri.toString().decode().normalize(), true)
			removeLoadingLink(uri)
		}

		private fun removeLoadingLink(uri: FreenetURI) {
			synchronized(loadingLinks) {
				loadingLinks.invalidate(uri.toString().decode().normalize())
			}
		}
	}

	override fun loadElement(link: String): LinkedElement {
		val normalizedLink = link.decode().normalize()
		synchronized(loadingLinks) {
			imageCache.getIfPresent(normalizedLink)?.run {
				return this
			}
			failureCache.getIfPresent(normalizedLink)?.run {
				return LinkedElement(link, failed = true)
			}
			if (loadingLinks.getIfPresent(normalizedLink) == null) {
				loadingLinks.put(normalizedLink, true)
				freenetInterface.startFetch(FreenetURI(link), callback)
			}
		}
		return LinkedElement(link, loading = true)
	}

	private fun String.decode() = URLDecoder.decode(this, "UTF-8")
	private fun String.normalize() = Normalizer.normalize(this, Normalizer.Form.NFC)

}
