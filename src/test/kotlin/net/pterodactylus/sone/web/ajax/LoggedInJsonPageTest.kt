package net.pterodactylus.sone.web.ajax

/**
 * Unit test for [LoggedInJsonPageTest].
 */
class LoggedInJsonPageTest : JsonPageTest("path", requiresLogin = true, pageSupplier = { webInterface -> LoggedInJsonPage("path", webInterface) })
