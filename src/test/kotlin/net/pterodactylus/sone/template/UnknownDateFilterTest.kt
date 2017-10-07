package net.pterodactylus.sone.template

import freenet.l10n.BaseL10n
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [UnknownDateFilter].
 */
class UnknownDateFilterTest {

	private val baseL10n = mock<BaseL10n>()
	private val unknownKey = "unknown.key"
	private val filter = UnknownDateFilter(baseL10n, unknownKey)

	@Test
	fun `filter returns given object for non-longs`() {
	    val someObject = Any()
		assertThat(filter.format(null, someObject, null), equalTo<Any>(someObject))
	}

	@Test
	fun `filter returns translated value of unknown key if zero is given`() {
	    whenever(baseL10n.getString(unknownKey)).thenReturn("translated")
		assertThat(filter.format(null, 0L, null), equalTo<Any>("translated"))
	}

	@Test
	fun `filter returns original long if non-zero value is given`() {
		assertThat(filter.format(null, 1L, null), equalTo<Any>(1L))
	}

}
