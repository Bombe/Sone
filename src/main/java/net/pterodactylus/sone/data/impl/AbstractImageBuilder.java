/*
 * Sone - AbstractImageBuilder.java - Copyright © 2013–2020 David Roden
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

import net.pterodactylus.sone.database.ImageBuilder;

/**
 * Abstract {@link ImageBuilder} implementation. It stores the state of the new
 * album and performs validation, you only need to implement {@link #build()}.
 */
public abstract class AbstractImageBuilder implements ImageBuilder {

	/** Whether to create an album with a random ID. */
	protected boolean randomId;

	/** The ID of the album to create. */
	protected String id;

	@Override
	public ImageBuilder randomId() {
		randomId = true;
		return this;
	}

	@Override
	public ImageBuilder withId(String id) {
		this.id = id;
		return this;
	}

	//
	// PROTECTED METHODS
	//

	/**
	 * Validates the state of this image builder.
	 *
	 * @throws IllegalStateException
	 * 		if the state is not valid for building a new image
	 */
	protected void validate() throws IllegalStateException {
		checkState((randomId && (id == null)) || (!randomId && (id != null)), "exactly one of random ID or custom ID must be set");
	}

}
