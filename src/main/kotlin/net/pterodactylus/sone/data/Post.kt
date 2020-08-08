package net.pterodactylus.sone.data

import net.pterodactylus.sone.database.PostBuilder
import java.util.Comparator.comparing

/**
 * Predicate that returns whether a post is _not_ from the future,
 * i.e. whether it should be visible now.
 */
@get:JvmName("noFuturePost")
val noFuturePost: (Post) -> Boolean = { it.time <= System.currentTimeMillis() }

/**
 * Comparator that orders posts by their time, newest posts first.
 */
@get:JvmName("newestPostFirst")
val newestPostFirst: Comparator<Post> = comparing(Post::getTime).reversed()

data class PostShell(val id: String, val soneId: String, val recipientId: String?, val time: Long, val text: String) {

	fun build(postBuilder: PostBuilder) =
			postBuilder.withId(id).from(soneId).let { if (recipientId != null) it.to(recipientId) else it }.withTime(time).withText(text).build()

}

fun Post.toShell() = PostShell(id, sone!!.id, recipient.orNull()?.id, time, text)
