/*
 * shortener - CSSPage.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.web.page;

import java.io.InputStream;

/**
 * {@link Page} implementation that delivers CSS files from the class path.
 * 
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CSSPage implements Page {

	/** The prefix for {@link #getPath()}. */
	private final String pathPrefix;

	/** The path used as prefix when loading resources. */
	private final String resourcePathPrefix;

	/**
	 * Creates a new CSS page.
	 * 
	 * @param pathPrefix
	 *            The prefix for {@link #getPath()}
	 * @param resourcePathPrefix
	 *            The path prefix when loading resources
	 */
	public CSSPage(String pathPrefix, String resourcePathPrefix) {
		this.pathPrefix = pathPrefix;
		this.resourcePathPrefix = resourcePathPrefix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return pathPrefix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response handleRequest(Request request) {
		String path = request.getURI().getPath();
		int lastSlash = path.lastIndexOf('/');
		String cssFilename = path.substring(lastSlash + 1);
		InputStream cssInputStream = getClass().getResourceAsStream(resourcePathPrefix + cssFilename);
		if (cssInputStream == null) {
			return new Response(404, "Not found.", null, (String) null);
		}
		return new Response(200, "OK", "text/css", null, cssInputStream);
	}

}
