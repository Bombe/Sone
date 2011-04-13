/*
 * Sone - DeleteImagePage.java - Copyright © 2011 David Roden
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

import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

/**
 * Page that lets the user delete an {@link Image}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DeleteImagePage extends SoneTemplatePage {

	/**
	 * Creates a new “delete image” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public DeleteImagePage(Template template, WebInterface webInterface) {
		super("deleteImage.html", template, "Page.DeleteImage.Title", webInterface, true);
	}

	//
	// SONETEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		if (request.getMethod() == Method.POST) {
			String imageId = request.getHttpRequest().getPartAsStringFailsafe("image", 36);
			Image image = webInterface.getCore().getImage(imageId, false);
			if (image == null) {
				throw new RedirectException("invalid.html");
			}
			if (!webInterface.getCore().isLocalSone(image.getSone())) {
				throw new RedirectException("noPermission.html");
			}
			webInterface.getCore().deleteImage(image);
			throw new RedirectException("imageBrowser.html?album=" + image.getAlbum().getId());
		}
	}

}
