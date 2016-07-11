/*
 * Sone - FreenetLinkPart.java - Copyright © 2011–2016 David Roden
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
 * {@link LinkPart} implementation that stores an additional attribute: if the
 * link is an SSK or USK link and the post was created by an identity that owns
 * the keyspace in question.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreenetLinkPart extends LinkPart {

	/** Whether the link is trusted. */
	private final boolean trusted;

	/**
	 * Creates a new freenet link part.
	 *
	 * @param link
	 *            The link of the part
	 * @param text
	 *            The text of the part
	 * @param trusted
	 *            {@code true} if the link is trusted, {@code false} otherwise
	 */
	public FreenetLinkPart(String link, String text, boolean trusted) {
		this(link, text, text, trusted);
	}

	/**
	 * Creates a new freenet link part.
	 *
	 * @param link
	 *            The link of the part
	 * @param text
	 *            The text of the part
	 * @param title
	 *            The title of the part
	 * @param trusted
	 *            {@code true} if the link is trusted, {@code false} otherwise
	 */
	public FreenetLinkPart(String link, String text, String title, boolean trusted) {
		super(link, text, title);
		this.trusted = trusted;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether the link is trusted.
	 *
	 * @return {@code true} if the link is trusted, {@code false} otherwise
	 */
	public boolean isTrusted() {
		return trusted;
	}

}
