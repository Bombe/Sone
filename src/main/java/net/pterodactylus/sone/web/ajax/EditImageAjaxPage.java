/*
 * Sone - EditImageAjaxPage.java - Copyright © 2011–2016 David Roden
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

import com.google.common.collect.ImmutableMap;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.template.ParserFilter;
import net.pterodactylus.sone.template.RenderFilter;
import net.pterodactylus.sone.template.ShortenFilter;
import net.pterodactylus.sone.text.TextFilter;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.TemplateContext;

/**
 * Page that stores a user’s image modifications.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class EditImageAjaxPage extends JsonPage {

	private final ParserFilter parserFilter;
	private final ShortenFilter shortenFilter;
	private final RenderFilter renderFilter;

	/**
	 * Creates a new edit image AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 * @param parserFilter
	 *            The parser filter for image descriptions
	 */
	public EditImageAjaxPage(WebInterface webInterface, ParserFilter parserFilter, ShortenFilter shortenFilter, RenderFilter renderFilter) {
		super("editImage.ajax", webInterface);
		this.parserFilter = parserFilter;
		this.shortenFilter = shortenFilter;
		this.renderFilter = renderFilter;
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
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
		if (title.isEmpty()) {
			return createErrorJsonObject("invalid-image-title");
		}
		String description = request.getHttpRequest().getParam("description").trim();
		image.modify().setTitle(title).setDescription(TextFilter.filter(request.getHttpRequest().getHeader("host"), description)).update();
		webInterface.getCore().touchConfiguration();
		return createSuccessJsonObject().put("imageId", image.getId()).put("title", image.getTitle()).put("description", image.getDescription()).put("parsedDescription", renderImageDescription(image));
	}

	private String renderImageDescription(Image image) {
		TemplateContext templateContext = new TemplateContext();
		ImmutableMap<String, Object> parameters = ImmutableMap.<String, Object>builder().put("sone", image.getSone()).build();
		Object parts = parserFilter.format(templateContext, image.getDescription(), parameters);
		Object shortenedParts = shortenFilter.format(templateContext, parts, parameters);
		return (String) renderFilter.format(templateContext, shortenedParts, parameters);
	}

}
