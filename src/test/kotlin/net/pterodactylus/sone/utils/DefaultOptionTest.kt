package net.pterodactylus.sone.utils

import com.google.common.base.Predicate
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.hamcrest.Matchers.sameInstance
import org.junit.Test

/**
 * Unit test for [DefaultOption].
 */
class DefaultOptionTest {

	private val defaultValue = Any()
	private val acceptedValue = Any()
	private val matchesAcceptedValue = Predicate<Any?> { it == acceptedValue }

	@Test
	fun `default option returns default value when unset`() {
		val defaultOption = DefaultOption(defaultValue)
		assertThat(defaultOption.get(), sameInstance(defaultValue))
	}

	@Test
	fun `default option returns null for real when unset`() {
		val defaultOption = DefaultOption(defaultValue)
		assertThat(defaultOption.real, nullValue())
	}

	@Test
	fun `default option will return set value`() {
		val defaultOption = DefaultOption(defaultValue)
		val newValue = Any()
		defaultOption.set(newValue)
		assertThat(defaultOption.get(), sameInstance(newValue))
	}

	@Test
	fun `default option with validator accepts valid values`() {
		val defaultOption = DefaultOption(defaultValue, matchesAcceptedValue)
		defaultOption.set(acceptedValue)
		assertThat(defaultOption.get(), sameInstance(acceptedValue))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `default option with validator rejects invalid values`() {
		val defaultOption = DefaultOption(defaultValue, matchesAcceptedValue)
		defaultOption.set(Any())
	}

	@Test
	fun `default option validates objects correctly`() {
		val defaultOption = DefaultOption(defaultValue, matchesAcceptedValue)
		assertThat(defaultOption.validate(acceptedValue), equalTo(true))
		assertThat(defaultOption.validate(Any()), equalTo(false))
	}

	@Test
	fun `setting to null will restore default value`() {
		val defaultOption = DefaultOption(defaultValue)
		defaultOption.set(null)
		assertThat(defaultOption.get(), sameInstance(defaultValue))
	}

	@Test
	fun `validate without validator will validate null`() {
		val defaultOption = DefaultOption(defaultValue)
		assertThat(defaultOption.validate(null), equalTo(true))
	}

	@Test
	fun `validate with validator will validate null`() {
		val defaultOption = DefaultOption(defaultValue, matchesAcceptedValue)
		assertThat(defaultOption.validate(null), equalTo(true))
	}

}
