package net.pterodactylus.sone.main

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * Unit test for [parseVersion].
 */
class VersionParserTest {

	@Test
	fun `version from missing file can not be read`() {
		assertThat(parseVersion("does-not-exist.yaml"), nullValue())
	}

	@Test
	fun `custom version file can be parsed`() {
		val version = parseVersion("custom-version.yaml")!!
		assertThat(version.id, equalTo("some-id"))
		assertThat(version.nice, equalTo("some-nice"))
	}

	@Test
	fun `default version file is parsed`() {
		val version = parseVersion()!!
		assertThat(version.id, equalTo("43f3e1c3a0f487e37e5851a2cc72756d271c7571"))
		assertThat(version.nice, equalTo("0.9.6-466-g43f3e1c"))
	}

	@Test
	fun `parsed version is created correctly`() {
		val version = parsedVersion!!
		assertThat(version.id, equalTo("43f3e1c3a0f487e37e5851a2cc72756d271c7571"))
		assertThat(version.nice, equalTo("0.9.6-466-g43f3e1c"))
	}

}
