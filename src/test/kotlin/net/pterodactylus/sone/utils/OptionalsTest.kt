package net.pterodactylus.sone.utils

import com.google.common.base.Optional
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * Test for [Optional] utils.
 */
class OptionalsTest {

	@Test
	fun `present optional can be transformed with let`() {
		val optional = Optional.of(1)
		assertThat(optional.let { it + 1 }, equalTo(2))
	}

	@Test
	fun `empty optional is transform to null with let`() {
		val optional = Optional.absent<Int>()
		assertThat(optional.let { it + 1 }, nullValue())
	}

	@Test
	fun `1 as optional is correct optional`() {
		val optional = 1.asOptional()
		assertThat(optional.get(), equalTo(1))
	}

	@Test
	fun `null as optional is asent optional`() {
		val optional = null.asOptional()
		assertThat(optional.isPresent, equalTo(false))
	}

}
