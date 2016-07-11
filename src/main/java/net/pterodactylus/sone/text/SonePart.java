/*
 * Sone - SonePart.java - Copyright © 2011–2016 David Roden
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

package net.pterodactylus.sone.text;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.template.SoneAccessor;

/**
 * {@link Part} implementation that stores a reference to a {@link Sone}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SonePart implements Part {

	/** The referenced {@link Sone}. */
	private final Sone sone;

	/**
	 * Creates a new Sone part.
	 *
	 * @param sone
	 *            The referenced Sone
	 */
	public SonePart(Sone sone) {
		this.sone = sone;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the referenced Sone.
	 *
	 * @return The referenced Sone
	 */
	public Sone getSone() {
		return sone;
	}

	//
	// PART METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return SoneAccessor.getNiceName(sone);
	}

}
