package net.pterodactylus.sone.database.memory;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.impl.SoneImpl;
import net.pterodactylus.sone.data.impl.AbstractSoneBuilder;
import net.pterodactylus.sone.database.Database;

/**
 * Memory-based {@link AbstractSoneBuilder} implementation.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MemorySoneBuilder extends AbstractSoneBuilder {

	private final Database database;

	public MemorySoneBuilder(Database database) {
		this.database = database;
	}

	@Override
	public Sone build() throws IllegalStateException {
		validate();
		return new SoneImpl(database, identity, local);
	}

}
