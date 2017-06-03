package net.pterodactylus.sone.utils

import com.google.common.base.Optional

fun <T, R> Optional<T>.let(block: (T) -> R): R? = if (isPresent) block(get()) else null
fun <T> Optional<T>.also(block: (T) -> Unit) = if (isPresent) block(get()) else Unit

fun <T> T?.asOptional(): Optional<T> = this?.let { Optional.of(it) } ?: Optional.absent<T>()
