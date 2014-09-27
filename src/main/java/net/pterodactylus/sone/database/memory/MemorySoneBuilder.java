package net.pterodactylus.sone.database.memory;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.SoneImpl;
import net.pterodactylus.sone.data.impl.AbstractSoneBuilder;

/**
 * Memory-based {@link AbstractSoneBuilder} implementation.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MemorySoneBuilder extends AbstractSoneBuilder {

	@Override
	public Sone build() throws IllegalStateException {
		validate();
		return new SoneImpl(identity, local);
	}

}
