package net.pterodactylus.sone.database;

import net.pterodactylus.sone.data.Sone;

/**
 * Interface for a store for {@link Sone}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface SoneStore {

	void storeSone(Sone sone);
	void removeSone(Sone sone);

}
