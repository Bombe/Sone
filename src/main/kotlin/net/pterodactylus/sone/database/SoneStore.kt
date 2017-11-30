package net.pterodactylus.sone.database

import net.pterodactylus.sone.data.Sone

/**
 * Interface for a store for [Sone]s.
 */
interface SoneStore {

	fun storeSone(sone: Sone)
	fun removeSone(sone: Sone)

}
