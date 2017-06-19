package net.pterodactylus.sone.utils

import com.google.common.base.Optional
import com.google.common.base.Optional.fromNullable
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

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
	fun `present optional can be processed with also`() {
		val called = AtomicBoolean(false)
		Optional.of(1).also { if (it == 1) called.set(true) }
		assertThat(called.get(), equalTo(true))
	}

	@Test
	fun `absent optional is not processed with also`() {
		val called = AtomicBoolean(false)
		Optional.absent<Int>().also { called.set(true) }
		assertThat(called.get(), equalTo(false))
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

	@Test
	fun testMapPresent() {
		val originalList = listOf(1, 2, null, 3, null)
		assertThat(originalList.mapPresent { fromNullable(it) }, contains(1, 2, 3))
	}

}
