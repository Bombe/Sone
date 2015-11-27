/*
 * Sone - Identified.java - Copyright © 2013–2015 David Roden
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

package net.pterodactylus.sone.data;

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.base.Optional;

/**
 * Interface for all objects that expose an ID.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Identified {

	/** Function to extract the ID from an optional. */
	public static final Function<Optional<? extends Identified>, Optional<String>> GET_ID = new Function<Optional<? extends Identified>, Optional<String>>() {

		@Override
		@Nonnull
		public Optional<String> apply(Optional<? extends Identified> identified) {
			return (identified == null) ? Optional.<String>absent() : (identified.isPresent() ? Optional.of(identified.get().getId()) : Optional.<String>absent());
		}
	};

	/**
	 * Returns the ID of this element.
	 *
	 * @return The ID of this element
	 */
	public String getId();

}
