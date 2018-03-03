/*
 * Sone - AbstractAlbumBuilder.java - Copyright © 2013–2016 David Roden
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

package net.pterodactylus.sone.data.impl;

import static com.google.common.base.Preconditions.checkState;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.AlbumBuilder;

/**
 * Abstract {@link AlbumBuilder} implementation. It stores the state of the new
 * album and performs validation, you only need to implement {@link #build()}.
 */
public abstract class AbstractAlbumBuilder implements AlbumBuilder {

	/** Whether to create an album with a random ID. */
	protected boolean randomId;

	/** The ID of the album to create. */
	protected String id;
	protected Sone sone;

	@Override
	public AlbumBuilder randomId() {
		randomId = true;
		return this;
	}

	@Override
	public AlbumBuilder withId(String id) {
		this.id = id;
		return this;
	}

	public AlbumBuilder by(Sone sone) {
		this.sone = sone;
		return this;
	}

	//
	// PROTECTED METHODS
	//

	/**
	 * Validates the state of this post builder.
	 *
	 * @throws IllegalStateException
	 * 		if the state is not valid for building a new post
	 */
	protected void validate() throws IllegalStateException {
		checkState((randomId && (id == null)) || (!randomId && (id != null)), "exactly one of random ID or custom ID must be set");
		checkState(sone != null, "Sone must not be null");
	}

}
