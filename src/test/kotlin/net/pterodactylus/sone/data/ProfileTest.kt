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
	fun `first name is initialized with null`() {
		assertThat(profile.firstName, nullValue())
	}

	@Test
	fun `setting first name to value will set it to value`() {
	    profile.firstName = "first name"
		assertThat(profile.firstName, equalTo("first name"))
	}

	@Test
	fun `setting first name to null will set it to null`() {
	    profile.firstName = null
		assertThat(profile.firstName, nullValue())
	}

	@Test
	fun `setting first name to empty string will set it to null`() {
	    profile.firstName = ""
		assertThat(profile.firstName, nullValue())
	}

	@Test
	fun `middle name is initialized with null`() {
		assertThat(profile.middleName, nullValue())
	}

	@Test
	fun `setting middle name to value will set it to value`() {
		profile.middleName = "middle name"
		assertThat(profile.middleName, equalTo("middle name"))
	}

	@Test
	fun `setting middle name to null will set it to null`() {
		profile.middleName = null
		assertThat(profile.middleName, nullValue())
	}

	@Test
	fun `setting middle name to empty string will set it to null`() {
		profile.middleName = ""
		assertThat(profile.middleName, nullValue())
	}

	@Test
	fun `last name is initialized with null`() {
		assertThat(profile.lastName, nullValue())
	}

	@Test
	fun `setting last name to value will set it to value`() {
		profile.lastName = "last name"
		assertThat(profile.lastName, equalTo("last name"))
	}

	@Test
	fun `setting last name to null will set it to null`() {
		profile.lastName = null
		assertThat(profile.lastName, nullValue())
	}

	@Test
	fun `setting last name to empty string will set it to null`() {
		profile.lastName = ""
		assertThat(profile.lastName, nullValue())
	}

	@Test
	fun `new fields are initialized with an empty string`() {
		val newField = profile.addField("testField")
		assertThat(newField.value, equalTo(""))
	}

}
