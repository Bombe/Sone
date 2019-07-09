package net.pterodactylus.sone.data

import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

class ClientTest {

	@Test
	fun `toString() formats client name and version`() {
		val client = Client("Test Client", "v123")
		assertThat(client.toString(), equalTo("Test Client v123"))
	}

}
