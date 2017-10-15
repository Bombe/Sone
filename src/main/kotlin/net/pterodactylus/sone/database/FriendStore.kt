package net.pterodactylus.sone.database

import net.pterodactylus.sone.data.Sone

/**
 * Stores information about the [friends][Sone.getFriends] of a [Sone].
 */
interface FriendStore {

	fun addFriend(localSone: Sone, friendSoneId: String)
	fun removeFriend(localSone: Sone, friendSoneId: String)

}
