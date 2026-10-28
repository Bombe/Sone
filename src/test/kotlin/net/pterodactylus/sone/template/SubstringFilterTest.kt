package net.pterodactylus.sone.template

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [SubstringFilter].
 */
class SubstringFilterTest {

	private val filter = SubstringFilter()
	private val string = "abcdefghijklmnopqrstuvwxyz"

	private fun filterText(vararg parameters: Pair<String, Int>) = filter.format(null, string, mapOf(*parameters))

	@Test
	fun `filter returns the input string when no parameters are given`() {
		assertThat(filterText(), equalTo<Any>(string))
	}

	@Test
	fun `filter returns 'abc' if start is omitted and length is three`() {
	    assertThat(filterText("length" to 3), equalTo<Any>("abc"))
	}

	@Test
	fun `filter returns complete string if length is larger than length of string`() {
	    assertThat(filterText("length" to 3000), equalTo<Any>(string))
	}

	@Test
	fun `filter returns part of the string if start is set to index within string`() {
	    assertThat(filterText("start" to 13), equalTo<Any>("nopqrstuvwxyz"))
	}

	@Test
	fun `filter returns last three characters if start is set to minus three`() {
		assertThat(filterText("start" to -3), equalTo<Any>("xyz"))
	}

	@Test
	fun `filter returns center part of string with start and length set`() {
	    assertThat(filterText("start" to 13, "length" to 3), equalTo<Any>("nop"))
	}

	@Test
	fun `filter returns end part of string with start and too-large length set`() {
	    assertThat(filterText("start" to 23, "length" to 30), equalTo<Any>("xyz"))
	}

	@Test
	fun `filter returns end part of string with negative start and too-large length set`() {
	    assertThat(filterText("start" to -3, "length" to 30), equalTo<Any>("xyz"))
	}

	@Test
	fun `filter returns part of end of string with negative start and small length set`() {
	    assertThat(filterText("start" to -6, "length" to 3), equalTo<Any>("uvw"))
	}

}
