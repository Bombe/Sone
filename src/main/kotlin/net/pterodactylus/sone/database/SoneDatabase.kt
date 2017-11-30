package net.pterodactylus.sone.database

/**
 * Combines a [SoneProvider] and a [SoneStore] into a Sone
 * database.
 */
interface SoneDatabase : SoneProvider, SoneBuilderFactory, SoneStore
