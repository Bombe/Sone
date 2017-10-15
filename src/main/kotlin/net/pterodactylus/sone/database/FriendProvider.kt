package net.pterodactylus.sone.database

import net.pterodactylus.sone.data.Sone

/**
 * Provides information about [friends][Sone.getFriends] of a [Sone].
 */
interface FriendProvider {

	fun getFriends(localSone: Sone): Collection<String>
	fun isFriend(localSone: Sone, friendSoneId: String): Boolean

}
