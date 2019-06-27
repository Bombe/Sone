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
		assertThat(version.id, equalTo("73241df664f676482d8ca2c13b03d3deac3eacba"))
		assertThat(version.nice, equalTo("v79-2-g73241df6"))
	}

	@Test
	fun `parsed version is created correctly`() {
		val version = parsedVersion!!
		assertThat(version.id, equalTo("73241df664f676482d8ca2c13b03d3deac3eacba"))
		assertThat(version.nice, equalTo("v79-2-g73241df6"))
	}

}
