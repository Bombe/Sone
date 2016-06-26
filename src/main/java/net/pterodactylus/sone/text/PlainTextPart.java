/*
 * Sone - PlainTextPart.java - Copyright © 2011–2016 David Roden
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

/**
 * {@link Part} implementation that holds a single piece of text.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PlainTextPart implements Part {

	/** The text of the part. */
	private final String text;

	/**
	 * Creates a new plain-text part.
	 *
	 * @param text
	 *            The text of the part
	 */
	public PlainTextPart(String text) {
		this.text = text;
	}

	//
	// PART METHODS
	//

	/**
	 * Returns the text of this part.
	 *
	 * @return The text of this part
	 */
	@Override
	public String getText() {
		return text;
	}

}
