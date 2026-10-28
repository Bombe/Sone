package net.pterodactylus.sone.test

import org.junit.Assert.assertThrows
import sun.misc.Unsafe

inline fun <reified O : Any> setField(instance: O, name: String, value: Any?) {
	val field = O::class.java.getDeclaredField(name)
	val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").apply {
		isAccessible = true
	}.get(null) as Unsafe
	val offset = unsafe.objectFieldOffset(field)
	unsafe.putObject(instance, offset, value)
}

inline fun <reified T : Throwable> assertThrows(noinline block: () -> Unit): T = assertThrows(T::class.java, block)
