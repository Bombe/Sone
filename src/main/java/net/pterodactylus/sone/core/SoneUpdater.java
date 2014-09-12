package net.pterodactylus.sone.core;

import net.pterodactylus.sone.data.Sone;

/**
 * Component that decides whether a Sone needs to be downloaded because a
 * newer edition was found.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface SoneUpdater {

	void updateSone(Sone sone, long edition);

}
