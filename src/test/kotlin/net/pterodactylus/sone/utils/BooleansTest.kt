package net.pterodactylus.sone.utils

import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

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

	@Test
	fun `onTrue returns true on true`() {
		assertThat(true.onTrue {}, equalTo(true))
	}

	@Test
	fun `onTrue returns false on false`() {
		assertThat(false.onTrue {}, equalTo(false))
	}

	@Test
	fun `onTrue is not executed on false`() {
		assertThat(false.onTrue { throw RuntimeException() }, equalTo(false))
	}

	@Test(expected = RuntimeException::class)
	fun `onTrue is executed on true`() {
		true.onTrue { throw RuntimeException() }
	}

	@Test
	fun `onFalse returns true on true`() {
		assertThat(true.onFalse {}, equalTo(true))
	}

	@Test
	fun `onFalse returns false on false`() {
		assertThat(false.onFalse {}, equalTo(false))
	}

	@Test
	fun `onFalse is not executed on true`() {
		assertThat(true.onFalse { throw RuntimeException() }, equalTo(true))
	}

	@Test(expected = RuntimeException::class)
	fun `onFalse is executed on false`() {
		false.onFalse { throw RuntimeException() }
	}

}
