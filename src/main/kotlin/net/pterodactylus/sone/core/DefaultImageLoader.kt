package net.pterodactylus.sone.core

import com.google.common.cache.CacheBuilder
import freenet.keys.FreenetURI
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import javax.inject.Inject

/**
 * [ImageLoader] implementation that uses a simple Guava [com.google.common.cache.Cache].
 */
class DefaultImageLoader @Inject constructor(private val freenetInterface: FreenetInterface) : ImageLoader {

	private val imageCache = CacheBuilder.newBuilder().build<String, LoadedImage>()
	private val callback = object : FreenetInterface.BackgroundFetchCallback {
		override fun loaded(uri: FreenetURI, mimeType: String, data: ByteArray) {
			if (!mimeType.startsWith("image/")) {
				return
			}
			val image = ByteArrayInputStream(data).use {
				ImageIO.read(it)
			}
			val loadedImage = LoadedImage(uri.toString(), mimeType, image.width, image.height)
			imageCache.get(uri.toString()) { loadedImage }
		}

		override fun failed(uri: FreenetURI) {
		}
	}

	override fun toLoadedImage(link: String): LoadedImage? {
		imageCache.getIfPresent(link)?.run {
			return this
		}
		freenetInterface.startFetch(FreenetURI(link), callback)
		return null
	}

}
