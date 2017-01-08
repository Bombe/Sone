package net.pterodactylus.sone.test

import com.google.common.base.Optional
import com.google.common.base.Optional.absent
import com.google.common.base.Optional.of

fun <T> T?.asOptional(): Optional<T> = if (this == null) absent<T>() else of(this)
