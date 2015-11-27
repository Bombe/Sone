/*
 * Sone - ImageBrowserPage.java - Copyright © 2011–2015 David Roden
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

import static com.google.common.collect.FluentIterable.from;
import static net.pterodactylus.sone.data.Album.FLATTENER;
import static net.pterodactylus.sone.data.Album.NOT_EMPTY;
import static net.pterodactylus.sone.data.Album.TITLE_COMPARATOR;
import static net.pterodactylus.sone.utils.NumberParsers.parseInt;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.collection.Pagination;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

/**
 * The image browser page is the entry page for the image management.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ImageBrowserPage extends SoneTemplatePage {

	/**
	 * Creates a new image browser page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public ImageBrowserPage(Template template, WebInterface webInterface) {
		super("imageBrowser.html", template, "Page.ImageBrowser.Title", webInterface, true);
	}

	//
	// SONETEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		String albumId = request.getHttpRequest().getParam("album", null);
		if (albumId != null) {
			Album album = webInterface.getCore().getAlbum(albumId);
			templateContext.set("albumRequested", true);
			templateContext.set("album", album);
			templateContext.set("page", request.getHttpRequest().getParam("page"));
			return;
		}
		String imageId = request.getHttpRequest().getParam("image", null);
		if (imageId != null) {
			Image image = webInterface.getCore().getImage(imageId, false);
			templateContext.set("imageRequested", true);
			templateContext.set("image", image);
			return;
		}
		String soneId = request.getHttpRequest().getParam("sone", null);
		if (soneId != null) {
			Optional<Sone> sone = webInterface.getCore().getSone(soneId);
			templateContext.set("soneRequested", true);
			templateContext.set("sone", sone.orNull());
			return;
		}
		String mode = request.getHttpRequest().getParam("mode", null);
		if ("gallery".equals(mode)) {
			templateContext.set("galleryRequested", true);
			List<Album> albums = new ArrayList<Album>();
			for (Sone sone : webInterface.getCore().getSones()) {
				albums.addAll(from(sone.getRootAlbum().getAlbums()).transformAndConcat(FLATTENER).filter(NOT_EMPTY).toList());
			}
			Collections.sort(albums, TITLE_COMPARATOR);
			Pagination<Album> albumPagination = new Pagination<Album>(albums, 12).setPage(parseInt(request.getHttpRequest().getParam("page"), 0));
			templateContext.set("albumPagination", albumPagination);
			templateContext.set("albums", albumPagination.getItems());
			return;
		}
		Sone sone = getCurrentSone(request.getToadletContext(), false);
		templateContext.set("soneRequested", true);
		templateContext.set("sone", sone);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLinkExcepted(URI link) {
		return true;
	}

}
