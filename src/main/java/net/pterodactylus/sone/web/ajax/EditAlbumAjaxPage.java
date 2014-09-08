/*
 * Sone - EditAlbumAjaxPage.java - Copyright © 2011–2013 David Roden
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

package net.pterodactylus.sone.web.ajax;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.text.TextFilter;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;

/**
 * Page that stores a user’s album modifications.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class EditAlbumAjaxPage extends JsonPage {

	/**
	 * Creates a new edit album AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public EditAlbumAjaxPage(WebInterface webInterface) {
		super("editAlbum.ajax", webInterface);
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
		String albumId = request.getHttpRequest().getParam("album");
		Album album = webInterface.getCore().getAlbum(albumId);
		if (album == null) {
			return createErrorJsonObject("invalid-album-id");
		}
		if (!album.getSone().isLocal()) {
			return createErrorJsonObject("not-authorized");
		}
		if ("true".equals(request.getHttpRequest().getParam("moveLeft"))) {
			Album swappedAlbum = album.getParent().moveAlbumUp(album);
			webInterface.getCore().touchConfiguration();
			return createSuccessJsonObject().put("sourceAlbumId", album.getId()).put("destinationAlbumId", swappedAlbum.getId());
		}
		if ("true".equals(request.getHttpRequest().getParam("moveRight"))) {
			Album swappedAlbum = album.getParent().moveAlbumDown(album);
			webInterface.getCore().touchConfiguration();
			return createSuccessJsonObject().put("sourceAlbumId", album.getId()).put("destinationAlbumId", swappedAlbum.getId());
		}
		String title = request.getHttpRequest().getParam("title").trim();
		String description = request.getHttpRequest().getParam("description").trim();
		album.modify().setTitle(title).setDescription(TextFilter.filter(request.getHttpRequest().getHeader("host"), description)).update();
		webInterface.getCore().touchConfiguration();
		return createSuccessJsonObject().put("albumId", album.getId()).put("title", album.getTitle()).put("description", album.getDescription());
	}

}
