package net.pterodactylus.sone.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * Unit test for [StringsKt].
 */
class StringsTest {

	@Test
	fun `non-empty string is returned as-is`() {
		assertThat("non-empty".emptyToNull, equalTo("non-empty"))
	}

	@Test
	fun `string with whitespace only is returned as null`() {
		assertThat("   ".emptyToNull, nullValue())
	}

	@Test
	fun `zero-length string is returned as null`() {
		assertThat("".emptyToNull, nullValue())
	}

	@Test
	fun `null is returned as null`() {
		assertThat(null.emptyToNull, nullValue())
	}

}
