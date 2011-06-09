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
		StringBuilder text = new StringBuilder();

		/* check basic operation. */
		text.setLength(0);
		parts = soneTextParser.parse(null, new StringReader("Test."));
		assertNotNull("Parts", parts);
		for (Part part : parts) {
			assertTrue("Part is PlainTextPart", part instanceof PlainTextPart);
			text.append(((PlainTextPart) part).getText());
		}
		assertEquals("Part Text", "Test.", text.toString());

		/* check empty lines at start and end. */
		text.setLength(0);
		parts = soneTextParser.parse(null, new StringReader("\nTest.\n\n"));
		assertNotNull("Parts", parts);
		for (Part part : parts) {
			assertTrue("Part is PlainTextPart", part instanceof PlainTextPart);
			text.append(((PlainTextPart) part).getText());
		}
		assertEquals("Part Text", "Test.", text.toString());

		/* check duplicate empty lines in the text. */
		text.setLength(0);
		parts = soneTextParser.parse(null, new StringReader("\nTest.\n\n\nTest."));
		assertNotNull("Parts", parts);
		for (Part part : parts) {
			assertTrue("Part is PlainTextPart", part instanceof PlainTextPart);
			text.append(((PlainTextPart) part).getText());
		}
		assertEquals("Part Text", "Test.\n\nTest.", text.toString());
	}

}
