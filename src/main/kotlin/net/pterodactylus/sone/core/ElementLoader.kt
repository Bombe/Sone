package net.pterodactylus.sone.core

import com.google.inject.ImplementedBy

/**
 * Component that loads images and supplies information about them.
 */
@ImplementedBy(DefaultElementLoader::class)
interface ElementLoader {

	fun loadElement(link: String): LinkedElement

}

data class LinkedElement(val link: String, val failed: Boolean = false, val loading: Boolean = false, val properties: Map<String, Any?> = emptyMap())
