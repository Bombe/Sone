package net.pterodactylus.sone.utils

/**
 * Returns the value of [block] if `this` is true, returns `null` otherwise.
 */
fun <R> Boolean.ifTrue(block: () -> R): R? = if (this) block() else null
