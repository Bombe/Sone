package net.pterodactylus.sone.test

import com.google.inject.Injector
import com.google.inject.Module
import javax.inject.Provider
import kotlin.reflect.KClass

fun <T : Any> KClass<T>.isProvidedBy(instance: T) = Module { it.bind(this.java).toProvider(Provider<T> { instance }) }
fun <T : Any> KClass<T>.isProvidedBy(provider: com.google.inject.Provider<T>) = Module { it.bind(this.java).toProvider(provider) }
fun <T : Any> KClass<T>.isProvidedBy(provider: KClass<out Provider<T>>) = Module { it.bind(this.java).toProvider(provider.java) }
inline fun <reified T : Any> KClass<T>.isProvidedByMock() = Module { it.bind(this.java).toProvider(Provider<T> { mock() }) }

inline fun <reified T : Any> Injector.getInstance() = getInstance(T::class.java)!!

fun <T : Any> supply(javaClass: Class<T>): Source<T> = object : Source<T> {
	override fun fromInstance(instance: T) = Module { it.bind(javaClass).toInstance(instance) }
	override fun byInstance(instance: T) = Module { it.bind(javaClass).toProvider(Provider<T> { instance }) }
	override fun byProvider(provider: com.google.inject.Provider<T>) = Module { it.bind(javaClass).toProvider(provider) }
	override fun byProvider(provider: Class<Provider<T>>) = Module { it.bind(javaClass).toProvider(provider) }
}

interface Source<T : Any> {
	fun fromInstance(instance: T): Module
	fun byInstance(instance: T): Module
	fun byProvider(provider: com.google.inject.Provider<T>): Module
	fun byProvider(provider: Class<Provider<T>>): Module
}
