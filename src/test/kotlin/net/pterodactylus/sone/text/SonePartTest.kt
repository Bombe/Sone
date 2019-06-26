package net.pterodactylus.sone.text

import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.mockito.Mockito.`when`

/**
 * Unit test for [SonePart].
 */
class SonePartTest {

	private val sone = mock<Sone>()

	init {
		`when`(sone.profile).thenReturn(mock())
		`when`(sone.name).thenReturn("sone")
	}

	private val part = SonePart(sone)

	@Test
	fun textIsConstructedFromSonesNiceName() {
		assertThat<String>(part.text, `is`<String>("sone"))
	}

}
