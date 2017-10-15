package net.pterodactylus.sone.database

/**
 * Combines a [FriendProvider] and a [FriendStore] into a friend database.
 */
interface FriendDatabase : FriendProvider, FriendStore
