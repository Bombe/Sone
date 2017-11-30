package net.pterodactylus.sone.utils

class Memoize<in T, out R>(private val calc: (T) -> R) : (T) -> R {

	private val values = mutableMapOf<T, R>()

	override fun invoke(value: T) =
			values.getOrPut(value, { calc(value) })

}

fun <T, R> ((T) -> R).memoize(): (T) -> R = Memoize(this)
