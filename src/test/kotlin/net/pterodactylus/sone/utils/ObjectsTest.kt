package net.pterodactylus.sone.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test

/**
 * Unit test for Object utils.
 */
class ObjectsTest {

	@Test
	fun `non-null value is turned into a list with one element`() {
		assertThat(5.asList(), contains(5))
	}

	@Test
	fun `null value is turned into empty list`() {
		assertThat(null.asList(), empty())
	}

	@Test(expected = IllegalArgumentException::class)
	fun `exception is thrown for null and true condition`() {
		null.throwOnNullIf(true) { IllegalArgumentException() }
	}

	@Test
	fun `exception is not thrown for null and false condition`() {
		assertThat(null.throwOnNullIf(false) { IllegalArgumentException() }, nullValue())
	}

	@Test
	fun `exception is not thrown for any and true condition`() {
		val any = Any()
		assertThat(any.throwOnNullIf(true) { IllegalArgumentException() }, equalTo(any))
	}

	@Test
	fun `exception is not thrown for any and false condition`() {
		val any = Any()
		assertThat(any.throwOnNullIf(false) { IllegalArgumentException() }, equalTo(any))
	}

}
