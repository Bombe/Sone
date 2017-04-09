package net.pterodactylus.sone.core

import com.google.common.base.Ticker
import com.google.common.cache.Cache
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

	private val loadingLinks: Cache<String, Boolean> = CacheBuilder.newBuilder().build<String, Boolean>()
	private val failureCache: Cache<String, Boolean> = CacheBuilder.newBuilder().ticker(ticker).expireAfterWrite(30, MINUTES).build<String, Boolean>()
	private val imageCache: Cache<String, LinkedElement> = CacheBuilder.newBuilder().build<String, LinkedElement>()
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
				imageCache.get(uri.toString().decode().normalize()) {
					LinkedElement(uri.toString(), properties = mapOf("size" to data.size, "sizeHuman" to data.size.human))
				}
			}
			removeLoadingLink(uri)
		}

		private val Int.human get() = when (this) {
			in 0..1023 -> "$this B"
			in 1024..1048575 -> "${this / 1024} KiB"
			in 1048576..1073741823 -> "${this / 1048576} MiB"
			else -> "${this / 1073741824} GiB"
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

	private fun String.decode() = URLDecoder.decode(this, "UTF-8")!!
	private fun String.normalize() = Normalizer.normalize(this, Normalizer.Form.NFC)!!

}
