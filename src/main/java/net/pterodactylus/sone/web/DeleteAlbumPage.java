/*
 * Sone - DeleteAlbumPage.java - Copyright © 2011 David Roden
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
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

/**
 * Page that lets the user delete an {@link Album}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DeleteAlbumPage extends SoneTemplatePage {

	/**
	 * Creates a new “delete album” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public DeleteAlbumPage(Template template, WebInterface webInterface) {
		super("deleteAlbum.html", template, "Page.DeleteAlbum.Title", webInterface, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, TemplateContext templateContext) throws RedirectException {
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
			if (request.getHttpRequest().isPartSet("abortDelete")) {
				throw new RedirectException("imageBrowser.html?album=" + album.getId());
			}
			Album parentAlbum = album.getParent();
			webInterface.getCore().deleteAlbum(album);
			if (parentAlbum == null) {
				throw new RedirectException("imageBrowser.html?sone=" + album.getSone().getId());
			}
			throw new RedirectException("imageBrowser.html?album=" + parentAlbum.getId());
		}
		String albumId = request.getHttpRequest().getParam("album");
		Album album = webInterface.getCore().getAlbum(albumId, false);
		if (album == null) {
			throw new RedirectException("invalid.html");
		}
		templateContext.set("album", album);
	}

}
