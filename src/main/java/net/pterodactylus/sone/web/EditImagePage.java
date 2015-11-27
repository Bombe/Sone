/*
 * Sone - EditImagePage.java - Copyright © 2010–2015 David Roden
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
import net.pterodactylus.sone.text.TextFilter;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

/**
 * Page that lets the user edit title and description of an {@link Image}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class EditImagePage extends SoneTemplatePage {

	/**
	 * Creates a new “edit image” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public EditImagePage(Template template, WebInterface webInterface) {
		super("editImage.html", template, "Page.EditImage.Title", webInterface, true);
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
			String imageId = request.getHttpRequest().getPartAsStringFailsafe("image", 36);
			String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
			Image image = webInterface.getCore().getImage(imageId, false);
			if (image == null) {
				throw new RedirectException("invalid.html");
			}
			if (!image.getSone().isLocal()) {
				throw new RedirectException("noPermission.html");
			}
			if ("true".equals(request.getHttpRequest().getPartAsStringFailsafe("moveLeft", 4))) {
				image.getAlbum().moveImageUp(image);
			} else if ("true".equals(request.getHttpRequest().getPartAsStringFailsafe("moveRight", 4))) {
				image.getAlbum().moveImageDown(image);
			} else {
				String title = request.getHttpRequest().getPartAsStringFailsafe("title", 100).trim();
				String description = request.getHttpRequest().getPartAsStringFailsafe("description", 1024).trim();
				if (title.length() == 0) {
					throw new RedirectException("emptyImageTitle.html");
				}
				image.modify().setTitle(title).setDescription(TextFilter.filter(request.getHttpRequest().getHeader("host"), description)).update();
			}
			webInterface.getCore().touchConfiguration();
			throw new RedirectException(returnPage);
		}
	}

}
