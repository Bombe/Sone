package net.pterodactylus.sone.database;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.Identity;

/**
 * Builder for {@link Sone} objects.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface SoneBuilder {

	SoneBuilder from(Identity identity);
	SoneBuilder local();

	Sone build() throws IllegalStateException;

}
