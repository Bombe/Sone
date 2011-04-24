/*
 * Sone - FreenetLinkParserTest.java - Copyright © 2010 David Roden
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

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;
import net.pterodactylus.util.template.HtmlFilter;
import net.pterodactylus.util.template.TemplateContextFactory;

/**
 * JUnit test case for {@link FreenetLinkParser}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreenetLinkParserTest extends TestCase {

	/**
	 * Tests the parser.
	 *
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void testParser() throws IOException {
		TemplateContextFactory templateContextFactory = new TemplateContextFactory();
		templateContextFactory.addFilter("html", new HtmlFilter());
		FreenetLinkParser parser = new FreenetLinkParser(null, templateContextFactory);
		FreenetLinkParserContext context = new FreenetLinkParserContext(null, null);
		Part part;

		part = parser.parse(context, new StringReader("Text."));
		assertEquals("Text.", part.toString());

		part = parser.parse(context, new StringReader("Text.\nText."));
		assertEquals("Text.\nText.", part.toString());

		part = parser.parse(context, new StringReader("Text.\n\nText."));
		assertEquals("Text.\n\nText.", part.toString());

		part = parser.parse(context, new StringReader("Text.\n\n\nText."));
		assertEquals("Text.\n\nText.", part.toString());

		part = parser.parse(context, new StringReader("\nText.\n\n\nText."));
		assertEquals("Text.\n\nText.", part.toString());

		part = parser.parse(context, new StringReader("\nText.\n\n\nText.\n"));
		assertEquals("Text.\n\nText.", part.toString());

		part = parser.parse(context, new StringReader("\nText.\n\n\nText.\n\n"));
		assertEquals("Text.\n\nText.", part.toString());

		part = parser.parse(context, new StringReader("\nText.\n\n\n\nText.\n\n\n"));
		assertEquals("Text.\n\nText.", part.toString());

		part = parser.parse(context, new StringReader("\n\nText.\n\n\n\nText.\n\n\n"));
		assertEquals("Text.\n\nText.", part.toString());

		part = parser.parse(context, new StringReader("\n\nText. KSK@a text.\n\n\n\nText.\n\n\n"));
		assertEquals("Text. <a class=\"freenet\" href=\"/KSK@a\" title=\"KSK@a\">a</a> text.\n\nText.", part.toString());
	}

}
