package net.pterodactylus.sone.core

import com.google.inject.ImplementedBy

/**
 * Component that loads images and supplies information about them.
 */
@ImplementedBy(DefaultElementLoader::class)
interface ElementLoader {

	fun loadElement(link: String): LinkedElement

}

interface LinkedElement {

	val link: String
	val loading: Boolean

}

data class LinkedImage(override val link: String, override val loading: Boolean = false) : LinkedElement
