package net.pterodactylus.sone.database.memory

import com.google.common.base.Optional.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import java.util.*

class MemoryPostTest {

	private val memoryDatabase = mock<MemoryDatabase>()

	@Test
	fun `memory post returns empty optional for post without recipient`() {
		val memoryPost = MemoryPost(memoryDatabase, memoryDatabase, UUID.randomUUID().toString(), "soneId", null, 123, "text")
		assertThat(memoryPost.recipient, equalTo(absent()))
	}

	@Test
	fun `empty optional is returned if recipient is set but non-existent`() {
		val memoryPost = MemoryPost(memoryDatabase, memoryDatabase, UUID.randomUUID().toString(), "soneId", "recipientId", 123, "text")
		assertThat(memoryPost.recipient, equalTo(absent()))
	}

}
