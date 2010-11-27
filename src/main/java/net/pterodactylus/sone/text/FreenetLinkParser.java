/*
 * Sone - FreenetLinkParser.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.template.TemplateFactory;

/**
 * {@link Parser} implementation that can recognize Freenet URIs.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreenetLinkParser implements Parser {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(FreenetLinkParser.class);

	/** Pattern to detect whitespace. */
	private static final Pattern whitespacePattern = Pattern.compile("[\\p{javaWhitespace}]");

	/**
	 * Enumeration for all recognized link types.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private enum LinkType {

		/** Link is a KSK. */
		KSK,

		/** Link is a CHK. */
		CHK,

		/** Link is an SSK. */
		SSK,

		/** Link is a USK. */
		USK

	}

	/** The template factory. */
	private final TemplateFactory templateFactory;

	/**
	 * Creates a new freenet link parser.
	 *
	 * @param templateFactory
	 *            The template factory
	 */
	public FreenetLinkParser(TemplateFactory templateFactory) {
		this.templateFactory = templateFactory;
	}

	//
	// PART METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Part parse(Reader source) throws IOException {
		PartContainer parts = new PartContainer();
		BufferedReader bufferedReader = (source instanceof BufferedReader) ? (BufferedReader) source : new BufferedReader(source);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			line = line.trim() + "\n";
			while (line.length() > 0) {
				int nextKsk = line.indexOf("KSK@");
				int nextChk = line.indexOf("CHK@");
				int nextSsk = line.indexOf("SSK@");
				int nextUsk = line.indexOf("USK@");
				if ((nextKsk == -1) && (nextChk == -1) && (nextSsk == -1) && (nextUsk == -1)) {
					parts.add(createPlainTextPart(line));
					break;
				}
				int next = Integer.MAX_VALUE;
				LinkType linkType = null;
				if ((nextKsk > -1) && (nextKsk < next)) {
					next = nextKsk;
					linkType = LinkType.KSK;
				}
				if ((nextChk > -1) && (nextChk < next)) {
					next = nextChk;
					linkType = LinkType.CHK;
				}
				if ((nextSsk > -1) && (nextSsk < next)) {
					next = nextSsk;
					linkType = LinkType.SSK;
				}
				if ((nextUsk > -1) && (nextUsk < next)) {
					next = nextUsk;
					linkType = LinkType.USK;
				}
				if ((next >= 8) && (line.substring(next - 8, next).equals("freenet:"))) {
					next -= 8;
					line = line.substring(0, next) + line.substring(next + 8);
				}
				Matcher matcher = whitespacePattern.matcher(line);
				int nextSpace = matcher.find(next) ? matcher.start() : line.length();
				if (nextSpace > (next + 4)) {
					parts.add(createPlainTextPart(line.substring(0, next)));
					String link = line.substring(next, nextSpace);
					String name = link;
					logger.log(Level.FINER, "Found link: " + link);
					logger.log(Level.FINEST, "Next: %d, CHK: %d, SSK: %d, USK: %d", new Object[] { next, nextChk, nextSsk, nextUsk });
					if (((linkType == LinkType.CHK) || (linkType == LinkType.SSK) || (linkType == LinkType.USK)) && (link.length() > 98) && (link.charAt(47) == ',') && (link.charAt(91) == ',') && (link.charAt(99) == '/')) {
						name = link.substring(0, 47) + "…" + link.substring(99);
					}
					parts.add(createLinkPart(link, name));
					line = line.substring(nextSpace);
				} else {
					parts.add(createPlainTextPart(line.substring(0, next + 4)));
					line = line.substring(next + 4);
				}
			}
		}
		return parts;
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates a new plain text part based on a template.
	 *
	 * @param text
	 *            The text to display
	 * @return The part that displays the given text
	 */
	private Part createPlainTextPart(String text) {
		return new TemplatePart(templateFactory.createTemplate(new StringReader("<% text|html>"))).set("text", text);
	}

	/**
	 * Creates a new link part based on a template.
	 *
	 * @param link
	 *            The target of the link
	 * @param name
	 *            The name of the link
	 * @return The part that displays the link
	 */
	private Part createLinkPart(String link, String name) {
		return new TemplatePart(templateFactory.createTemplate(new StringReader("<a href=\"/<% link|html>\"><% name|html></a>"))).set("link", link).set("name", name);
	}

}
