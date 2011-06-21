/*
 * Sone - ParserFilter.java - Copyright © 2011 David Roden
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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.text.FreenetLinkPart;
import net.pterodactylus.sone.text.LinkPart;
import net.pterodactylus.sone.text.Part;
import net.pterodactylus.sone.text.PlainTextPart;
import net.pterodactylus.sone.text.PostPart;
import net.pterodactylus.sone.text.SonePart;
import net.pterodactylus.sone.text.SoneTextParser;
import net.pterodactylus.sone.text.SoneTextParserContext;
import net.pterodactylus.sone.web.page.Page.Request;
import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateContextFactory;
import net.pterodactylus.util.template.TemplateParser;

/**
 * Filter that filters a given text through a {@link SoneTextParser}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ParserFilter implements Filter {

	/** The core. */
	private final Core core;

	/** The link parser. */
	private final SoneTextParser soneTextParser;

	/** The template context factory. */
	private final TemplateContextFactory templateContextFactory;

	/** The template for {@link PlainTextPart}s. */
	private final Template plainTextTemplate = TemplateParser.parse(new StringReader("<%text|html>"));

	/** The template for {@link FreenetLinkPart}s. */
	private final Template linkTemplate = TemplateParser.parse(new StringReader("<a class=\"<%cssClass|html>\" href=\"<%link|html>\" title=\"<%title|html>\"><%text|html></a>"));

	/**
	 * Creates a new filter that runs its input through a {@link SoneTextParser}
	 * .
	 *
	 * @param core
	 *            The core
	 * @param templateContextFactory
	 *            The context factory for rendering the parts
	 * @param soneTextParser
	 *            The Sone text parser
	 */
	public ParserFilter(Core core, TemplateContextFactory templateContextFactory, SoneTextParser soneTextParser) {
		this.core = core;
		this.templateContextFactory = templateContextFactory;
		this.soneTextParser = soneTextParser;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, String> parameters) {
		String text = String.valueOf(data);
		int length = -1;
		try {
			length = Integer.parseInt(parameters.get("length"));
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		String soneKey = parameters.get("sone");
		if (soneKey == null) {
			soneKey = "sone";
		}
		Sone sone = (Sone) templateContext.get(soneKey);
		if (sone == null) {
			sone = core.getSone(soneKey, false);
		}
		Request request = (Request) templateContext.get("request");
		SoneTextParserContext context = new SoneTextParserContext(request, sone);
		StringWriter parsedTextWriter = new StringWriter();
		try {
			Iterable<Part> parts = soneTextParser.parse(context, new StringReader(text));
			if (length > -1) {
				List<Part> shortenedParts = new ArrayList<Part>();
				for (Part part : parts) {
					if (part instanceof PlainTextPart) {
						String longText = ((PlainTextPart) part).getText();
						if (length >= longText.length()) {
							shortenedParts.add(part);
						} else {
							shortenedParts.add(new PlainTextPart(longText.substring(0, length)));
						}
						length -= longText.length();
					} else if (part instanceof LinkPart) {
						shortenedParts.add(part);
						length -= ((LinkPart) part).getText().length();
					} else {
						shortenedParts.add(part);
					}
					if (length <= 0) {
						break;
					}
				}
				parts = shortenedParts;
			}
			render(parsedTextWriter, parts);
		} catch (IOException ioe1) {
			/* no exceptions in a StringReader or StringWriter, ignore. */
		}
		return parsedTextWriter.toString();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Renders the given parts.
	 *
	 * @param writer
	 *            The writer to render the parts to
	 * @param parts
	 *            The parts to render
	 */
	private void render(Writer writer, Iterable<Part> parts) {
		for (Part part : parts) {
			render(writer, part);
		}
	}

	/**
	 * Renders the given part.
	 *
	 * @param writer
	 *            The writer to render the part to
	 * @param part
	 *            The part to render
	 */
	@SuppressWarnings("unchecked")
	private void render(Writer writer, Part part) {
		if (part instanceof PlainTextPart) {
			render(writer, (PlainTextPart) part);
		} else if (part instanceof FreenetLinkPart) {
			render(writer, (FreenetLinkPart) part);
		} else if (part instanceof LinkPart) {
			render(writer, (LinkPart) part);
		} else if (part instanceof SonePart) {
			render(writer, (SonePart) part);
		} else if (part instanceof PostPart) {
			render(writer, (PostPart) part);
		} else if (part instanceof Iterable<?>) {
			render(writer, (Iterable<Part>) part);
		}
	}

	/**
	 * Renders the given plain-text part.
	 *
	 * @param writer
	 *            The writer to render the part to
	 * @param plainTextPart
	 *            The part to render
	 */
	private void render(Writer writer, PlainTextPart plainTextPart) {
		TemplateContext templateContext = templateContextFactory.createTemplateContext();
		templateContext.set("text", plainTextPart.getText());
		plainTextTemplate.render(templateContext, writer);
	}

	/**
	 * Renders the given freenet link part.
	 *
	 * @param writer
	 *            The writer to render the part to
	 * @param freenetLinkPart
	 *            The part to render
	 */
	private void render(Writer writer, FreenetLinkPart freenetLinkPart) {
		renderLink(writer, "/" + freenetLinkPart.getLink(), freenetLinkPart.getText(), freenetLinkPart.getTitle(), freenetLinkPart.isTrusted() ? "freenet-trusted" : "freenet");
	}

	/**
	 * Renders the given link part.
	 *
	 * @param writer
	 *            The writer to render the part to
	 * @param linkPart
	 *            The part to render
	 */
	private void render(Writer writer, LinkPart linkPart) {
		renderLink(writer, "/?_CHECKED_HTTP_=" + linkPart.getLink(), linkPart.getText(), linkPart.getTitle(), "internet");
	}

	/**
	 * Renders the given Sone part.
	 *
	 * @param writer
	 *            The writer to render the part to
	 * @param sonePart
	 *            The part to render
	 */
	private void render(Writer writer, SonePart sonePart) {
		renderLink(writer, "viewSone.html?sone=" + sonePart.getSone().getId(), SoneAccessor.getNiceName(sonePart.getSone()), SoneAccessor.getNiceName(sonePart.getSone()), "in-sone");
	}

	/**
	 * Renders the given post part.
	 *
	 * @param writer
	 *            The writer to render the part to
	 * @param postPart
	 *            The part to render
	 */
	private void render(Writer writer, PostPart postPart) {
		renderLink(writer, "viewPost.html?post=" + postPart.getPost().getId(), getExcerpt(postPart.getPost().getText(), 20), SoneAccessor.getNiceName(postPart.getPost().getSone()), "in-sone");
	}

	/**
	 * Renders the given link.
	 *
	 * @param writer
	 *            The writer to render the link to
	 * @param link
	 *            The link to render
	 * @param text
	 *            The text of the link
	 * @param title
	 *            The title of the link
	 * @param cssClass
	 *            The CSS class of the link
	 */
	private void renderLink(Writer writer, String link, String text, String title, String cssClass) {
		TemplateContext templateContext = templateContextFactory.createTemplateContext();
		templateContext.set("cssClass", cssClass);
		templateContext.set("link", link);
		templateContext.set("text", text);
		templateContext.set("title", title);
		linkTemplate.render(templateContext, writer);
	}

	//
	// STATIC METHODS
	//

	/**
	 * Returns up to {@code length} characters from the given text, appending
	 * “…” if the text is longer.
	 *
	 * @param text
	 *            The text to get an excerpt from
	 * @param length
	 *            The maximum length of the excerpt (without the ellipsis)
	 * @return The excerpt of the text
	 */
	private static String getExcerpt(String text, int length) {
		if (text.length() > length) {
			return text.substring(0, length) + "…";
		}
		return text;
	}

}
