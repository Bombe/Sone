package net.pterodactylus.sone.text

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.template.SoneAccessor

/**
 * [Part] implementation that stores a reference to a [Sone].
 */
data class SonePart(val sone: Sone) : Part {

	override val text: String = SoneAccessor.getNiceName(sone)

}
