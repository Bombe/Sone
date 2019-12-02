package net.pterodactylus.sone.test

import com.google.inject.*
import com.google.inject.name.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.mockito.*
import javax.inject.Provider
import kotlin.reflect.*

fun <T : Any> KClass<T>.isProvidedBy(instance: T) = Module { it.bind(this.java).toProvider(Provider<T> { instance }) }
fun <T : Any> KClass<T>.withNameIsProvidedBy(instance: T, name: String) = Module { it.bind(this.java).annotatedWith(Names.named(name)).toProvider(Provider<T> { instance }) }
fun <T : Any> KClass<T>.isProvidedBy(provider: com.google.inject.Provider<T>) = Module { it.bind(this.java).toProvider(provider) }
fun <T : Any> KClass<T>.isProvidedBy(provider: KClass<out Provider<T>>) = Module { it.bind(this.java).toProvider(provider.java) }
inline fun <reified T : Any> KClass<T>.isProvidedByMock() = Module { it.bind(this.java).toProvider(Provider<T> { mock() }) }
inline fun <reified T : Any> KClass<T>.isProvidedByDeepMock() = Module { it.bind(this.java).toProvider(Provider<T> { deepMock() }) }

inline fun <reified T : Any> Injector.getInstance(annotation: Annotation? = null): T = annotation
		?.let { getInstance(Key.get(object : TypeLiteral<T>() {}, it)) }
		?: getInstance(Key.get(object : TypeLiteral<T>() {}))


inline fun <reified T : Any> Injector.verifySingletonInstance() {
	val firstInstance = getInstance<T>()
	val secondInstance = getInstance<T>()
	assertThat(firstInstance, sameInstance(secondInstance))
}

fun <T : Any> supply(javaClass: Class<T>): Source<T> = object : Source<T> {
	override fun fromInstance(instance: T) = Module { it.bind(javaClass).toInstance(instance) }
	override fun byInstance(instance: T) = Module { it.bind(javaClass).toProvider(Provider<T> { instance }) }
	override fun byProvider(provider: com.google.inject.Provider<T>) = Module { it.bind(javaClass).toProvider(provider) }
	override fun byProvider(provider: Class<Provider<T>>) = Module { it.bind(javaClass).toProvider(provider) }
	override fun byMock() = Module { it.bind(javaClass).toInstance(Mockito.mock(javaClass)) }
}

interface Source<T : Any> {
	fun fromInstance(instance: T): Module
	fun byInstance(instance: T): Module
	fun byProvider(provider: com.google.inject.Provider<T>): Module
	fun byProvider(provider: Class<Provider<T>>): Module
	fun byMock(): Module
}
