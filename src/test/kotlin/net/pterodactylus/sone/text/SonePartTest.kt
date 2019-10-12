package net.pterodactylus.sone.text

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [SonePart].
 */
class SonePartTest {

	private val sone = mock<Sone>()

	init {
		whenever(sone.profile).thenReturn(mock())
		whenever(sone.name).thenReturn("sone")
	}

	private val part = SonePart(sone)

	@Test
	fun textIsConstructedFromSonesNiceName() {
		assertThat<String>(part.text, equalTo<String>("sone"))
	}

}
