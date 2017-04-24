package net.pterodactylus.sone.utils

val String?.emptyToNull get() = if ((this?.trim() ?: "") == "") null else this
