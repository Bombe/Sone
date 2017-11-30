package net.pterodactylus.sone.database

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.wot.Identity

/**
 * Builder for [Sone] objects.
 */
interface SoneBuilder {

	fun from(identity: Identity): SoneBuilder
	fun local(): SoneBuilder

	fun build(): Sone

}
