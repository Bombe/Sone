package net.pterodactylus.sone.database;

/**
 * Combines a {@link SoneProvider} and a {@link SoneStore} into a Sone
 * database.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface SoneDatabase extends SoneProvider, SoneBuilderFactory, SoneStore {

}
