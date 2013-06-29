/*
 * Sone - GetImagePage.java - Copyright © 2011–2013 David Roden
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

import java.io.IOException;
import java.net.URI;

import net.pterodactylus.sone.data.TemporaryImage;
import net.pterodactylus.sone.web.page.FreenetPage;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.web.Response;

/**
 * Page that delivers a {@link TemporaryImage} to the browser.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetImagePage implements FreenetPage {

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
	public boolean isPrefixPage() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response handleRequest(FreenetRequest request, Response response) throws IOException {
		String imageId = request.getHttpRequest().getParam("image");
		TemporaryImage temporaryImage = webInterface.getCore().getTemporaryImage(imageId);
		if (temporaryImage == null) {
			return response.setStatusCode(404).setStatusText("Not found.").setContentType("text/html; charset=utf-8");
		}
		String contentType= temporaryImage.getMimeType();
		return response.setStatusCode(200).setStatusText("OK").setContentType(contentType).addHeader("Content-Disposition", "attachment; filename=" + temporaryImage.getId() + "." + contentType.substring(contentType.lastIndexOf('/') + 1)).write(temporaryImage.getImageData());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLinkExcepted(URI link) {
		return false;
	}

}
