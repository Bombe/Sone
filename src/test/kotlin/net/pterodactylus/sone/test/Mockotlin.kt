package net.pterodactylus.sone.test

import com.google.inject.Module
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

inline fun <reified T : Any> mock(): T = Mockito.mock<T>(T::class.java)!!
inline fun <reified T : Any> mockBuilder(): T = Mockito.mock<T>(T::class.java, Mockito.RETURNS_SELF)!!
inline fun <reified T : Any> deepMock(): T = Mockito.mock<T>(T::class.java, Mockito.RETURNS_DEEP_STUBS)!!
inline fun <reified T : Any> capture(): ArgumentCaptor<T> = ArgumentCaptor.forClass(T::class.java)

inline fun <reified T : Any> bind(implementation: T): Module =
		Module { it!!.bind(T::class.java).toInstance(implementation) }

inline fun <reified T : Any> bindMock(): Module =
		Module { it!!.bind(T::class.java).toInstance(mock<T>()) }

inline fun <reified T: Any> whenever(methodCall: T) = Mockito.`when`(methodCall)
