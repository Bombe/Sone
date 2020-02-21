package net.pterodactylus.sone.data

/**
 * Predicate that returns whether a post is _not_ from the future,
 * i.e. whether it should be visible now.
 */
@get:JvmName("noFuturePost")
val noFuturePost: (Post) -> Boolean = { it.time <= System.currentTimeMillis() }
