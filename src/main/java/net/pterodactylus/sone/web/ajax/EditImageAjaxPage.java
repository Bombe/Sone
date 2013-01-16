/*
 * Sone - EditImageAjaxPage.java - Copyright © 2011–2012 David Roden
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

import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.template.ParserFilter;
import net.pterodactylus.sone.text.TextFilter;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.collection.MapBuilder;
import net.pterodactylus.util.json.JsonObject;
import net.pterodactylus.util.template.TemplateContext;

/**
 * Page that stores a user’s image modifications.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class EditImageAjaxPage extends JsonPage {

	/** Parser for image descriptions. */
	private final ParserFilter parserFilter;

	/**
	 * Creates a new edit image AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 * @param parserFilter
	 *            The parser filter for image descriptions
	 */
	public EditImageAjaxPage(WebInterface webInterface, ParserFilter parserFilter) {
		super("editImage.ajax", webInterface);
		this.parserFilter = parserFilter;
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(FreenetRequest request) {
		String imageId = request.getHttpRequest().getParam("image");
		Image image = webInterface.getCore().getImage(imageId, false);
		if (image == null) {
			return createErrorJsonObject("invalid-image-id");
		}
		if (!image.getSone().isLocal()) {
			return createErrorJsonObject("not-authorized");
		}
		if ("true".equals(request.getHttpRequest().getParam("moveLeft"))) {
			Image swappedImage = image.getAlbum().moveImageUp(image);
			webInterface.getCore().touchConfiguration();
			return createSuccessJsonObject().put("sourceImageId", image.getId()).put("destinationImageId", swappedImage.getId());
		}
		if ("true".equals(request.getHttpRequest().getParam("moveRight"))) {
			Image swappedImage = image.getAlbum().moveImageDown(image);
			webInterface.getCore().touchConfiguration();
			return createSuccessJsonObject().put("sourceImageId", image.getId()).put("destinationImageId", swappedImage.getId());
		}
		String title = request.getHttpRequest().getParam("title").trim();
		String description = request.getHttpRequest().getParam("description").trim();
		image.setTitle(title).setDescription(TextFilter.filter(request.getHttpRequest().getHeader("host"), description));
		webInterface.getCore().touchConfiguration();
		return createSuccessJsonObject().put("imageId", image.getId()).put("title", image.getTitle()).put("description", image.getDescription()).put("parsedDescription", (String) parserFilter.format(new TemplateContext(), image.getDescription(), new MapBuilder<String, Object>().put("sone", image.getSone()).get()));
	}

}
