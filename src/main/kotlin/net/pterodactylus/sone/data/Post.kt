package net.pterodactylus.sone.data

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
