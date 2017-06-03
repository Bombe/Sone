package net.pterodactylus.sone.utils

import com.google.common.base.Optional

fun <T, R> Optional<T>.let(block: (T) -> R): R? = if (isPresent) block(get()) else null

fun <T> T?.asOptional(): Optional<T> = this?.let { Optional.of(it) } ?: Optional.absent<T>()
