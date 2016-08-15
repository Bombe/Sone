/*
 * Sone - SoneTextParserTest.java - Copyright © 2011–2016 David Roden
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.Collection;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.impl.IdOnlySone;
import net.pterodactylus.sone.database.SoneProvider;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.junit.Test;

/**
 * JUnit test case for {@link SoneTextParser}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneTextParserTest {

	@SuppressWarnings("static-method")
	@Test
	public void testPlainText() throws IOException {
		SoneTextParser soneTextParser = new SoneTextParser(null, null);
		Iterable<Part> parts;

		/* check basic operation. */
		parts = soneTextParser.parse("Test.", null);
		assertThat("Parts", parts, notNullValue());
		assertThat("Part Text", "Test.", is(convertText(parts, PlainTextPart.class)));

		/* check empty lines at start and end. */
		parts = soneTextParser.parse("\nTest.\n\n", null);
		assertThat("Parts", parts, notNullValue());
		assertThat("Part Text", "Test.", is(convertText(parts, PlainTextPart.class)));

		/* check duplicate empty lines in the text. */
		parts = soneTextParser.parse("\nTest.\n\n\nTest.", null);
		assertThat("Parts", parts, notNullValue());
		assertThat("Part Text", "Test.\n\nTest.", is(convertText(parts, PlainTextPart.class)));
	}

	@SuppressWarnings("static-method")
	@Test
	public void testKSKLinks() throws IOException {
		SoneTextParser soneTextParser = new SoneTextParser(null, null);
		Iterable<Part> parts;

		/* check basic links. */
		parts = soneTextParser.parse("KSK@gpl.txt", null);
		assertThat("Parts", parts, notNullValue());
		assertThat("Part Text", "[KSK@gpl.txt|gpl.txt|gpl.txt]", is(convertText(parts, FreenetLinkPart.class)));

		/* check embedded links. */
		parts = soneTextParser.parse("Link is KSK@gpl.txt\u200b.", null);
		assertThat("Parts", parts, notNullValue());
		assertThat("Part Text", "Link is [KSK@gpl.txt|gpl.txt|gpl.txt]\u200b.", is(convertText(parts, PlainTextPart.class, FreenetLinkPart.class)));

		/* check embedded links and line breaks. */
		parts = soneTextParser.parse("Link is KSK@gpl.txt\nKSK@test.dat\n", null);
		assertThat("Parts", parts, notNullValue());
		assertThat("Part Text", "Link is [KSK@gpl.txt|gpl.txt|gpl.txt]\n[KSK@test.dat|test.dat|test.dat]", is(convertText(parts, PlainTextPart.class, FreenetLinkPart.class)));
	}

	@SuppressWarnings({ "synthetic-access", "static-method" })
	@Test
	public void testEmptyLinesAndSoneLinks() throws IOException {
		SoneTextParser soneTextParser = new SoneTextParser(new TestSoneProvider(), null);
		Iterable<Part> parts;

		/* check basic links. */
		parts = soneTextParser.parse("Some text.\n\nLink to sone://DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU and stuff.", null);
		assertThat("Parts", parts, notNullValue());
		assertThat("Part Text", "Some text.\n\nLink to [Sone|DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU] and stuff.", is(convertText(parts, PlainTextPart.class, SonePart.class)));
	}

	@SuppressWarnings({ "synthetic-access", "static-method" })
	@Test
	public void testEmpyHttpLinks() throws IOException {
		SoneTextParser soneTextParser = new SoneTextParser(new TestSoneProvider(), null);
		Iterable<Part> parts;

		/* check empty http links. */
		parts = soneTextParser.parse("Some text. Empty link: http:// – nice!", null);
		assertThat("Parts", parts, notNullValue());
		assertThat("Part Text", "Some text. Empty link: http:// – nice!", is(convertText(parts, PlainTextPart.class)));
	}


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
			assertThat("Part", part, notNullValue());
			if (validClasses.length != 0) {
				assertThat("Part’s class", part.getClass(), isIn(validClasses));
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
