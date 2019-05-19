package net.pterodactylus.sone.web.page

import freenet.clients.http.*

class PageMakerInteraction(toadletContext: ToadletContext, pageTitle: String) {

	private val pageMaker: PageMaker = toadletContext.pageMaker
	private val pageNode: PageNode = pageMaker.getPageNode(pageTitle, toadletContext)

	fun addStyleSheet(styleSheet: String) {
		pageNode.addCustomStyleSheet(styleSheet)
	}

	fun addLinkNode(linkAttributes: Map<String, String>) {
		pageNode.headNode.addChild("link").let {
			linkAttributes.forEach(it::addAttribute)
		}
	}

	fun addShortcutIcon(shortcutIcon: String) {
		pageNode.addForwardLink("icon", shortcutIcon)
	}

	fun setContent(content: String) {
		pageNode.content.addChild("%", content)
	}

	fun renderPage(): String =
			pageNode.outer.generate()

}
