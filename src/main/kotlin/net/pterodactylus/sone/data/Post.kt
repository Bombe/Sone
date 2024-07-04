package net.pterodactylus.sone.data

import java.util.Comparator.comparing
import kotlin.time.ExperimentalTime
import kotlin.time.days

/**
 * Predicate that returns whether a post is _not_ from the future,
 * i.e. whether it should be visible now.
 */
@get:JvmName("noFuturePost")
val noFuturePost: (Post) -> Boolean = { it.time <= System.currentTimeMillis() }

/**
 * Predicate that returns whether a post less than a year old,
 * i.e. whether it should be visible now.
 */
@OptIn(ExperimentalTime::class)
@get:JvmName("noOldPost")
val noOldPost: (Post, Int) -> Boolean = { p: Post, days: Int -> p.time > (System.currentTimeMillis() - days.days.inMilliseconds) }

/**
 * Comparator that orders posts by their time, newest posts first.
 */
@get:JvmName("newestPostFirst")
val newestPostFirst: Comparator<Post> = comparing(Post::getTime).reversed()
