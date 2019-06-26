package net.pterodactylus.sone.web.page

import com.google.inject.*
import freenet.clients.http.*

@ImplementedBy(DefaultPageMakerInteractionFactory::class)
interface PageMakerInteractionFactory {

	fun createPageMaker(toadletContext: ToadletContext, pageTitle: String): PageMakerInteraction

}

class DefaultPageMakerInteractionFactory : PageMakerInteractionFactory {

	override fun createPageMaker(toadletContext: ToadletContext, pageTitle: String) =
			PageMakerInteraction(toadletContext, pageTitle)

}
