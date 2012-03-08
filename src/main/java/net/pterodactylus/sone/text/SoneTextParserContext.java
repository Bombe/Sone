/*
 * Sone - SoneTextParserContext.java - Copyright © 2011–2012 David Roden
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
import net.pterodactylus.sone.web.page.FreenetRequest;

/**
 * {@link ParserContext} implementation for the {@link SoneTextParser}. It
 * stores the {@link Sone} that provided the parsed text so that certain links
 * can be marked in a different way.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneTextParserContext implements ParserContext {

	/** The request being processed. */
	private final FreenetRequest request;

	/** The posting Sone. */
	private final Sone postingSone;

	/**
	 * Creates a new link parser context.
	 *
	 * @param request
	 *            The request being processed
	 * @param postingSone
	 *            The posting Sone
	 */
	public SoneTextParserContext(FreenetRequest request, Sone postingSone) {
		this.request = request;
		this.postingSone = postingSone;
	}

	/**
	 * Returns the request that is currently being processed.
	 *
	 * @return The request being processed
	 */
	public FreenetRequest getRequest() {
		return request;
	}

	/**
	 * Returns the Sone that provided the text that is being parsed.
	 *
	 * @return The posting Sone
	 */
	public Sone getPostingSone() {
		return postingSone;
	}

}
