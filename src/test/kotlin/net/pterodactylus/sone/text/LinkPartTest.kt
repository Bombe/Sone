package net.pterodactylus.sone.text

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [LinkPart].
 */
class LinkPartTest {

	@Test
	fun linkIsUsedAsTitleIfNoTitleIsGiven() {
		assertThat(LinkPart("link", "text").title, equalTo("link"))
	}

}
