package net.pterodactylus.sone.utils

fun <T> T?.asList() = this?.let(::listOf) ?: emptyList()
val Any?.unit get() = Unit

fun <T> T?.throwOnNullIf(throwCondition: Boolean, exception: () -> Throwable) =
		if (this == null && throwCondition) throw exception() else this
