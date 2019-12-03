package net.pterodactylus.sone.test

import net.pterodactylus.util.web.*

/**
 * Dummy implementation of a [Page].
 */
class TestPage<REQ : Request> : Page<REQ> {

	override fun getPath() = ""
	override fun isPrefixPage() = false
	override fun handleRequest(freenetRequest: REQ, response: Response) = response

}
