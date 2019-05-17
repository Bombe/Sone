package net.pterodactylus.sone.web.page

import net.pterodactylus.util.web.*

annotation class MenuName(val value: String)

val Page<*>.menuName get() = javaClass.getAnnotation(MenuName::class.java)?.value
