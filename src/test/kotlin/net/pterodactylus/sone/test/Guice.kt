package net.pterodactylus.sone.test

import com.google.inject.Module
import javax.inject.Provider
import kotlin.reflect.KClass

fun <T : Any> KClass<T>.isInstance(instance: T) = Module { it.bind(this.java).toInstance(instance) }
fun <T : Any> KClass<T>.isProvidedBy(instance: T) = Module { it.bind(this.java).toProvider { instance } }
fun <T : Any> KClass<T>.isProvidedBy(provider: com.google.inject.Provider<T>) = Module { it.bind(this.java).toProvider(provider) }
fun <T : Any> KClass<T>.isProvidedBy(provider: KClass<out Provider<T>>) = Module { it.bind(this.java).toProvider(provider.java) }
