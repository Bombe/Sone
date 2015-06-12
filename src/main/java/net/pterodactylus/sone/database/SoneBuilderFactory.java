package net.pterodactylus.sone.database;

/**
 * Factory for {@link SoneBuilder}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface SoneBuilderFactory {

	SoneBuilder newSoneBuilder();

}
