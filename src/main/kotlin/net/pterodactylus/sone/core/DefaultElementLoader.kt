package net.pterodactylus.sone.core

import com.google.common.base.Ticker
import com.google.common.cache.CacheBuilder
import freenet.keys.FreenetURI
import java.io.ByteArrayInputStream
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
			return !mimeType.startsWith("image/")
		}

		override fun loaded(uri: FreenetURI, mimeType: String, data: ByteArray) {
			if (!mimeType.startsWith("image/")) {
				return
			}
			ByteArrayInputStream(data).use {
				ImageIO.read(it)
			}?.let {
				imageCache.get(uri.toString()) { LinkedElement(uri.toString()) }
			}
			removeLoadingLink(uri)
		}

		override fun failed(uri: FreenetURI) {
			failureCache.put(uri.toString(), true)
			removeLoadingLink(uri)
		}

		private fun removeLoadingLink(uri: FreenetURI) {
			synchronized(loadingLinks) {
				loadingLinks.invalidate(uri.toString())
			}
		}
	}

	override fun loadElement(link: String): LinkedElement {
		synchronized(loadingLinks) {
			imageCache.getIfPresent(link)?.run {
				return this
			}
			failureCache.getIfPresent(link)?.run {
				return LinkedElement(link, failed = true)
			}
			if (loadingLinks.getIfPresent(link) == null) {
				loadingLinks.put(link, true)
				freenetInterface.startFetch(FreenetURI(link), callback)
			}
		}
		return LinkedElement(link, loading = true)
	}

}
