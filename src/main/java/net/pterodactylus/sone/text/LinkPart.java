/*
 * Sone - LinkPart.java - Copyright © 2011 David Roden
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
 * {@link Part} implementation that can hold a link. A link contains of three
 * attributes: the link itself, the text that is shown instead of the link, and
 * an explanatory text that can be displayed e.g. as a tooltip.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LinkPart implements Part {

	/** The link of this part. */
	private final String link;

	/** The text of this part. */
	private final String text;

	/** The title of this part. */
	private final String title;

	/**
	 * Creates a new link part.
	 *
	 * @param link
	 *            The link of the link part
	 * @param text
	 *            The text of the link part
	 */
	public LinkPart(String link, String text) {
		this(link, text, text);
	}

	/**
	 * Creates a new link part.
	 *
	 * @param link
	 *            The link of the link part
	 * @param text
	 *            The text of the link part
	 * @param title
	 *            The title of the link part
	 */
	public LinkPart(String link, String text, String title) {
		this.link = link;
		this.text = text;
		this.title = title;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the link of this part.
	 *
	 * @return The link of this part
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Returns the text of this part.
	 *
	 * @return The text of this part
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns the title of this part.
	 *
	 * @return The title of this part
	 */
	public String getTitle() {
		return title;
	}

}
