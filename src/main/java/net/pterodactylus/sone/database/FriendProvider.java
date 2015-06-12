package net.pterodactylus.sone.database;

import java.util.Collection;

import net.pterodactylus.sone.data.Sone;

/**
 * Provides information about {@link Sone#getFriends() friends} of a {@link Sone}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface FriendProvider {

	Collection<String> getFriends(Sone localSone);
	boolean isFriend(Sone localSone, String friendSoneId);

}
