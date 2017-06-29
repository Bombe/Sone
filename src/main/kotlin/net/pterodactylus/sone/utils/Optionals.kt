package net.pterodactylus.sone.utils

import com.google.common.base.Optional

fun <T, R> Optional<T>.let(block: (T) -> R): R? = if (isPresent) block(get()) else null
fun <T> Optional<T>.also(block: (T) -> Unit): Optional<T> { if (isPresent) block(get()); return this }

fun <T> T?.asOptional(): Optional<T> = this?.let { Optional.of(it) } ?: Optional.absent<T>()

fun <T, R> Iterable<T>.mapPresent(transform: (T) -> Optional<R>): List<R> =
		map(transform).filter { it.isPresent }.map { it.get() }
