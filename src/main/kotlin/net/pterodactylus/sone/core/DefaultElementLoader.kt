package net.pterodactylus.sone.core

import com.google.common.cache.CacheBuilder
import freenet.keys.FreenetURI
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import javax.inject.Inject

/**
 * [ElementLoader] implementation that uses a simple Guava [com.google.common.cache.Cache].
 */
class DefaultElementLoader @Inject constructor(private val freenetInterface: FreenetInterface) : ElementLoader {

	private val loadingLinks = CacheBuilder.newBuilder().build<String, Boolean>()
	private val imageCache = CacheBuilder.newBuilder().build<String, LinkedImage>()
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
				imageCache.get(uri.toString()) { LinkedImage(uri.toString()) }
			}
			removeLoadingLink(uri)
		}

		override fun failed(uri: FreenetURI) {
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
			if (loadingLinks.getIfPresent(link) == null) {
				loadingLinks.put(link, true)
				freenetInterface.startFetch(FreenetURI(link), callback)
			}
		}
		return object : LinkedElement {
			override val link = link
			override val loading = true
		}
	}

}
