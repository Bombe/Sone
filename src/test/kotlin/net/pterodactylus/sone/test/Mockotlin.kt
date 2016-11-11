package net.pterodactylus.sone.test

import org.mockito.ArgumentCaptor
import org.mockito.Mockito

inline fun <reified T : Any> mock(): T = Mockito.mock<T>(T::class.java)!!
inline fun <reified T : Any> capture(): ArgumentCaptor<T> = ArgumentCaptor.forClass(T::class.java)
