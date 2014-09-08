/*
 * Sone - UploadImagePage.java - Copyright © 2011–2013 David Roden
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.TemporaryImage;
import net.pterodactylus.sone.text.TextFilter;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

import com.google.common.io.ByteStreams;

import freenet.support.api.Bucket;
import freenet.support.api.HTTPUploadedFile;

/**
 * Page implementation that lets the user upload an image.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UploadImagePage extends SoneTemplatePage {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(UploadImagePage.class);

	/**
	 * Creates a new “upload image” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
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
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		if (request.getMethod() == Method.POST) {
			Sone currentSone = getCurrentSone(request.getToadletContext());
			String parentId = request.getHttpRequest().getPartAsStringFailsafe("parent", 36);
			Album parent = webInterface.getCore().getAlbum(parentId);
			if (parent == null) {
				/* TODO - signal error */
				return;
			}
			if (!currentSone.equals(parent.getSone())) {
				/* TODO - signal error. */
				return;
			}
			String name = request.getHttpRequest().getPartAsStringFailsafe("title", 200);
			String description = request.getHttpRequest().getPartAsStringFailsafe("description", 4000);
			HTTPUploadedFile uploadedFile = request.getHttpRequest().getUploadedFile("image");
			Bucket fileBucket = uploadedFile.getData();
			InputStream imageInputStream = null;
			ByteArrayOutputStream imageDataOutputStream = null;
			try {
				imageInputStream = fileBucket.getInputStream();
				/* TODO - check length */
				imageDataOutputStream = new ByteArrayOutputStream((int) fileBucket.size());
				ByteStreams.copy(imageInputStream, imageDataOutputStream);
			} catch (IOException ioe1) {
				logger.log(Level.WARNING, "Could not read uploaded image!", ioe1);
				return;
			} finally {
				fileBucket.free();
				Closer.close(imageInputStream);
				Closer.close(imageDataOutputStream);
			}
			byte[] imageData = imageDataOutputStream.toByteArray();
			ByteArrayInputStream imageDataInputStream = null;
			Image uploadedImage = null;
			try {
				imageDataInputStream = new ByteArrayInputStream(imageData);
				uploadedImage = ImageIO.read(imageDataInputStream);
				if (uploadedImage == null) {
					templateContext.set("messages", webInterface.getL10n().getString("Page.UploadImage.Error.InvalidImage"));
					return;
				}
				String mimeType = getMimeType(imageData);
				TemporaryImage temporaryImage = webInterface.getCore().createTemporaryImage(mimeType, imageData);
				net.pterodactylus.sone.data.Image image = webInterface.getCore().createImage(currentSone, parent, temporaryImage);
				image.modify().setTitle(name).setDescription(TextFilter.filter(request.getHttpRequest().getHeader("host"), description)).setWidth(uploadedImage.getWidth(null)).setHeight(uploadedImage.getHeight(null)).update();
			} catch (IOException ioe1) {
				logger.log(Level.WARNING, "Could not read uploaded image!", ioe1);
				return;
			} finally {
				Closer.close(imageDataInputStream);
				Closer.flush(uploadedImage);
			}
			throw new RedirectException("imageBrowser.html?album=" + parent.getId());
		}
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Tries to detect the MIME type of the encoded image.
	 *
	 * @param imageData
	 *            The encoded image
	 * @return The MIME type of the image, or “application/octet-stream” if the
	 *         image type could not be detected
	 */
	private static String getMimeType(byte[] imageData) {
		ByteArrayInputStream imageDataInputStream = new ByteArrayInputStream(imageData);
		try {
			ImageInputStream imageInputStream = ImageIO.createImageInputStream(imageDataInputStream);
			Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
			if (imageReaders.hasNext()) {
				return imageReaders.next().getOriginatingProvider().getMIMETypes()[0];
			}
		} catch (IOException ioe1) {
			logger.log(Level.FINE, "Could not detect MIME type for image.", ioe1);
		}
		return "application/octet-stream";
	}

}
