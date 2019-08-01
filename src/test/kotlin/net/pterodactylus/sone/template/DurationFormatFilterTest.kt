/**
 * Sone - DurationFormatFilterTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.template

import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

class DurationFormatFilterTest {

	private val filter = DurationFormatFilter()

	@Test
	fun `random object is returned as it is`() {
		val randomObject = Any()
		assertThat(filter.format(null, randomObject, emptyMap()), sameInstance(randomObject))
	}

	@Test
	fun `integer 0 is rendered as “0s”`() {
		verifyDuration(0, "0s")
	}

	@Test
	fun `long 0 is rendered as “0s”`() {
		verifyDuration(0L, "0s")
	}

	@Test
	fun `12 is rendered as “12_0s”`() {
		verifyDuration(12, "12.0s")
	}

	@Test
	fun `123 is rendered as “2_1m”`() {
		verifyDuration(123, "2.1m")
	}

	@Test
	fun `12345 is rendered as “3_4h”`() {
		verifyDuration(12345, "3.4h")
	}

	@Test
	fun `123456 is rendered as “1_4d”`() {
		verifyDuration(123456, "1.4d")
	}

	@Test
	fun `1234567 is rendered as “2_0w”`() {
		verifyDuration(1234567, "2.0w")
	}

	@Test
	fun `123456789 with scale ms is rendered as “1_4d”`() {
		verifyDuration(123456789, "1.4d", "ms")
	}

	@Test
	fun `123456789 with scale μs is rendered as “2_1m”`() {
		verifyDuration(123456789, "2.1m", "μs")
	}

	@Test
	fun `123456789 with scale ns is rendered as “123_5ms”`() {
		verifyDuration(123456789, "123.5ms", "ns")
	}

	@Test
	fun `123456 with scale ns is rendered as “123_5μs”`() {
		verifyDuration(123456, "123.5μs", "ns")
	}

	@Test
	fun `123 with scale ns is rendered as “123_0ns”`() {
		verifyDuration(123, "123.0ns", "ns")
	}

	private fun verifyDuration(value: Any, expectedRendering: String, scale: String? = null) {
		assertThat(filter.format(null, value, scale?.let { mapOf("scale" to scale) } ?: emptyMap()), equalTo<Any>(expectedRendering))
	}

}
