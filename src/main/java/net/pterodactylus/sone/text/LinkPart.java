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
 * TODO
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LinkPart implements Part {

	private final String link;
	private final String text;
	private final String title;

	public LinkPart(String link, String text) {
		this(link, text, text);
	}

	public LinkPart(String link, String text, String title) {
		this.link = link;
		this.text = text;
		this.title = title;
	}

	//
	// ACCESSORS
	//

	public String getLink() {
		return link;
	}

	public String getText() {
		return text;
	}

	public String getTitle() {
		return title;
	}

}
