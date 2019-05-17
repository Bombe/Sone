package net.pterodactylus.sone.web.pages

import freenet.clients.http.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import net.pterodactylus.util.web.*
import java.net.*

/**
 * Base page for the Sone web interface.
 */
open class SoneTemplatePage @JvmOverloads constructor(
		path: String,
		private val webInterface: WebInterface,
		loaders: Loaders,
		templateRenderer: TemplateRenderer,
		private val pageTitleKey: String? = null,
		private val requiresLogin: Boolean = false,
		private val pageTitle: (FreenetRequest) -> String = { pageTitleKey?.let(webInterface.l10n::getString) ?: "" }
) : FreenetTemplatePage(path, templateRenderer, loaders, "noPermission.html") {

	private val core = webInterface.core
	private val sessionProvider: SessionProvider = webInterface

	protected fun getCurrentSone(toadletContext: ToadletContext, createSession: Boolean = true) =
			sessionProvider.getCurrentSone(toadletContext, createSession)

	protected fun setCurrentSone(toadletContext: ToadletContext, sone: Sone?) =
			sessionProvider.setCurrentSone(toadletContext, sone)

	fun requiresLogin() = requiresLogin

	override fun getPageTitle(request: FreenetRequest) = getPageTitle(request.toSoneRequest(core, webInterface))

	open fun getPageTitle(soneRequest: SoneRequest) = pageTitle(soneRequest)

	override val styleSheets = listOf("css/sone.css")

	override val shortcutIcon = "images/icon.png"

	override fun getAdditionalLinkNodes(request: FreenetRequest) =
			listOf(mapOf(
					"rel" to "search",
					"type" to "application/opensearchdescription+xml",
					"title" to "Sone",
					"href" to "http://${request.httpRequest.getHeader("host")}/Sone/OpenSearch.xml"
			))

	final override fun processTemplate(request: FreenetRequest, templateContext: TemplateContext) {
		super.processTemplate(request, templateContext)
		templateContext["preferences"] = core.preferences
		templateContext["currentSone"] = getCurrentSone(request.toadletContext)
		templateContext["localSones"] = core.localSones
		templateContext["request"] = request
		templateContext["currentVersion"] = SonePlugin.getPluginVersion()
		templateContext["hasLatestVersion"] = core.updateChecker.hasLatestVersion()
		templateContext["latestEdition"] = core.updateChecker.latestEdition
		templateContext["latestVersion"] = core.updateChecker.latestVersion
		templateContext["latestVersionTime"] = core.updateChecker.latestVersionDate
		webInterface.getNotifications(getCurrentSone(request.toadletContext)).sortedBy(Notification::getCreatedTime).run {
			templateContext["notifications"] = this
			templateContext["notificationHash"] = this.hashCode()
		}
		handleRequest(request, templateContext)
	}

	open fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		handleRequest(freenetRequest.toSoneRequest(core, webInterface), templateContext)
	}

	open fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
	}

	override fun getRedirectTarget(request: FreenetRequest): String? {
		if (requiresLogin && getCurrentSone(request.toadletContext) == null) {
			val parameters = request.httpRequest.parameterNames
					.flatMap { name -> request.httpRequest.getMultipleParam(name).map { name to it } }
					.joinToString("&") { "${it.first.urlEncode}=${it.second.urlEncode}" }
					.emptyToNull
			return "login.html?target=${request.httpRequest.path}${parameters?.let { ("?" + it).urlEncode } ?: ""}"
		}
		return null
	}

	private val String.urlEncode: String get() = URLEncoder.encode(this, "UTF-8")

	override fun isEnabled(toadletContext: ToadletContext) =
			isEnabled(SoneRequest(toadletContext.uri, Method.GET, HTTPRequestImpl(toadletContext.uri, "GET"), toadletContext, webInterface.l10n, webInterface.sessionManager, core, webInterface))

	open fun isEnabled(soneRequest: SoneRequest) = when {
		requiresLogin && getCurrentSone(soneRequest.toadletContext) == null -> false
		core.preferences.requireFullAccess && !soneRequest.toadletContext.isAllowedFullAccess -> false
		else -> true
	}

}
