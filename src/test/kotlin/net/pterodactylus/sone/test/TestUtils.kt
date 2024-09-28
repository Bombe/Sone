package net.pterodactylus.sone.test

import org.junit.rules.ExpectedException
import sun.misc.Unsafe

inline fun <reified O : Any> setField(instance: O, name: String, value: Any?) {
	val field = O::class.java.getDeclaredField(name)
	val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").apply {
		isAccessible = true
	}.get(null) as Unsafe
	val offset = unsafe.objectFieldOffset(field)
	unsafe.putObject(instance, offset, value)
}

inline fun <reified T : Throwable> ExpectedException.expect() = expect(T::class.java)
