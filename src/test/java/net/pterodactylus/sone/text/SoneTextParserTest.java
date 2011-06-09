/*
 * Sone - SoneTextParserTest.java - Copyright © 2011 David Roden
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

/**
 * JUnit test case for {@link SoneTextParser}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneTextParserTest extends TestCase {

	/**
	 * Tests basic plain-text operation of the parser.
	 *
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void testPlainText() throws IOException {
		SoneTextParser soneTextParser = new SoneTextParser(null, null);
		Iterable<Part> parts;

		/* check basic operation. */
		parts = soneTextParser.parse(null, new StringReader("Test."));
		assertNotNull("Parts", parts);
		assertEquals("Part Text", "Test.", convertText(parts, PlainTextPart.class));

		/* check empty lines at start and end. */
		parts = soneTextParser.parse(null, new StringReader("\nTest.\n\n"));
		assertNotNull("Parts", parts);
		assertEquals("Part Text", "Test.", convertText(parts, PlainTextPart.class));

		/* check duplicate empty lines in the text. */
		parts = soneTextParser.parse(null, new StringReader("\nTest.\n\n\nTest."));
		assertNotNull("Parts", parts);
		assertEquals("Part Text", "Test.\n\nTest.", convertText(parts, PlainTextPart.class));
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Converts all given {@link Part}s into a string, validating that the
	 * part’s classes match only the expected classes.
	 *
	 * @param parts
	 *            The parts to convert to text
	 * @param validClasses
	 *            The valid classes; if no classes are given, all classes are
	 *            valid
	 * @return The converted text
	 */
	private String convertText(Iterable<Part> parts, Class<?>... validClasses) {
		StringBuilder text = new StringBuilder();
		for (Part part : parts) {
			assertNotNull("Part", part);
			boolean classValid = validClasses.length == 0;
			for (Class<?> validClass : validClasses) {
				if (validClass.isAssignableFrom(part.getClass())) {
					classValid = true;
					break;
				}
			}
			if (!classValid) {
				assertEquals("Part’s Class", null, part.getClass());
			}
			if (part instanceof PlainTextPart) {
				text.append(((PlainTextPart) part).getText());
			} else if (part instanceof FreenetLinkPart) {
				FreenetLinkPart freenetLinkPart = (FreenetLinkPart) part;
				text.append('[').append(freenetLinkPart.getLink()).append('|').append(freenetLinkPart.isTrusted() ? "trusted|" : "").append(freenetLinkPart.getText()).append(']');
			} else if (part instanceof LinkPart) {
				LinkPart linkPart = (LinkPart) part;
				text.append('[').append(linkPart.getLink()).append('|').append(linkPart.getText()).append(']');
			}
		}
		return text.toString();
	}

}
