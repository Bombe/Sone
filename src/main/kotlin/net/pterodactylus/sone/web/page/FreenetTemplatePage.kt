/*
 * Sone - FreenetTemplatePage.java - Copyright © 2010–2019 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.web.page

import freenet.clients.http.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.util.template.*
import net.pterodactylus.util.web.*
import java.io.*
import java.lang.String.*
import java.net.*
import java.util.logging.*
import java.util.logging.Logger.*

/**
 * Base class for all [Page]s that are rendered with [Template]s and
 * fit into Freenet’s web interface.
 */
open class FreenetTemplatePage(
		private val path: String,
		private val templateContextFactory: TemplateContextFactory,
		loaders: Loaders,
		template: Template,
		private val invalidFormPasswordRedirectTarget: String
) : FreenetPage, LinkEnabledCallback {

	open val styleSheets: Collection<String> = emptySet()
	open val shortcutIcon: String? get() = null
	open val isFullAccessOnly get() = false

	override fun getPath() = path

	open fun getPageTitle(request: FreenetRequest) = ""

	override fun isPrefixPage() = false

	open fun getRedirectTarget(request: FreenetRequest): String? = null

	open fun getAdditionalLinkNodes(request: FreenetRequest): List<Map<String, String>> = emptyList()

	override fun isLinkExcepted(link: URI) = false

	override fun isEnabled(toadletContext: ToadletContext) = !isFullAccessOnly

	private val template = templatePath?.let(loaders::loadTemplate) ?: template

	override fun handleRequest(request: FreenetRequest, response: Response): Response {
		getRedirectTarget(request)?.let { redirectTarget -> return RedirectResponse(redirectTarget) }

		if (isFullAccessOnly && !request.toadletContext.isAllowedFullAccess) {
			return response.setStatusCode(401).setStatusText("Not authorized").setContentType("text/html")
		}
		val toadletContext = request.toadletContext
		if (request.method == Method.POST) {
			/* require form password. */
			val formPassword = request.httpRequest.getPartAsStringFailsafe("formPassword", 32)
			if (formPassword != toadletContext.container.formPassword) {
				return RedirectResponse(invalidFormPasswordRedirectTarget)
			}
		}
		val pageMaker = toadletContext.pageMaker
		val pageNode = pageMaker.getPageNode(getPageTitle(request), toadletContext)

		styleSheets.forEach(pageNode::addCustomStyleSheet)
		getAdditionalLinkNodes(request)
				.map { it to pageNode.headNode.addChild("link") }
				.forEach { (linkNodeParameters, linkNode) ->
					linkNodeParameters.forEach(linkNode::addAttribute)
				}
		shortcutIcon?.let { pageNode.addForwardLink("icon", it) }

		val templateContext = templateContextFactory.createTemplateContext()
		templateContext.mergeContext(template.initialContext)
		try {
			val start = System.nanoTime()
			processTemplate(request, templateContext)
			val finish = System.nanoTime()
			logger.log(Level.FINEST, format("Template was rendered in %.2fms.", (finish - start) / 1000000.0))
		} catch (re1: RedirectException) {
			return RedirectResponse(re1.target ?: "")
		}

		val stringWriter = StringWriter()
		template.render(templateContext, stringWriter)
		pageNode.content.addChild("%", stringWriter.toString())

		return response.setStatusCode(200).setStatusText("OK").setContentType("text/html").write(pageNode.outer.generate())
	}

	open fun processTemplate(request: FreenetRequest, templateContext: TemplateContext) {
		/* do nothing. */
	}

	class RedirectException(val target: String?) : Exception() {
		override fun toString(): String = format("RedirectException{target='%s'}", target)
	}

}

private val logger: Logger = getLogger(FreenetTemplatePage::class.java.name)
