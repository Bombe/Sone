package net.pterodactylus.sone.database;

/**
 * Combines a {@link FriendProvider} and a {@link FriendStore} into a friend database.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface FriendDatabase extends FriendProvider, FriendStore {

}
