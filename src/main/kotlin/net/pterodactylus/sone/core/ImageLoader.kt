package net.pterodactylus.sone.core

import com.google.inject.ImplementedBy

/**
 * Component that loads images and supplies information about them.
 */
@ImplementedBy(DefaultImageLoader::class)
interface ImageLoader {

	fun toLoadedImage(link: String): LoadedImage?

}

data class LoadedImage(val link: String, val mimeType: String, val width: Int, val height: Int)
