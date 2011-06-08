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
	private final SoneTextParser textParser;

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
	 */
	public ParserFilter(Core core, TemplateContextFactory templateContextFactory) {
		this.core = core;
		this.templateContextFactory = templateContextFactory;
		textParser = new SoneTextParser(core);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, String> parameters) {
		String text = String.valueOf(data);
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
			render(parsedTextWriter, textParser.parse(context, new StringReader(text)));
		} catch (IOException ioe1) {
			/* no exceptions in a StringReader or StringWriter, ignore. */
		}
		return parsedTextWriter.toString();
	}

	//
	// PRIVATE METHODS
	//

	private void render(Writer writer, Iterable<Part> parts) throws IOException {
		for (Part part : parts) {
			render(writer, part);
		}
	}

	private void render(Writer writer, Part part) throws IOException {
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

	private void render(Writer writer, PlainTextPart plainTextPart) throws IOException {
		TemplateContext templateContext = templateContextFactory.createTemplateContext();
		templateContext.set("text", plainTextPart.getText());
		plainTextTemplate.render(templateContext, writer);
	}

	private void render(Writer writer, FreenetLinkPart freenetLinkPart) throws IOException {
		renderLink(writer, "/" + freenetLinkPart.getLink(), freenetLinkPart.getText(), freenetLinkPart.getTitle(), freenetLinkPart.isTrusted() ? "freenet-trusted" : "freenet");
	}

	private void render(Writer writer, LinkPart linkPart) throws IOException {
		renderLink(writer, "/?_CHECKED_HTTP_=" + linkPart.getLink(), linkPart.getText(), linkPart.getTitle(), "internet");
	}

	private void render(Writer writer, SonePart sonePart) throws IOException {
		renderLink(writer, "viewSone.html?sone=" + sonePart.getSone().getId(), SoneAccessor.getNiceName(sonePart.getSone()), SoneAccessor.getNiceName(sonePart.getSone()), "in-sone");
	}

	private void render(Writer writer, PostPart postPart) throws IOException {
		renderLink(writer, "viewPost.html?post=" + postPart.getPost().getId(), getExcerpt(postPart.getPost().getText(), 20), SoneAccessor.getNiceName(postPart.getPost().getSone()), "in-sone");
	}

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

	private static String getExcerpt(String text, int length) {
		if (text.length() > length) {
			return text.substring(0, length) + "…";
		}
		return text;
	}

}
