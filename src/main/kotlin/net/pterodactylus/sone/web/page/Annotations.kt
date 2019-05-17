package net.pterodactylus.sone.web.page

import net.pterodactylus.util.web.*

annotation class MenuName(val value: String)

val Page<*>.menuName get() = javaClass.getAnnotation(MenuName::class.java)?.value

annotation class TemplatePath(val value: String)

val Page<*>.templatePath get() = javaClass.getAnnotation(TemplatePath::class.java)?.value

annotation class ToadletPath(val value: String)

val Page<*>.toadletPath get() = javaClass.getAnnotation(ToadletPath::class.java)?.value
