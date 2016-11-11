package net.pterodactylus.sone.text

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test

/**
 * Unit test for [FreenetLinkPart].
 */
class FreenetLinkPartTest {

	@Test
	fun linkIsUsedAsTitleIfNoTextIsGiven() {
		assertThat(FreenetLinkPart("link", "text", true).title, `is`("link"))
	}

}
