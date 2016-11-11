package net.pterodactylus.sone.core

/**
 * Component that loads images and supplies information about them.
 */
interface ImageLoader {

	fun toLoadedImage(link: String): LoadedImage?

}

data class LoadedImage(val link: String, val mimeType: String, val width: Int, val height: Int)
