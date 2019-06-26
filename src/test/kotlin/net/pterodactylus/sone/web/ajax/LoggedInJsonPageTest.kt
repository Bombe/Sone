package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*

/**
 * Unit test for [LoggedInJsonPageTest].
 */
class LoggedInJsonPageTest : JsonPageTest("path", requiresLogin = true, pageSupplier = ::TestPage)

@ToadletPath("path")
class TestPage(webInterface: WebInterface) : LoggedInJsonPage(webInterface)
