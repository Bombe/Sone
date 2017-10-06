package net.pterodactylus.sone.web.pages

import freenet.support.api.Bucket
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.utils.headers
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.utils.use
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import org.bouncycastle.asn1.x500.style.RFC4519Style.title
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Page implementation that lets the user upload an image.
 */
class UploadImagePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("uploadImage.html", template, "Page.UploadImage.Title", webInterface, true) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			val parentAlbum = freenetRequest.parameters["parent"]!!.let(webInterface.core::getAlbum) ?: throw RedirectException("noPermission.html")
			if (parentAlbum.sone != getCurrentSone(freenetRequest.toadletContext)) {
				throw RedirectException("noPermission.html")
			}
			val title = freenetRequest.parameters["title", 200].emptyToNull ?: throw RedirectException("emptyImageTitle.html")

			val uploadedFile = freenetRequest.httpRequest.getUploadedFile("image")
			val bytes = uploadedFile.data.use { it.toByteArray() }
			val bufferedImage = bytes.toImage()
			if (bufferedImage == null) {
				templateContext["messages"] = webInterface.l10n.getString("Page.UploadImage.Error.InvalidImage")
				return
			}

			val temporaryImage = webInterface.core.createTemporaryImage(bytes.mimeType, bytes)
			webInterface.core.createImage(getCurrentSone(freenetRequest.toadletContext), parentAlbum, temporaryImage).modify().apply {
				setWidth(bufferedImage.width)
				setHeight(bufferedImage.height)
				setTitle(title)
				setDescription(TextFilter.filter(freenetRequest.headers["Host"], freenetRequest.parameters["description", 4000]))
			}.update()
			throw RedirectException("imageBrowser.html?album=${parentAlbum.id}")
		}
	}

	private fun Bucket.toByteArray(): ByteArray = ByteArrayOutputStream(size().toInt()).use { outputStream ->
		inputStream.copyTo(outputStream)
		outputStream.toByteArray()
	}

	private fun ByteArray.toImage(): BufferedImage? = ByteArrayInputStream(this).use {
		ImageIO.read(it)
	}

	private val ByteArray.mimeType get() = ByteArrayInputStream(this).use {
		ImageIO.createImageInputStream(it).use {
			ImageIO.getImageReaders(it).asSequence()
					.firstOrNull()?.originatingProvider?.mimeTypes?.firstOrNull()
					?: UNKNOWN_MIME_TYPE
		}
	}

}

private const val UNKNOWN_MIME_TYPE = "application/octet-stream"
