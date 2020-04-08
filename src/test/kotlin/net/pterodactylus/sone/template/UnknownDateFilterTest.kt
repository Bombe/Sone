package net.pterodactylus.sone.template

import net.pterodactylus.sone.freenet.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import java.util.*

/**
 * Unit test for [UnknownDateFilter].
 */
class UnknownDateFilterTest {

	private val translation = object : Translation {
		override val currentLocale = Locale.ENGLISH
		override fun translate(key: String) = if (key == unknownKey) "translated" else ""
	}
	private val unknownKey = "unknown.key"
	private val filter = UnknownDateFilter(translation, unknownKey)

	@Test
	fun `filter returns given object for non-longs`() {
		val someObject = Any()
		assertThat(filter.format(null, someObject, null), equalTo<Any>(someObject))
	}

	@Test
	fun `filter returns translated value of unknown key if zero is given`() {
		assertThat(filter.format(null, 0L, null), equalTo<Any>("translated"))
	}

	@Test
	fun `filter returns original long if non-zero value is given`() {
		assertThat(filter.format(null, 1L, null), equalTo<Any>(1L))
	}

}
