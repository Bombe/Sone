/*
 * Sone - CreateAlbumPage.java - Copyright © 2011 David Roden
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
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.text.TextFilter;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

/**
 * Page that lets the user create a new album.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreateAlbumPage extends SoneTemplatePage {

	/**
	 * Creates a new “create album” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public CreateAlbumPage(Template template, WebInterface webInterface) {
		super("createAlbum.html", template, "Page.CreateAlbum.Title", webInterface, true);
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
		if (request.getMethod() == Method.POST) {
			String name = request.getHttpRequest().getPartAsStringFailsafe("name", 64).trim();
			if (name.length() == 0) {
				templateContext.set("nameMissing", true);
				return;
			}
			String description = request.getHttpRequest().getPartAsStringFailsafe("description", 256).trim();
			Sone currentSone = getCurrentSone(request.getToadletContext());
			String parentId = request.getHttpRequest().getPartAsStringFailsafe("parent", 36);
			Album parent = webInterface.getCore().getAlbum(parentId, false);
			Album album = webInterface.getCore().createAlbum(currentSone, parent);
			album.setTitle(name).setDescription(TextFilter.filter(request.getHttpRequest().getHeader("host"), description));
			webInterface.getCore().touchConfiguration();
			throw new RedirectException("imageBrowser.html?album=" + album.getId());
		}
	}

}
