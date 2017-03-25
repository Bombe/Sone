package net.pterodactylus.sone.utils

import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import net.pterodactylus.util.web.Request

val Request.isGET get() = this.method == GET
val Request.isPOST get() = this.method == POST
