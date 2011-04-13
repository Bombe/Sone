/*
 * Sone - RedirectPage.java - Copyright © 2011 David Roden
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

/**
 * Page implementation that redirects the user to another URL.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class RedirectPage implements Page {

	/** The original path. */
	private String originalPath;

	/** The path to redirect the browser to. */
	private String newPath;

	/**
	 * Creates a new redirect page.
	 *
	 * @param originalPath
	 *            The original path
	 * @param newPath
	 *            The path to redirect the browser to
	 */
	public RedirectPage(String originalPath, String newPath) {
		this.originalPath = originalPath;
		this.newPath = newPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return originalPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response handleRequest(Request request) {
		return new RedirectResponse(newPath);
	}

}
