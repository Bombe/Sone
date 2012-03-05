/*
 * Sone - ImageLinkFilter.java - Copyright © 2011 David Roden
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

package net.pterodactylus.sone.template;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.object.Default;
import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateContextFactory;
import net.pterodactylus.util.template.TemplateParser;

/**
 * Template filter that turns an {@link Image} into an HTML &lt;img&gt; tag,
 * using some parameters to influence parameters of the image.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ImageLinkFilter implements Filter {

	/** The template to render for the &lt;img&gt; tag. */
	private static final Template linkTemplate = TemplateParser.parse(new StringReader("<img<%ifnull !class> class=\"<%class|css>\"<%/if> src=\"<%src|html><%if forceDownload>?forcedownload=true<%/if>\" alt=\"<%alt|html>\" title=\"<%title|html>\" width=\"<%width|html>\" height=\"<%height|html>\" style=\"position: relative;<%ifnull ! top>top: <% top|html>;<%/if><%ifnull ! left>left: <% left|html>;<%/if>\"/>"));

	/** The core. */
	private final Core core;

	/** The template context factory. */
	private final TemplateContextFactory templateContextFactory;

	/**
	 * Creates a new image link filter.
	 *
	 * @param core
	 *            The core
	 * @param templateContextFactory
	 *            The template context factory
	 */
	public ImageLinkFilter(Core core, TemplateContextFactory templateContextFactory) {
		this.core = core;
		this.templateContextFactory = templateContextFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, String> parameters) {
		Image image = null;
		if (data instanceof String) {
			image = core.getImage((String) data, false);
		} else if (data instanceof Image) {
			image = (Image) data;
		}
		if (image == null) {
			return null;
		}
		String imageClass = parameters.get("class");
		int maxWidth = Numbers.safeParseInteger(parameters.get("max-width"), Integer.MAX_VALUE);
		int maxHeight = Numbers.safeParseInteger(parameters.get("max-height"), Integer.MAX_VALUE);
		String mode = String.valueOf(parameters.get("mode"));
		String title = parameters.get("title");
		if ((title != null) && title.startsWith("=")) {
			title = String.valueOf(templateContext.get(title.substring(1)));
		}

		TemplateContext linkTemplateContext = templateContextFactory.createTemplateContext();
		linkTemplateContext.set("class", imageClass);
		if (image.isInserted()) {
			linkTemplateContext.set("src", "/" + image.getKey());
			linkTemplateContext.set("forceDownload", true);
		} else {
			linkTemplateContext.set("src", "getImage.html?image=" + image.getId());
		}
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		if ("enlarge".equals(mode)) {
			double scale = Math.max(maxWidth / (double) imageWidth, maxHeight / (double) imageHeight);
			linkTemplateContext.set("width", (int) (imageWidth * scale + 0.5));
			linkTemplateContext.set("height", (int) (imageHeight * scale + 0.5));
			linkTemplateContext.set("left", String.format("%dpx", (int) (maxWidth - (imageWidth * scale)) / 2));
			linkTemplateContext.set("top", String.format("%dpx", (int) (maxHeight - (imageHeight * scale)) / 2));
		} else {
			double scale = 1;
			if ((imageWidth > maxWidth) || (imageHeight > maxHeight)) {
				scale = Math.min(maxWidth / (double) imageWidth, maxHeight / (double) imageHeight);
			}
			linkTemplateContext.set("width", (int) (imageWidth * scale + 0.5));
			linkTemplateContext.set("height", (int) (imageHeight * scale + 0.5));
		}
		linkTemplateContext.set("alt", Default.forNull(title, image.getDescription()));
		linkTemplateContext.set("title", Default.forNull(title, image.getTitle()));

		StringWriter stringWriter = new StringWriter();
		linkTemplate.render(linkTemplateContext, stringWriter);
		return stringWriter.toString();
	}

}
