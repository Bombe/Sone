package net.pterodactylus.sone.database

/**
 * Factory for [SoneBuilder]s.
 */
interface SoneBuilderFactory {

	fun newSoneBuilder(): SoneBuilder

}
