package net.pterodactylus.sone.web.pages

import freenet.support.api.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.text.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import java.awt.image.*
import java.io.*
import javax.imageio.*
import javax.inject.*

/**
 * Page implementation that lets the user upload an image.
 */
@TemplatePath("/templates/invalid.html")
@ToadletPath("uploadImage.html")
class UploadImagePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("uploadImage.html", "Page.UploadImage.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val parentAlbum = soneRequest.parameters["parent"]!!.let(soneRequest.core::getAlbum) ?: throw RedirectException("noPermission.html")
			if (parentAlbum.sone != currentSone) {
				throw RedirectException("noPermission.html")
			}
			val title = soneRequest.parameters["title", 200].emptyToNull ?: throw RedirectException("emptyImageTitle.html")

			val uploadedFile = soneRequest.httpRequest.getUploadedFile("image")
			val bytes = uploadedFile.data.use { it.toByteArray() }
			val bufferedImage = bytes.toImage()
			if (bufferedImage == null) {
				templateContext["messages"] = soneRequest.l10n.getString("Page.UploadImage.Error.InvalidImage")
				return
			}

			val temporaryImage = soneRequest.core.createTemporaryImage(bytes.mimeType, bytes)
			soneRequest.core.createImage(currentSone, parentAlbum, temporaryImage).modify().apply {
				setWidth(bufferedImage.width)
				setHeight(bufferedImage.height)
				setTitle(title)
				setDescription(TextFilter.filter(soneRequest.headers["Host"], soneRequest.parameters["description", 4000]))
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

	private val ByteArray.mimeType
		get() = ByteArrayInputStream(this).use {
			ImageIO.createImageInputStream(it).use {
				ImageIO.getImageReaders(it).asSequence()
						.firstOrNull()?.originatingProvider?.mimeTypes?.firstOrNull()
						?: UNKNOWN_MIME_TYPE
			}
		}

}

private const val UNKNOWN_MIME_TYPE = "application/octet-stream"
