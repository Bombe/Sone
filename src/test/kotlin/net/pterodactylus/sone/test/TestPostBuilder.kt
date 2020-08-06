package net.pterodactylus.sone.test

import com.google.common.base.Optional
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.database.PostBuilder
import java.util.UUID

/**
 * [PostBuilder] implementation that returns a mocked [Post].
 */
class TestPostBuilder : PostBuilder {

	private val post = mock<Post>()
	private var recipientId: String? = null

	override fun copyPost(post: Post): PostBuilder = this

	override fun from(senderId: String): PostBuilder = apply {
		val sone = mock<Sone>()
		whenever(sone.id).thenReturn(senderId)
		whenever(post.sone).thenReturn(sone)
	}

	override fun randomId(): PostBuilder = apply {
		whenever(post.id).thenReturn(UUID.randomUUID().toString())
	}

	override fun withId(id: String): PostBuilder = apply {
		whenever(post.id).thenReturn(id)
	}

	override fun currentTime(): PostBuilder = apply {
		whenever(post.time).thenReturn(System.currentTimeMillis())
	}

	override fun withTime(time: Long): PostBuilder = apply {
		whenever(post.time).thenReturn(time)
	}

	override fun withText(text: String): PostBuilder = apply {
		whenever(post.text).thenReturn(text)
	}

	override fun to(recipientId: String): PostBuilder = apply {
		this.recipientId = recipientId
	}

	override fun build(): Post = post
			.also {
				whenever(post.recipientId).thenReturn(Optional.fromNullable(recipientId))
			}

}
