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

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.Collection;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.impl.IdOnlySone;
import net.pterodactylus.sone.database.PostProvider;
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

	private final SoneTextParser soneTextParser = new SoneTextParser(null, null);

	@SuppressWarnings("static-method")
	@Test
	public void testPlainText() throws IOException {
		/* check basic operation. */
		Iterable<Part> parts = soneTextParser.parse("Test.", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class), is("Test."));

		/* check empty lines at start and end. */
		parts = soneTextParser.parse("\nTest.\n\n", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class), is("Test."));

		/* check duplicate empty lines in the text. */
		parts = soneTextParser.parse("\nTest.\n\n\nTest.", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class), is("Test.\n\nTest."));
	}

	@Test
	public void consecutiveLinesAreSeparatedByLinefeed() {
		Iterable<Part> parts = soneTextParser.parse("Text.\nText", null);
		assertThat("Part Text", convertText(parts), is("Text.\nText"));
	}

	@Test
	public void freenetLinksHaveTheFreenetPrefixRemoved() {
		Iterable<Part> parts = soneTextParser.parse("freenet:KSK@gpl.txt", null);
		assertThat("Part Text", convertText(parts), is("[KSK@gpl.txt|KSK@gpl.txt|gpl.txt]"));
	}

	@Test
	public void onlyTheFirstItemInALineIsPrefixedWithALineBreak() {
		Iterable<Part> parts = soneTextParser.parse("Text.\nKSK@gpl.txt and KSK@gpl.txt", null);
		assertThat("Part Text", convertText(parts), is("Text.\n[KSK@gpl.txt|KSK@gpl.txt|gpl.txt] and [KSK@gpl.txt|KSK@gpl.txt|gpl.txt]"));
	}

	@Test
	public void soneLinkWithTooShortSoneIdIsRenderedAsPlainText() {
		Iterable<Part> parts = soneTextParser.parse("sone://too-short", null);
		assertThat("Part Text", convertText(parts), is("sone://too-short"));
	}

	@Test
	public void soneLinkIsRenderedCorrectlyIfSoneIsNotPresent() {
		SoneTextParser parser = new SoneTextParser(new AbsentSoneProvider(), null);
		Iterable<Part> parts = parser.parse("sone://DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU", null);
		assertThat("Part Text", convertText(parts), is("[Sone|DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU]"));
	}

	@Test
	public void soneAndPostCanBeParsedFromTheSameText() {
		SoneTextParser parser = new SoneTextParser(new TestSoneProvider(), new TestPostProvider());
		Iterable<Part> parts = parser.parse("Text sone://DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU more text post://f3757817-b45a-497a-803f-9c5aafc10dc6 even more text", null);
		assertThat("Part Text", convertText(parts), is("Text [Sone|DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU] more text [Post|f3757817-b45a-497a-803f-9c5aafc10dc6|text] even more text"));
	}

	@Test
	public void postLinkIsRenderedAsPlainTextIfPostIdIsTooShort() {
		Iterable<Part> parts = soneTextParser.parse("post://too-short", null);
		assertThat("Part Text", convertText(parts), is("post://too-short"));
	}

	@Test
	public void postLinkIsRenderedCorrectlyIfPostIsPresent() {
		SoneTextParser parser = new SoneTextParser(null, new TestPostProvider());
		Iterable<Part> parts = parser.parse("post://f3757817-b45a-497a-803f-9c5aafc10dc6", null);
		assertThat("Part Text", convertText(parts), is("[Post|f3757817-b45a-497a-803f-9c5aafc10dc6|text]"));
	}

	@Test
	public void postLinkIsRenderedAsPlainTextIfPostIsAbsent() {
		SoneTextParser parser = new SoneTextParser(null, new AbsentPostProvider());
		Iterable<Part> parts = parser.parse("post://f3757817-b45a-497a-803f-9c5aafc10dc6", null);
		assertThat("Part Text", convertText(parts), is("post://f3757817-b45a-497a-803f-9c5aafc10dc6"));
	}

	@Test
	public void nameOfFreenetLinkDoesNotContainUrlParameters() {
		Iterable<Part> parts = soneTextParser.parse("KSK@gpl.txt?max-size=12345", null);
		assertThat("Part Text", convertText(parts), is("[KSK@gpl.txt?max-size=12345|KSK@gpl.txt|gpl.txt]"));
	}

	@Test
	public void trailingSlashInFreenetLinkIsRemovedForName() {
		Iterable<Part> parts = soneTextParser.parse("KSK@gpl.txt/", null);
		assertThat("Part Text", convertText(parts), is("[KSK@gpl.txt/|KSK@gpl.txt/|gpl.txt]"));
	}

	@Test
	public void lastMetaStringOfFreenetLinkIsUsedAsName() {
		Iterable<Part> parts = soneTextParser.parse("CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/COPYING", null);
		assertThat("Part Text", convertText(parts), is("[CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/COPYING|CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/COPYING|COPYING]"));
	}

	@Test
	public void freenetLinkWithoutMetaStringsAndDocNameGetsFirstNineCharactersOfKeyAsName() {
		Iterable<Part> parts = soneTextParser.parse("CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8", null);
		assertThat("Part Text", convertText(parts), is("[CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8|CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8|CHK@qM1nm]"));
	}

	@Test
	public void malformedKeyIsRenderedAsPlainText() {
		Iterable<Part> parts = soneTextParser.parse("CHK@qM1nmgU", null);
		assertThat("Part Text", convertText(parts), is("CHK@qM1nmgU"));
	}

	@Test
	public void httpsLinkHasItsPathsShortened() {
		Iterable<Part> parts = soneTextParser.parse("https://test.test/some-long-path/file.txt", null);
		assertThat("Part Text", convertText(parts), is("[https://test.test/some-long-path/file.txt|https://test.test/some-long-path/file.txt|test.test/…/file.txt]"));
	}

	@Test
	public void httpLinksHaveTheirLastSlashRemoved() {
		Iterable<Part> parts = soneTextParser.parse("http://test.test/test/", null);
		assertThat("Part Text", convertText(parts), is("[http://test.test/test/|http://test.test/test/|test.test/…]"));
	}

	@Test
	public void wwwPrefixIsRemovedForHostnameWithTwoDotsAndNoPath() {
		Iterable<Part> parts = soneTextParser.parse("http://www.test.test", null);
		assertThat("Part Text", convertText(parts), is("[http://www.test.test|http://www.test.test|test.test]"));
	}

	@Test
	public void wwwPrefixIsRemovedForHostnameWithTwoDotsAndAPath() {
		Iterable<Part> parts = soneTextParser.parse("http://www.test.test/test.html", null);
		assertThat("Part Text", convertText(parts), is("[http://www.test.test/test.html|http://www.test.test/test.html|test.test/test.html]"));
	}

	@Test
	public void hostnameIsKeptIntactIfNotBeginningWithWww() {
		Iterable<Part> parts = soneTextParser.parse("http://test.test.test/test.html", null);
		assertThat("Part Text", convertText(parts), is("[http://test.test.test/test.html|http://test.test.test/test.html|test.test.test/test.html]"));
	}

	@Test
	public void hostnameWithOneDotButNoSlashIsKeptIntact() {
		Iterable<Part> parts = soneTextParser.parse("http://test.test", null);
		assertThat("Part Text", convertText(parts), is("[http://test.test|http://test.test|test.test]"));
	}

	@Test
	public void urlParametersAreRemovedForHttpLinks() {
		Iterable<Part> parts = soneTextParser.parse("http://test.test?foo=bar", null);
		assertThat("Part Text", convertText(parts), is("[http://test.test?foo=bar|http://test.test?foo=bar|test.test]"));
	}

	@Test
	public void emptyStringIsParsedCorrectly() {
		Iterable<Part> parts = soneTextParser.parse("", null);
		assertThat("Part Text", convertText(parts), is(""));
	}

	@Test
	public void linksAreParsedInCorrectOrder() {
		Iterable<Part> parts = soneTextParser.parse("KSK@ CHK@", null);
		assertThat("Part Text", convertText(parts), is("KSK@ CHK@"));
	}

	@Test
	public void invalidSskAndUskLinkIsParsedAsText() {
		Iterable<Part> parts = soneTextParser.parse("SSK@a USK@a", null);
		assertThat("Part Text", convertText(parts), is("SSK@a USK@a"));
	}

	@Test
	public void sskLinkWithoutContextIsNotTrusted() {
		Iterable<Part> parts = soneTextParser.parse("SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test", null);
		assertThat("Part Text", convertText(parts), is("[SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|test]"));
	}

	@Test
	public void sskLinkWithContextWithoutSoneIsNotTrusted() {
		SoneTextParserContext context = new SoneTextParserContext(null);
		Iterable<Part> parts = soneTextParser.parse("SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test", context);
		assertThat("Part Text", convertText(parts), is("[SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|test]"));
	}

	@Test
	public void sskLinkWithContextWithDifferentSoneIsNotTrusted() {
		SoneTextParserContext context = new SoneTextParserContext(new IdOnlySone("DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU"));
		Iterable<Part> parts = soneTextParser.parse("SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test", context);
		assertThat("Part Text", convertText(parts), is("[SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|test]"));
	}

	@Test
	public void sskLinkWithContextWithCorrectSoneIsTrusted() {
		SoneTextParserContext context = new SoneTextParserContext(new IdOnlySone("qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU"));
		Iterable<Part> parts = soneTextParser.parse("SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test", context);
		assertThat("Part Text", convertText(parts), is("[SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|trusted|SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|test]"));
	}

	@Test
	public void uskLinkWithContextWithCorrectSoneIsTrusted() {
		SoneTextParserContext context = new SoneTextParserContext(new IdOnlySone("qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU"));
		Iterable<Part> parts = soneTextParser.parse("USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0", context);
		assertThat("Part Text", convertText(parts), is("[USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|trusted|USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|test]"));
	}

	@SuppressWarnings("static-method")
	@Test
	public void testKSKLinks() throws IOException {
		/* check basic links. */
		Iterable<Part> parts = soneTextParser.parse("KSK@gpl.txt", null);
		assertThat("Part Text", convertText(parts, FreenetLinkPart.class), is("[KSK@gpl.txt|KSK@gpl.txt|gpl.txt]"));

		/* check embedded links. */
		parts = soneTextParser.parse("Link is KSK@gpl.txt\u200b.", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class, FreenetLinkPart.class), is("Link is [KSK@gpl.txt|KSK@gpl.txt|gpl.txt]\u200b."));

		/* check embedded links and line breaks. */
		parts = soneTextParser.parse("Link is KSK@gpl.txt\nKSK@test.dat\n", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class, FreenetLinkPart.class), is("Link is [KSK@gpl.txt|KSK@gpl.txt|gpl.txt]\n[KSK@test.dat|KSK@test.dat|test.dat]"));
	}

	@SuppressWarnings({ "synthetic-access", "static-method" })
	@Test
	public void testEmptyLinesAndSoneLinks() throws IOException {
		SoneTextParser soneTextParser = new SoneTextParser(new TestSoneProvider(), null);

		/* check basic links. */
		Iterable<Part> parts = soneTextParser.parse("Some text.\n\nLink to sone://DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU and stuff.", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class, SonePart.class), is("Some text.\n\nLink to [Sone|DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU] and stuff."));
	}

	@SuppressWarnings({ "synthetic-access", "static-method" })
	@Test
	public void testEmpyHttpLinks() throws IOException {
		SoneTextParser soneTextParser = new SoneTextParser(new TestSoneProvider(), null);

		/* check empty http links. */
		Iterable<Part> parts = soneTextParser.parse("Some text. Empty link: http:// – nice!", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class), is("Some text. Empty link: http:// – nice!"));
	}

	@Test
	public void httpLinkWithoutParensEndsAtNextClosingParen() {
		Iterable<Part> parts = soneTextParser.parse("Some text (and a link: http://example.sone/abc) – nice!", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class, LinkPart.class), is("Some text (and a link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc]) – nice!"));
	}

	@Test
	public void uskLinkEndsAtFirstNonNumericNonSlashCharacterAfterVersionNumber() {
		Iterable<Part> parts = soneTextParser.parse("Some link (USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0). Nice", null);
		assertThat("Part Text", convertText(parts), is("Some link ([USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|test]). Nice"));
	}

	@Test
	public void httpLinkWithOpenedAndClosedParensEndsAtNextClosingParen() {
		Iterable<Part> parts = soneTextParser.parse("Some text (and a link: http://example.sone/abc_(def)) – nice!", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class, LinkPart.class), is("Some text (and a link: [http://example.sone/abc_(def)|http://example.sone/abc_(def)|example.sone/abc_(def)]) – nice!"));
	}

	@Test
	public void punctuationIsIgnoredAtEndOfLinkBeforeWhitespace() {
		Iterable<Part> parts = soneTextParser.parse("Some text and a link: http://example.sone/abc. Nice!", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class, LinkPart.class), is("Some text and a link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc]. Nice!"));
	}

	@Test
	public void multiplePunctuationCharactersAreIgnoredAtEndOfLinkBeforeWhitespace() {
		Iterable<Part> parts = soneTextParser.parse("Some text and a link: http://example.sone/abc... Nice!", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class, LinkPart.class), is("Some text and a link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc]... Nice!"));
	}

	@Test
	public void commasAreIgnoredAtEndOfLinkBeforeWhitespace() {
		Iterable<Part> parts = soneTextParser.parse("Some text and a link: http://example.sone/abc, nice!", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class, LinkPart.class), is("Some text and a link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc], nice!"));
	}

	@Test
	public void exclamationMarksAreIgnoredAtEndOfLinkBeforeWhitespace() {
		Iterable<Part> parts = soneTextParser.parse("A link: http://example.sone/abc!", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class, LinkPart.class), is("A link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc]!"));
	}

	@Test
	public void questionMarksAreIgnoredAtEndOfLinkBeforeWhitespace() {
		Iterable<Part> parts = soneTextParser.parse("A link: http://example.sone/abc?", null);
		assertThat("Part Text", convertText(parts, PlainTextPart.class, LinkPart.class), is("A link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc]?"));
	}

	@Test
	public void correctFreemailAddressIsLinkedToCorrectly() {
		Iterable<Part> parts = soneTextParser.parse("Mail me at sone@t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail!", null);
		assertThat("Part Text", convertText(parts), is("Mail me at [Freemail|sone|t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra|nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI]!"));
	}

	@Test
	public void freemailAddressWithInvalidFreemailIdIsParsedAsText() {
		Iterable<Part> parts = soneTextParser.parse("Mail me at sone@t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqr8.freemail!", null);
		assertThat("Part Text", convertText(parts), is("Mail me at sone@t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqr8.freemail!"));
	}

	@Test
	public void freemailAddressWithInvalidSizedFreemailIdIsParsedAsText() {
		Iterable<Part> parts = soneTextParser.parse("Mail me at sone@4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail!", null);
		assertThat("Part Text", convertText(parts), is("Mail me at sone@4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail!"));
	}

	@Test
	public void freemailAddressWithoutLocalPartIsParsedAsText() {
		Iterable<Part> parts = soneTextParser.parse("     @t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail!", null);
		assertThat("Part Text", convertText(parts), is("     @t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail!"));
	}

	@Test
	public void correctFreemailAddressIsParsedCorrectly() {
		Iterable<Part> parts = soneTextParser.parse("sone@t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail", null);
		assertThat("Part Text", convertText(parts), is("[Freemail|sone|t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra|nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI]"));
	}

	@Test
	public void localPartOfFreemailAddressCanContainLettersDigitsMinusDotUnderscore() {
		Iterable<Part> parts = soneTextParser.parse("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._@t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail", null);
		assertThat("Part Text", convertText(parts), is("[Freemail|ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._|t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra|nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI]"));
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
				text.append('[').append(freenetLinkPart.getLink()).append('|').append(freenetLinkPart.getTrusted() ? "trusted|" : "").append(freenetLinkPart.getTitle()).append('|').append(freenetLinkPart.getText()).append(']');
			} else if (part instanceof FreemailPart) {
				FreemailPart freemailPart = (FreemailPart) part;
				text.append(format("[Freemail|%s|%s|%s]", freemailPart.getEmailLocalPart(), freemailPart.getFreemailId(), freemailPart.getIdentityId()));
			} else if (part instanceof LinkPart) {
				LinkPart linkPart = (LinkPart) part;
				text.append('[').append(linkPart.getLink()).append('|').append(linkPart.getTitle()).append('|').append(linkPart.getText()).append(']');
			} else if (part instanceof SonePart) {
				SonePart sonePart = (SonePart) part;
				text.append("[Sone|").append(sonePart.getSone().getId()).append(']');
			} else if (part instanceof PostPart) {
				PostPart postPart = (PostPart) part;
				text.append("[Post|").append(postPart.getPost().getId()).append("|").append(postPart.getPost().getText()).append("]");
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

	private static class AbsentSoneProvider extends TestSoneProvider {

		@Override
		public Optional<Sone> getSone(String soneId) {
			return Optional.absent();
		}

	}

	private static class TestPostProvider implements PostProvider {

		@Override
		public Optional<Post> getPost(final String postId) {
			return Optional.<Post>of(new Post() {
				@Override
				public String getId() {
					return postId;
				}

				@Override
				public boolean isLoaded() {
					return false;
				}

				@Override
				public Sone getSone() {
					return null;
				}

				@Override
				public Optional<String> getRecipientId() {
					return null;
				}

				@Override
				public Optional<Sone> getRecipient() {
					return null;
				}

				@Override
				public long getTime() {
					return 0;
				}

				@Override
				public String getText() {
					return "text";
				}

				@Override
				public boolean isKnown() {
					return false;
				}

				@Override
				public Post setKnown(boolean known) {
					return null;
				}
			});
		}

		@Override
		public Collection<Post> getPosts(String soneId) {
			return null;
		}

		@Override
		public Collection<Post> getDirectedPosts(String recipientId) {
			return null;
		}

	}

	private static class AbsentPostProvider extends TestPostProvider {

		@Override
		public Optional<Post> getPost(String postId) {
			return Optional.absent();
		}

	}

}
