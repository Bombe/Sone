package net.pterodactylus.sone.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * Unit test for [Booleans].
 */
class BooleansTest {

	@Test
	fun `ifTrue is executed if boolean is true`() {
		assertThat(true.ifTrue { true }, equalTo(true))
	}

	@Test
	fun `ifTrue is not executed if boolean is false`() {
		assertThat(false.ifTrue { true }, nullValue())
	}

	@Test
	fun `ifFalse is executed if boolean is false`() {
		assertThat(false.ifFalse { true }, equalTo(true))
	}

	@Test
	fun `ifFalse is not executed if boolean is true`() {
		assertThat(true.ifFalse { true }, nullValue())
	}

}
