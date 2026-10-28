package net.pterodactylus.sone.test

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.database.PostBuilder
import java.util.UUID

/**
 * [PostBuilder] implementation that returns a mocked [Post].
 */
class TestPostBuilder : PostBuilder {

	private var id: String? = null
	private var soneId: String? = null
	private var recipientId: String? = null
	private var time: Long? = null
	private var text: String? = null

	override fun copyPost(post: Post): PostBuilder = this

	override fun from(senderId: String): PostBuilder = apply {
		soneId = senderId
	}

	override fun randomId(): PostBuilder = apply {
		id = UUID.randomUUID().toString()
	}

	override fun withId(id: String): PostBuilder = apply {
		this.id = id
	}

	override fun currentTime(): PostBuilder = apply {
		time = System.currentTimeMillis()
	}

	override fun withTime(time: Long): PostBuilder = apply {
		this.time = time
	}

	override fun withText(text: String): PostBuilder = apply {
		this.text = text
	}

	override fun to(recipientId: String): PostBuilder = apply {
		this.recipientId = recipientId
	}

	override fun build(): Post =
			createPost(text!!, sone = createRemoteSone(soneId!!), time = time!!, recipient = recipientId?.let { createRemoteSone(it) }, id = id!!)

}
