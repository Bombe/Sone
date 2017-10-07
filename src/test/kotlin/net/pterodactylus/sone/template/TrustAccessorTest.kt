package net.pterodactylus.sone.template

import net.pterodactylus.sone.freenet.wot.Trust
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [TrustAccessor].
 */
class TrustAccessorTest {

	private val accessor = TrustAccessor()

	@Test
	fun `accessor returns false if there is no explicit trust assigned`() {
		assertThat(accessor.get(null, Trust(null, null, null), "assigned"), equalTo<Any>(false))
	}

	@Test
	fun `accessor returns true if there is explicit trust assigned`() {
		assertThat(accessor.get(null, Trust(0, null, null), "assigned"), equalTo<Any>(true))
	}

	@Test
	fun `reflection accessor is used for other members`() {
		assertThat(accessor.get(null, Trust(0, 0, 0), "hashCode"), equalTo<Any>(Trust(0, 0, 0).hashCode()))
	}

}
