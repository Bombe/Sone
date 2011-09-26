/*
 * Sone - EditAlbumPage.java - Copyright © 2011 David Roden
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

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

/**
 * Page that lets the user edit the name and description of an album.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class EditAlbumPage extends SoneTemplatePage {

	/**
	 * Creates a new “edit album” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public EditAlbumPage(Template template, WebInterface webInterface) {
		super("editAlbum.html", template, "Page.EditAlbum.Title", webInterface, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		if (request.getMethod() == Method.POST) {
			String albumId = request.getHttpRequest().getPartAsStringFailsafe("album", 36);
			Album album = webInterface.getCore().getAlbum(albumId, false);
			if (album == null) {
				throw new RedirectException("invalid.html");
			}
			if (!webInterface.getCore().isLocalSone(album.getSone())) {
				throw new RedirectException("noPermission.html");
			}
			String albumImageId = request.getHttpRequest().getPartAsStringFailsafe("album-image", 36);
			if (webInterface.getCore().getImage(albumImageId, false) == null) {
				albumImageId = null;
			}
			album.setAlbumImage(albumImageId);
			String title = request.getHttpRequest().getPartAsStringFailsafe("title", 100).trim();
			if (title.length() == 0) {
				templateContext.set("titleMissing", true);
				return;
			}
			String description = request.getHttpRequest().getPartAsStringFailsafe("description", 1000).trim();
			album.setTitle(title).setDescription(description);
			webInterface.getCore().touchConfiguration();
			throw new RedirectException("imageBrowser.html?album=" + album.getId());
		}
	}

}
