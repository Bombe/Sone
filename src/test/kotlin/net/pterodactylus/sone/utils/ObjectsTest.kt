package net.pterodactylus.sone.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
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

}
