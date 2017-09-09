package net.pterodactylus.sone.utils

/**
 * Returns the value of [block] if `this` is true, returns `null` otherwise.
 */
fun <R> Boolean.ifTrue(block: () -> R): R? = if (this) block() else null

/**
 * Returns the value of [block] if `this` is false, returns `null` otherwise.
 */
fun <R> Boolean.ifFalse(block: () -> R): R? = if (!this) block() else null
