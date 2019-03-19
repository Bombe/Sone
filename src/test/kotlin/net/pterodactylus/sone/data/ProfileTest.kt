package net.pterodactylus.sone.data

import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

/**
 * Unit test for [Profile].
 */
class ProfileTest {

	private val sone = mock<Sone>()
	private val profile = Profile(sone)

	@Test
	fun `new fields are initialized with an empty string`() {
		val newField = profile.addField("testField")
		assertThat(newField.value, equalTo(""))
	}

}
