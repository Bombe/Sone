/*
 * Sone - SoneTextParserTest.kt - Copyright © 2011–2020 David Roden
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

package net.pterodactylus.sone.text

import com.google.inject.Guice.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * JUnit test case for [SoneTextParser].
 */
class SoneTextParserTest {

	private val soneTextParser = SoneTextParser(null, null)

	@Test
	fun `basic operation`() {
		val parts = soneTextParser.parse("Test.", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java), equalTo("Test."))
	}

	@Test
	fun `empty lines at start and end are stripped`() {
		val parts = soneTextParser.parse("\nTest.\n\n", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java), equalTo("Test."))
	}

	@Test
	fun `duplicate empty lines in the text are stripped`() {
		val parts = soneTextParser.parse("\nTest.\n\n\nTest.", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java), equalTo("Test.\n\nTest."))
	}

	@Test
	fun `consecutive lines are separated by linefeed`() {
		val parts = soneTextParser.parse("Text.\nText", null)
		assertThat("Part Text", convertText(parts), equalTo("Text.\nText"))
	}

	@Test
	fun `freenet links have the freenet prefix removed`() {
		val parts = soneTextParser.parse("freenet:KSK@gpl.txt", null)
		assertThat("Part Text", convertText(parts), equalTo("[KSK@gpl.txt|KSK@gpl.txt|gpl.txt]"))
	}

	@Test
	fun `only the first item in a line is prefixed with a line break`() {
		val parts = soneTextParser.parse("Text.\nKSK@gpl.txt and KSK@gpl.txt", null)
		assertThat("Part Text", convertText(parts), equalTo("Text.\n[KSK@gpl.txt|KSK@gpl.txt|gpl.txt] and [KSK@gpl.txt|KSK@gpl.txt|gpl.txt]"))
	}

	@Test
	fun `sone link with too short sone ID is rendered as plain text`() {
		val parts = soneTextParser.parse("sone://too-short", null)
		assertThat("Part Text", convertText(parts), equalTo("sone://too-short"))
	}

	@Test
	fun `sone link is rendered correctly if sone is not present`() {
		val parser = SoneTextParser(AbsentSoneProvider(), null)
		val parts = parser.parse("sone://DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU", null)
		assertThat("Part Text", convertText(parts), equalTo("[Sone|DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU]"))
	}

	@Test
	fun `sone and post can be parsed from the same text`() {
		val parser = SoneTextParser(TestSoneProvider(), TestPostProvider())
		val parts = parser.parse("Text sone://DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU more text post://f3757817-b45a-497a-803f-9c5aafc10dc6 even more text", null)
		assertThat("Part Text", convertText(parts), equalTo("Text [Sone|DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU] more text [Post|f3757817-b45a-497a-803f-9c5aafc10dc6|text] even more text"))
	}

	@Test
	fun `post link is rendered as plain text if post ID is too short`() {
		val parts = soneTextParser.parse("post://too-short", null)
		assertThat("Part Text", convertText(parts), equalTo("post://too-short"))
	}

	@Test
	fun `post link is rendered correctly if post is present`() {
		val parser = SoneTextParser(null, TestPostProvider())
		val parts = parser.parse("post://f3757817-b45a-497a-803f-9c5aafc10dc6", null)
		assertThat("Part Text", convertText(parts), equalTo("[Post|f3757817-b45a-497a-803f-9c5aafc10dc6|text]"))
	}

	@Test
	fun `post link is rendered as plain text if post is absent`() {
		val parser = SoneTextParser(null, AbsentPostProvider())
		val parts = parser.parse("post://f3757817-b45a-497a-803f-9c5aafc10dc6", null)
		assertThat("Part Text", convertText(parts), equalTo("post://f3757817-b45a-497a-803f-9c5aafc10dc6"))
	}

	@Test
	fun `name of freenet link does not contain url parameters`() {
		val parts = soneTextParser.parse("KSK@gpl.txt?max-size=12345", null)
		assertThat("Part Text", convertText(parts), equalTo("[KSK@gpl.txt?max-size=12345|KSK@gpl.txt|gpl.txt]"))
	}

	@Test
	fun `trailing slash in freenet link is removed for name`() {
		val parts = soneTextParser.parse("KSK@gpl.txt/", null)
		assertThat("Part Text", convertText(parts), equalTo("[KSK@gpl.txt/|KSK@gpl.txt/|gpl.txt]"))
	}

	@Test
	fun `last meta string of freenet link is used as name`() {
		val parts = soneTextParser.parse("CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/COPYING", null)
		assertThat("Part Text", convertText(parts), equalTo("[CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/COPYING|CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/COPYING|COPYING]"))
	}

	@Test
	fun `freenet link without meta strings and doc name gets first nine characters of key as name`() {
		val parts = soneTextParser.parse("CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8", null)
		assertThat("Part Text", convertText(parts), equalTo("[CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8|CHK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8|CHK@qM1nm]"))
	}

	@Test
	fun `malformed key is rendered as plain text`() {
		val parts = soneTextParser.parse("CHK@qM1nmgU", null)
		assertThat("Part Text", convertText(parts), equalTo("CHK@qM1nmgU"))
	}

	@Test
	fun `https link has its paths shortened`() {
		val parts = soneTextParser.parse("https://test.test/some-long-path/file.txt", null)
		assertThat("Part Text", convertText(parts), equalTo("[https://test.test/some-long-path/file.txt|https://test.test/some-long-path/file.txt|test.test/…/file.txt]"))
	}

	@Test
	fun `http links have their last slash removed`() {
		val parts = soneTextParser.parse("http://test.test/test/", null)
		assertThat("Part Text", convertText(parts), equalTo("[http://test.test/test/|http://test.test/test/|test.test/…]"))
	}

	@Test
	fun `www prefix is removed for hostname with two dots and no path`() {
		val parts = soneTextParser.parse("http://www.test.test", null)
		assertThat("Part Text", convertText(parts), equalTo("[http://www.test.test|http://www.test.test|test.test]"))
	}

	@Test
	fun `www prefix is removed for hostname with two dots and a path`() {
		val parts = soneTextParser.parse("http://www.test.test/test.html", null)
		assertThat("Part Text", convertText(parts), equalTo("[http://www.test.test/test.html|http://www.test.test/test.html|test.test/test.html]"))
	}

	@Test
	fun `hostname is kept intact if not beginning with www`() {
		val parts = soneTextParser.parse("http://test.test.test/test.html", null)
		assertThat("Part Text", convertText(parts), equalTo("[http://test.test.test/test.html|http://test.test.test/test.html|test.test.test/test.html]"))
	}

	@Test
	fun `hostname with one dot but no slash is kept intact`() {
		val parts = soneTextParser.parse("http://test.test", null)
		assertThat("Part Text", convertText(parts), equalTo("[http://test.test|http://test.test|test.test]"))
	}

	@Test
	fun `url parameters are removed for http links`() {
		val parts = soneTextParser.parse("http://test.test?foo=bar", null)
		assertThat("Part Text", convertText(parts), equalTo("[http://test.test?foo=bar|http://test.test?foo=bar|test.test]"))
	}

	@Test
	fun `empty string is parsed correctly`() {
		val parts = soneTextParser.parse("", null)
		assertThat("Part Text", convertText(parts), equalTo(""))
	}

	@Test
	fun `links are parsed in correct order`() {
		val parts = soneTextParser.parse("KSK@ CHK@", null)
		assertThat("Part Text", convertText(parts), equalTo("KSK@ CHK@"))
	}

	@Test
	fun `invalid ssk and usk link is parsed as text`() {
		val parts = soneTextParser.parse("SSK@a USK@a", null)
		assertThat("Part Text", convertText(parts), equalTo("SSK@a USK@a"))
	}

	@Test
	fun `ssk without document name is parsed correctly`() {
		val parts = soneTextParser.parse(
				"SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8", null)
		assertThat("Part Text", convertText(parts),
				equalTo("[SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8|"
						+ "SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8|"
						+ "SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU]"))
	}

	@Test
	fun `ssk link without context is not trusted`() {
		val parts = soneTextParser.parse("SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test", null)
		assertThat("Part Text", convertText(parts), equalTo("[SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|test]"))
	}

	@Test
	fun `ssk link with context without sone is not trusted`() {
		val context = SoneTextParserContext(null)
		val parts = soneTextParser.parse("SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test", context)
		assertThat("Part Text", convertText(parts), equalTo("[SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|test]"))
	}

	@Test
	fun `ssk link with context with different sone is not trusted`() {
		val context = SoneTextParserContext(IdOnlySone("DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU"))
		val parts = soneTextParser.parse("SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test", context)
		assertThat("Part Text", convertText(parts), equalTo("[SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|test]"))
	}

	@Test
	fun `ssk link with context with correct sone is trusted`() {
		val context = SoneTextParserContext(IdOnlySone("qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU"))
		val parts = soneTextParser.parse("SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test", context)
		assertThat("Part Text", convertText(parts), equalTo("[SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|trusted|SSK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test|test]"))
	}

	@Test
	fun `usk link with context with correct sone is trusted`() {
		val context = SoneTextParserContext(IdOnlySone("qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU"))
		val parts = soneTextParser.parse("USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0", context)
		assertThat("Part Text", convertText(parts), equalTo("[USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|trusted|USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|test]"))
	}

	@Test
	fun `usk links with backlinks is parsed correctly`() {
		val context = SoneTextParserContext(IdOnlySone("qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU"))
		val parts = soneTextParser.parse("USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0/../../../USK@nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI,DuQSUZiI~agF8c-6tjsFFGuZ8eICrzWCILB60nT8KKo,AQACAAE/sone/78/", context)
		assertThat("Part Text", convertText(parts), equalTo("[USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|trusted|USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|test]"))
	}

	@Test
	fun `test basic ksk links`() {
		val parts: Iterable<Part> = soneTextParser.parse("KSK@gpl.txt", null)
		assertThat("Part Text", convertText(parts, FreenetLinkPart::class.java), equalTo("[KSK@gpl.txt|KSK@gpl.txt|gpl.txt]"))
	}

	@Test
	fun `embedded ksk links are parsed correctly`() {
		val parts = soneTextParser.parse("Link is KSK@gpl.txt\u200b.", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java, FreenetLinkPart::class.java), equalTo("Link is [KSK@gpl.txt|KSK@gpl.txt|gpl.txt]\u200b."))
	}

	@Test
	fun `embedded ksk links and line breaks are parsed correctly`() {
		val parts = soneTextParser.parse("Link is KSK@gpl.txt\nKSK@test.dat\n", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java, FreenetLinkPart::class.java), equalTo("Link is [KSK@gpl.txt|KSK@gpl.txt|gpl.txt]\n[KSK@test.dat|KSK@test.dat|test.dat]"))
	}

	@Test
	fun `ksk links with backlinks are parsed correctly`() {
		val parts = soneTextParser.parse("KSK@gallery/../Sone/imageBrowser.html?album=30c930ee-97cd-11e9-bd44-f3e595768b77", null)
		assertThat("Part Text", convertText(parts, FreenetLinkPart::class.java), equalTo("[KSK@gallery|KSK@gallery|gallery]"))
	}

	@Test
	fun `test empty lines and sone links`() {
		val soneTextParser = SoneTextParser(TestSoneProvider(), null)

		/* check basic links. */
		val parts = soneTextParser.parse("Some text.\n\nLink to sone://DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU and stuff.", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java, SonePart::class.java), equalTo("Some text.\n\nLink to [Sone|DAxKQzS48mtaQc7sUVHIgx3fnWZPQBz0EueBreUVWrU] and stuff."))
	}

	@Test
	fun `test empy http links`() {
		val soneTextParser = SoneTextParser(TestSoneProvider(), null)

		/* check empty http links. */
		val parts = soneTextParser.parse("Some text. Empty link: http:// – nice!", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java), equalTo("Some text. Empty link: http:// – nice!"))
	}

	@Test
	fun `http link without parens ends at next closing paren`() {
		val parts = soneTextParser.parse("Some text (and a link: http://example.sone/abc) – nice!", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java, LinkPart::class.java), equalTo("Some text (and a link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc]) – nice!"))
	}

	@Test
	fun `usk link ends at first non numeric non slash character after version number`() {
		val parts = soneTextParser.parse("Some link (USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0). Nice", null)
		assertThat("Part Text", convertText(parts), equalTo("Some link ([USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|test]). Nice"))
	}

	@Test
	fun `usk link with filename shows the filename`() {
		val parts = soneTextParser.parse("Some link (USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0/images/image.jpg). Nice", null)
		assertThat("Part Text", convertText(parts), equalTo("Some link ([USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0/images/image.jpg|USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0/images/image.jpg|image.jpg]). Nice"))
	}

	@Test
	fun `usk link without filename but ending in slash shows the path`() {
		val parts = soneTextParser.parse("Some link (USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0/). Nice", null)
		assertThat("Part Text", convertText(parts), equalTo("Some link ([USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|USK@qM1nmgU-YUnIttmEhqjTl7ifAF3Z6o~5EPwQW03uEQU,aztSUkT-VT1dWvfSUt9YpfyW~Flmf5yXpBnIE~v8sAg,AAMC--8/test/0|test]). Nice"))
	}

	@Test
	fun `http link with opened and closed parens ends at next closing paren`() {
		val parts = soneTextParser.parse("Some text (and a link: http://example.sone/abc_(def)) – nice!", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java, LinkPart::class.java), equalTo("Some text (and a link: [http://example.sone/abc_(def)|http://example.sone/abc_(def)|example.sone/abc_(def)]) – nice!"))
	}

	@Test
	fun `punctuation is ignored at end of link before whitespace`() {
		val parts = soneTextParser.parse("Some text and a link: http://example.sone/abc. Nice!", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java, LinkPart::class.java), equalTo("Some text and a link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc]. Nice!"))
	}

	@Test
	fun `multiple punctuation characters are ignored at end of link before whitespace`() {
		val parts = soneTextParser.parse("Some text and a link: http://example.sone/abc... Nice!", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java, LinkPart::class.java), equalTo("Some text and a link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc]... Nice!"))
	}

	@Test
	fun `commas are ignored at end of link before whitespace`() {
		val parts = soneTextParser.parse("Some text and a link: http://example.sone/abc, nice!", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java, LinkPart::class.java), equalTo("Some text and a link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc], nice!"))
	}

	@Test
	fun `exclamation marks are ignored at end of link before whitespace`() {
		val parts = soneTextParser.parse("A link: http://example.sone/abc!", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java, LinkPart::class.java), equalTo("A link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc]!"))
	}

	@Test
	fun `question marks are ignored at end of link before whitespace`() {
		val parts = soneTextParser.parse("A link: http://example.sone/abc?", null)
		assertThat("Part Text", convertText(parts, PlainTextPart::class.java, LinkPart::class.java), equalTo("A link: [http://example.sone/abc|http://example.sone/abc|example.sone/abc]?"))
	}

	@Test
	fun `correct freemail address is linked to correctly`() {
		val parts = soneTextParser.parse("Mail me at sone@t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail!", null)
		assertThat("Part Text", convertText(parts), equalTo("Mail me at [Freemail|sone|t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra|nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI]!"))
	}

	@Test
	fun `freemail address with invalid freemail id is parsed as text`() {
		val parts = soneTextParser.parse("Mail me at sone@t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqr8.freemail!", null)
		assertThat("Part Text", convertText(parts), equalTo("Mail me at sone@t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqr8.freemail!"))
	}

	@Test
	fun `freemail address with invalid sized freemail id is parsed as text`() {
		val parts = soneTextParser.parse("Mail me at sone@4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail!", null)
		assertThat("Part Text", convertText(parts), equalTo("Mail me at sone@4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail!"))
	}

	@Test
	fun `freemail address without local part is parsed as text`() {
		val parts = soneTextParser.parse("     @t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail!", null)
		assertThat("Part Text", convertText(parts), equalTo("     @t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail!"))
	}

	@Test
	fun `local part of freemail address can contain letters digits minus dot underscore`() {
		val parts = soneTextParser.parse("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._@t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra.freemail", null)
		assertThat("Part Text", convertText(parts), equalTo("[Freemail|ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._|t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra|nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI]"))
	}

	private fun convertText(parts: Iterable<Part>, vararg validClasses: Class<*>): String {
		if (validClasses.isNotEmpty()) {
			assertThat(parts.map { it.javaClass }.distinct() - validClasses.distinct(), empty())
		}
		return parts.joinToString("") { part ->
			when (part) {
				is PlainTextPart -> part.text
				is FreenetLinkPart -> "[${part.link}|${if (part.trusted) "trusted|" else ""}${part.title}|${part.text}]"
				is FreemailPart -> "[Freemail|${part.emailLocalPart}|${part.freemailId}|${part.identityId}]"
				is LinkPart -> "[${part.link}|${part.title}|${part.text}]"
				is SonePart -> "[Sone|${part.sone.id}]"
				is PostPart -> "[Post|${part.post.id}|${part.post.text}]"
				else -> throw NoSuchElementException()
			}
		}
	}

	@Test
	fun `parser can be created by guice`() {
		val injector = createInjector(
				SoneProvider::class.isProvidedByMock(),
				PostProvider::class.isProvidedByMock()
		)
		assertThat(injector.getInstance<SoneTextParser>(), notNullValue())
	}

	/**
	 * Mock Sone provider.
	 */
	private open class TestSoneProvider : SoneProvider {

		override val soneLoader = this::getSone
		override val sones: Collection<Sone> = emptySet()
		override val localSones: Collection<Sone> = emptySet()
		override val remoteSones: Collection<Sone> = emptySet()

		override fun getSone(soneId: String): Sone? = IdOnlySone(soneId)

	}

	private class AbsentSoneProvider : TestSoneProvider() {

		override fun getSone(soneId: String): Sone? = null

	}

	private open class TestPostProvider : PostProvider {

		override fun getPost(postId: String): Post? {
			return object : Post {
				override val id = postId
				override fun isLoaded() = false
				override fun getSone() = null
				override fun getRecipientId() = null
				override fun getRecipient() = null
				override fun getTime() = 0L
				override fun getText() = "text"
				override fun isKnown() = false
				override fun setKnown(known: Boolean) = null
			}
		}

		override fun getPosts(soneId: String) = emptySet<Post>()
		override fun getDirectedPosts(recipientId: String) = emptySet<Post>()

	}

	private class AbsentPostProvider : TestPostProvider() {

		override fun getPost(postId: String): Post? = null

	}

}
