package net.pterodactylus.sone.database.memory

import com.google.common.base.Optional.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import java.util.*

class MemoryPostTest {

	@Test
	fun `memory post returns empty optional for post without recipient`() {
		val postDatabase = mock<MemoryDatabase>()
		val memoryPost = MemoryPost(postDatabase, postDatabase, UUID.randomUUID().toString(), "soneId", null, 123, "text")
		assertThat(memoryPost.recipient, equalTo(absent()))
	}

}
