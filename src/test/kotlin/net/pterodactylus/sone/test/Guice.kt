package net.pterodactylus.sone.test

import com.google.inject.Module
import kotlin.reflect.KClass

fun <T : Any> T.bindAs(bindClass: KClass<T>) = Module { it.bind(bindClass.java).toInstance(this@bindAs) }
