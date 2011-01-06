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

import net.pterodactylus.sone.data.Sone;
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
	private static final Pattern whitespacePattern = Pattern.compile("[\\u000a\u0020\u00a0\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u200c\u200d\u202f\u205f\u2060\u2800\u3000]");

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
		USK,

		/** Link is HTTP. */
		HTTP,

		/** Link is HTTPS. */
		HTTPS;

	}

	/** The template factory. */
	private final TemplateFactory templateFactory;

	/** The Sone that posted the currently parsed text. */
	private Sone postingSone;

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
	// ACCESSORS
	//

	/**
	 * Sets the Sone that posted the text that will be parsed in the next call
	 * to {@link #parse(Reader)}. You need to synchronize calling this method
	 * and {@link #parse(Reader)}!
	 *
	 * @param sone
	 *            The Sone that posted the text
	 */
	public void setPostingSone(Sone sone) {
		postingSone = sone;
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
				int nextHttp = line.indexOf("http://");
				int nextHttps = line.indexOf("https://");
				if ((nextKsk == -1) && (nextChk == -1) && (nextSsk == -1) && (nextUsk == -1) && (nextHttp == -1) && (nextHttps == -1)) {
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
				if ((nextHttp > -1) && (nextHttp < next)) {
					next = nextHttp;
					linkType = LinkType.HTTP;
				}
				if ((nextHttps > -1) && (nextHttps < next)) {
					next = nextHttps;
					linkType = LinkType.HTTPS;
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
					logger.log(Level.FINER, "Found link: %s", link);
					logger.log(Level.FINEST, "Next: %d, CHK: %d, SSK: %d, USK: %d", new Object[] { next, nextChk, nextSsk, nextUsk });
					if (linkType == LinkType.KSK) {
						name = link.substring(4);
					} else if ((linkType == LinkType.CHK) || (linkType == LinkType.SSK) || (linkType == LinkType.USK)) {
						if (name.indexOf('/') > -1) {
							if (!name.endsWith("/")) {
								name = name.substring(name.lastIndexOf('/') + 1);
							} else {
								if (name.indexOf('/') != name.lastIndexOf('/')) {
									name = name.substring(name.lastIndexOf('/', name.lastIndexOf('/') - 1));
								} else {
									/* shorten to 5 chars. */
									name = name.substring(4, 9);
								}
							}
						}
						if (name.indexOf('?') > -1) {
							name = name.substring(0, name.indexOf('?'));
						}
						boolean fromPostingSone = false;
						if ((linkType == LinkType.SSK) || (linkType == LinkType.USK)) {
							fromPostingSone = link.substring(4, 47).equals(postingSone.getId());
						}
						parts.add(fromPostingSone ? createTrustedFreenetLinkPart(link, name) : createFreenetLinkPart(link, name));
					} else if ((linkType == LinkType.HTTP) || (linkType == LinkType.HTTPS)) {
						name = link.substring(linkType == LinkType.HTTP ? 7 : 8);
						int firstSlash = name.indexOf('/');
						int lastSlash = name.lastIndexOf('/');
						if ((lastSlash - firstSlash) > 3) {
							name = name.substring(0, firstSlash + 1) + "…" + name.substring(lastSlash);
						}
						if (name.endsWith("/")) {
							name = name.substring(0, name.length() - 1);
						}
						if (((name.indexOf('/') > -1) && (name.indexOf('.') < name.lastIndexOf('.', name.indexOf('/'))) || ((name.indexOf('/') == -1) && (name.indexOf('.') < name.lastIndexOf('.')))) && name.startsWith("www.")) {
							name = name.substring(4);
						}
						if (name.indexOf('?') > -1) {
							name = name.substring(0, name.indexOf('?'));
						}
						link = "?_CHECKED_HTTP_=" + link;
						parts.add(createInternetLinkPart(link, name));
					}
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
	 * Creates a new part based on a template that links to a site within the
	 * normal internet.
	 *
	 * @param link
	 *            The target of the link
	 * @param name
	 *            The name of the link
	 * @return The part that displays the link
	 */
	private Part createInternetLinkPart(String link, String name) {
		return new TemplatePart(templateFactory.createTemplate(new StringReader("<a class=\"internet\" href=\"/<% link|html>\" title=\"<% link|html>\"><% name|html></a>"))).set("link", link).set("name", name);
	}

	/**
	 * Creates a new part based on a template that links to a site within
	 * freenet.
	 *
	 * @param link
	 *            The target of the link
	 * @param name
	 *            The name of the link
	 * @return The part that displays the link
	 */
	private Part createFreenetLinkPart(String link, String name) {
		return new TemplatePart(templateFactory.createTemplate(new StringReader("<a class=\"freenet\" href=\"/<% link|html>\" title=\"<% link|html>\"><% name|html></a>"))).set("link", link).set("name", name);
	}

	/**
	 * Creates a new part based on a template that links to a page in the
	 * poster’s keyspace.
	 *
	 * @param link
	 *            The target of the link
	 * @param name
	 *            The name of the link
	 * @return The part that displays the link
	 */
	private Part createTrustedFreenetLinkPart(String link, String name) {
		return new TemplatePart(templateFactory.createTemplate(new StringReader("<a class=\"freenet-trusted\" href=\"/<% link|html>\" title=\"<% link|html>\"><% name|html></a>"))).set("link", link).set("name", name);
	}

}
