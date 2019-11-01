package net.pterodactylus.sone.utils

/**
 * Returns the value of [block] if `this` is true, returns `null` otherwise.
 */
fun <R> Boolean.ifTrue(block: () -> R): R? = if (this) block() else null

/**
 * Returns the value of [block] if `this` is false, returns `null` otherwise.
 */
fun <R> Boolean.ifFalse(block: () -> R): R? = if (!this) block() else null

/**
 * Returns `this` but runs the given block if `this`  is `false`.
 *
 * @param block The block to run if `this` is `false`
 * @return `this`
 */
fun Boolean.onFalse(block: () -> Unit): Boolean = this.also { if (!this) block() }
