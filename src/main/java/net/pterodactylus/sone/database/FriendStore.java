package net.pterodactylus.sone.database;

import net.pterodactylus.sone.data.Sone;

/**
 * Stores information about the {@link Sone#getFriends() friends} of a {@link Sone}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface FriendStore {

	void addFriend(Sone localSone, String friendSoneId);
	void removeFriend(Sone localSone, String friendSoneId);

}
