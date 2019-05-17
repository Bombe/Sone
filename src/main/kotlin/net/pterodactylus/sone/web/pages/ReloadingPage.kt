package net.pterodactylus.sone.web.pages

import net.pterodactylus.util.web.*
import java.io.*
import javax.inject.*

/**
 * [Page] implementation that delivers static files from the filesystem.
 */
class ReloadingPage<R : Request> @Inject constructor(private val prefix: String, private val path: String, private val mimeType: String) : Page<R> {

	override fun isPrefixPage() = true

	override fun getPath() = prefix

	override fun handleRequest(request: R, response: Response): Response {
		val filename = request.uri.path.split("/").last()
		File(path, filename).also { file ->
			if (file.exists()) {
				response.content.use { output ->
					file.forEachBlock { buffer, bytesRead ->
						output.write(buffer, 0, bytesRead)
					}
				}
				response.statusCode = 200
				response.contentType = mimeType
			} else {
				response.statusCode = 404
				response.statusText = "Not found"
			}
		}
		return response
	}

}
