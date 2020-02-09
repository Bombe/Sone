/*
 * Sone - MemoryPostBuilder.java - Copyright © 2013–2020 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.database.memory;

import java.util.*;
import javax.annotation.*;

import net.pterodactylus.sone.data.*;
import net.pterodactylus.sone.data.impl.*;
import net.pterodactylus.sone.database.*;

/**
 * {@link PostBuilder} implementation that creates a {@link MemoryPost}.
 */
class MemoryPostBuilder extends AbstractPostBuilder {

	private final MemoryDatabase database;

	public MemoryPostBuilder(MemoryDatabase memoryDatabase, SoneProvider soneProvider) {
		super(soneProvider);
		database = memoryDatabase;
	}

	@Nonnull
	@Override
	public Post build() throws IllegalStateException {
		validate();
		return new MemoryPost(database, soneProvider, randomId ? UUID.randomUUID().toString() : id, senderId, recipientId, currentTime ? System.currentTimeMillis() : time, text);
	}

}
