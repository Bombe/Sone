package net.pterodactylus.sone.test

import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Module
import com.google.inject.TypeLiteral
import com.google.inject.name.Names
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.sameInstance
import org.mockito.Mockito
import javax.inject.Provider
import kotlin.reflect.KClass

fun <T : Any> KClass<T>.isProvidedBy(instance: T) = Module { it.bind(this.java).toProvider(Provider<T> { instance }) }
fun <T : Any> KClass<T>.withNameIsProvidedBy(instance: T, name: String) = Module { it.bind(this.java).annotatedWith(Names.named(name)).toProvider(Provider<T> { instance }) }
fun <T : Any> KClass<T>.isProvidedBy(provider: com.google.inject.Provider<T>) = Module { it.bind(this.java).toProvider(provider) }
fun <T : Any> KClass<T>.isProvidedBy(provider: KClass<out Provider<T>>) = Module { it.bind(this.java).toProvider(provider.java) }
fun <T : Any> Key<T>.isProvidedBy(instance: T) = Module { it.bind(this).toProvider(Provider<T> { instance }) }
inline fun <reified T : Any> KClass<T>.isProvidedByMock() = Module { it.bind(this.java).toProvider(Provider<T> { mock() }) }
inline fun <reified T : Any> KClass<T>.isProvidedByDeepMock() = Module { it.bind(this.java).toProvider(Provider<T> { deepMock() }) }

inline fun <reified T> key(): Key<T> = Key.get(object : TypeLiteral<T>() {})
inline fun <reified T> key(annotation: Annotation): Key<T> = Key.get(object : TypeLiteral<T>() {}, annotation)

inline fun <reified T : Any> Injector.getInstance(annotation: Annotation? = null): T = annotation
		?.let { getInstance(Key.get(object : TypeLiteral<T>() {}, it)) }
		?: getInstance(Key.get(object : TypeLiteral<T>() {}))


inline fun <reified T : Any> Injector.verifySingletonInstance(annotation: Annotation? = null) {
	val firstInstance = getInstance<T>(annotation)
	val secondInstance = getInstance<T>(annotation)
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
