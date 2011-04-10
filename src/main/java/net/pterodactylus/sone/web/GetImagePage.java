/*
 * Sone - GetImagePage.java - Copyright © 2011 David Roden
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

package net.pterodactylus.sone.web;

import net.pterodactylus.sone.data.TemporaryImage;
import net.pterodactylus.sone.web.page.Page;

/**
 * Page that delivers a {@link TemporaryImage} to the browser.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetImagePage implements Page {

	/** The Sone web interface. */
	private final WebInterface webInterface;

	/**
	 * Creates a new “get image” page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public GetImagePage(WebInterface webInterface) {
		this.webInterface = webInterface;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return "getImage.html";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response handleRequest(Request request) {
		String imageId = request.getHttpRequest().getParam("image");
		TemporaryImage temporaryImage = webInterface.getCore().getTemporaryImage(imageId);
		if (temporaryImage == null) {
			return new Response(404, "Not found.", "text/plain; charset=utf-8", "");
		}
		return new Response(200, "OK", temporaryImage.getMimeType(), temporaryImage.getImageData()).setHeader("Content-Disposition", "attachment; filename=" + temporaryImage.getId() + "." + temporaryImage.getMimeType().substring(temporaryImage.getMimeType().lastIndexOf("/") + 1));
	}

}
