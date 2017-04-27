package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetPage
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.web.Response
import java.net.URI

/**
 * Page that delivers a {@link TemporaryImage} to the browser.
 */
class GetImagePage(webInterface: WebInterface): FreenetPage {

	private val core = webInterface.core!!

	override fun getPath(): String {
		return "getImage.html"
	}

	override fun isPrefixPage(): Boolean {
		return false
	}

	override fun handleRequest(request: FreenetRequest, response: Response): Response {
		val image = core.getTemporaryImage(request.httpRequest.getParam("image")) ?: return response.apply {
			statusCode = 404
			statusText = "Not found."
			contentType = "text/html; charset=utf-8"
		}
		return response.apply {
			statusCode = 200
			contentType = image.mimeType
			content.write(image.imageData)
			addHeader("Content-Disposition", "attachment; filename=${image.id}.${image.mimeType.split('/')[1]}")
		}
	}

	override fun isLinkExcepted(link: URI?): Boolean {
		return false
	}

}
