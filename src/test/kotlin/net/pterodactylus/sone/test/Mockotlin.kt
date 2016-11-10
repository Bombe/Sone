package net.pterodactylus.sone.test

import org.mockito.Mockito

inline fun <reified T : Any> mock(): T = Mockito.mock<T>(T::class.java)!!
