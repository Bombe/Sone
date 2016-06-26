/*
 * Sone - SoneTextParserTest.java - Copyright © 2011–2015 David Roden
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
import java.util.Arrays;
import java.util.Collection;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.impl.IdOnlySone;
import net.pterodactylus.sone.database.SoneProvider;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import junit.framework.TestCase;

/**
 * JUnit test case for {@link SoneTextParser}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneTextParserTest extends TestCase {

	//
	// ACTIONS
	//

	/**
	 * Tests basic plain-text operation of the parser.
	 *
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@SuppressWarnings("static-method")
	public void testPlainText() throws IOException {
		SoneTextParser soneTextParser = new SoneTextParser(null, null);
		Iterable<Part> parts;

		/* check basic operation. */
		parts = soneTextParser.parse("Test.", null);
		assertNotNull("Parts", parts);
		assertEquals("Part Text", "Test.", convertText(parts, PlainTextPart.class));

		/* check empty lines at start and end. */
		parts = soneTextParser.parse("\nTest.\n\n", null);
		assertNotNull("Parts", parts);
		assertEquals("Part Text", "Test.", convertText(parts, PlainTextPart.class));

		/* check duplicate empty lines in the text. */
		parts = soneTextParser.parse("\nTest.\n\n\nTest.", null);
		assertNotNull("Parts", parts);
		assertEquals("Part Text", "Test.\n\nTest.", convertText(parts, PlainTextPart.class));
	}

	/**
	 * Tests parsing of KSK links.
	 *
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@SuppressWarnings("static-method")
	public void testKSKLinks() throws IOException {
		SoneTextParser soneTextParser = new SoneTextParser(null, null);
		Iterable<Part> parts;

		/* check basic links. */
		parts = soneTextParser.parse("KSK@gpl.txt", null);
		assertNotNull("Parts", parts);
		assertEquals("Part Text", "[KSK@gpl.txt|gpl.txt|gpl.txt]", convertText(parts, FreenetLinkPart.class));

		/* check embedded links. */
		parts = soneTextParser.parse("Link is KSK@gpl.txt\u200b.", null);
		assertNotNull("Parts", parts);
		assertEquals("Part Text", "Link is [KSK@gpl.txt|gpl.txt|gpl.txt]\u200b.", convertText(parts, PlainTextPart.class, FreenetLinkPart.class));

		/* check embedded links and line breaks. */
		parts = soneTextParser.parse("Link is KSK@gpl.txt\nKSK@test.dat\n", null);
		assertNotNull("Parts", parts);
		assertEquals("Part Text", "Link is [KSK@gpl.txt|gpl.txt|gpl.txt]\n[KSK@test.dat|test.dat|test.dat]", convertText(parts, PlainTextPart.class, FreenetLinkPart.class));
	}

	/**
	 * Test case for a bug that was discovered in 0.6.7.
	 *
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@SuppressWarnings({ "synthetic-access", "static-method" })
	public void testEmptyLinesAndSoneLinks() throws IOException {
		SoneTextParser soneTextParser = new SoneTextParser(new TestSoneProvider(), null);
		Iterable<Part> parts;

		/* check basic links. */
		parts = soneTextParser.parse("Some text.\n\nLink to sone://DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU and stuff.", null);
		assertNotNull("Parts", parts);
		assertEquals("Part Text", "Some text.\n\nLink to [Sone|DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU] and stuff.", convertText(parts, PlainTextPart.class, SonePart.class));
	}

	/**
	 * Test for a bug discovered in Sone 0.8.4 where a plain “http://” would be
	 * parsed into a link.
	 *
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@SuppressWarnings({ "synthetic-access", "static-method" })
	public void testEmpyHttpLinks() throws IOException {
		SoneTextParser soneTextParser = new SoneTextParser(new TestSoneProvider(), null);
		Iterable<Part> parts;

		/* check empty http links. */
		parts = soneTextParser.parse("Some text. Empty link: http:// – nice!", null);
		assertNotNull("Parts", parts);
		assertEquals("Part Text", "Some text. Empty link: http:// – nice!", convertText(parts, PlainTextPart.class));
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
	private static String convertText(Iterable<Part> parts, Class<?>... validClasses) {
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
				fail("Part’s Class (" + part.getClass() + ") is not one of " + Arrays.toString(validClasses));
			}
			if (part instanceof PlainTextPart) {
				text.append(((PlainTextPart) part).getText());
			} else if (part instanceof FreenetLinkPart) {
				FreenetLinkPart freenetLinkPart = (FreenetLinkPart) part;
				text.append('[').append(freenetLinkPart.getLink()).append('|').append(freenetLinkPart.isTrusted() ? "trusted|" : "").append(freenetLinkPart.getTitle()).append('|').append(freenetLinkPart.getText()).append(']');
			} else if (part instanceof LinkPart) {
				LinkPart linkPart = (LinkPart) part;
				text.append('[').append(linkPart.getLink()).append('|').append(linkPart.getTitle()).append('|').append(linkPart.getText()).append(']');
			} else if (part instanceof SonePart) {
				SonePart sonePart = (SonePart) part;
				text.append("[Sone|").append(sonePart.getSone().getId()).append(']');
			}
		}
		return text.toString();
	}

	/**
	 * Mock Sone provider.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class TestSoneProvider implements SoneProvider {

		@Override
		public Function<String, Optional<Sone>> soneLoader() {
			return new Function<String, Optional<Sone>>() {
				@Override
				public Optional<Sone> apply(String soneId) {
					return getSone(soneId);
				}
			};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Optional<Sone> getSone(final String soneId) {
			return Optional.<Sone>of(new IdOnlySone(soneId));
		}

		/**
		 * {@inheritDocs}
		 */
		@Override
		public Collection<Sone> getSones() {
			return null;
		}

		/**
		 * {@inheritDocs}
		 */
		@Override
		public Collection<Sone> getLocalSones() {
			return null;
		}

		/**
		 * {@inheritDocs}
		 */
		@Override
		public Collection<Sone> getRemoteSones() {
			return null;
		}

	}

}
