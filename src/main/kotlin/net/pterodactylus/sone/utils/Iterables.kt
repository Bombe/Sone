package net.pterodactylus.sone.utils

import com.google.common.base.Optional

fun <T, R> Iterable<T>.mapPresent(transform: (T) -> Optional<R>): List<R> =
		map(transform).filter { it.isPresent }.map { it.get() }
