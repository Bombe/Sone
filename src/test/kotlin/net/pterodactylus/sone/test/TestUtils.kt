package net.pterodactylus.sone.test

import org.junit.rules.*
import java.lang.reflect.*

private val modifiers = Field::class.java.getDeclaredField("modifiers").apply {
	isAccessible = true
}

fun setField(instance: Any, name: String, value: Any?) {
	generateSequence<Class<*>>(instance.javaClass) { it.superclass }
			.flatMap { it.declaredFields.asSequence() }
			.filter { it.name == name }
			.toList()
			.forEach { field ->
				field.isAccessible = true
				modifiers.setInt(field, field.modifiers and Modifier.FINAL.inv())
				field.set(instance, value)
			}
}

inline fun <reified T : Throwable> ExpectedException.expect() = expect(T::class.java)
