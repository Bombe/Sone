/*
 * Sone - UploadImagePage.java - Copyright © 2011 David Roden
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

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import freenet.support.api.Bucket;
import freenet.support.api.HTTPUploadedFile;

/**
 * TODO
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UploadImagePage extends SoneTemplatePage {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(UploadImagePage.class);

	/**
	 * TODO
	 *
	 * @param template
	 * @param webInterface
	 */
	public UploadImagePage(Template template, WebInterface webInterface) {
		super("uploadImage.html", template, "Page.UploadImage.Title", webInterface, true);
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
			Sone currentSone = getCurrentSone(request.getToadletContext());
			String parentId = request.getHttpRequest().getPartAsStringFailsafe("parent", 36);
			Album parent = webInterface.getCore().getAlbum(parentId, false);
			if (parent == null) {
				return;
			}
			String name = request.getHttpRequest().getPartAsStringFailsafe("title", 200);
			String description = request.getHttpRequest().getPartAsStringFailsafe("description", 4000);
			HTTPUploadedFile uploadedFile = request.getHttpRequest().getUploadedFile("image");
			Bucket fileBucket = uploadedFile.getData();
			InputStream imageInputStream = null;
			net.pterodactylus.sone.data.Image image = null;
			try {
				imageInputStream = fileBucket.getInputStream();
				Image uploadedImage = ImageIO.read(imageInputStream);
				if (uploadedImage == null) {
					templateContext.set("messages", webInterface.getL10n().getString("Page.UploadImage.Error.InvalidImage"));
					return;
				}
				image = new net.pterodactylus.sone.data.Image().setSone(currentSone);
				image.setTitle(name).setDescription(description).setWidth(uploadedImage.getWidth(null)).setHeight(uploadedImage.getHeight(null));
				parent.addImage(image);
				uploadedImage.flush();
			} catch (IOException ioe1) {
				logger.log(Level.WARNING, "Could not read uploaded image!", ioe1);
				return;
			} finally {
				Closer.close(imageInputStream);
				fileBucket.free();
			}
			throw new RedirectException("imageBrowser.html?image=" + image.getId());
		}
	}

}
