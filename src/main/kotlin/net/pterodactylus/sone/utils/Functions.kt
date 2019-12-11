package net.pterodactylus.sone.utils

import java.util.function.*

/** Allows easy invocation of Java Consumers. */
operator fun <T> Consumer<T>.invoke(t: T) = accept(t)

/** Allows easy invocation of Java Runnables. */
operator fun Runnable.invoke() = run()
