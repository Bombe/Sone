package net.pterodactylus.sone.web.pages

import freenet.clients.http.*
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.web.SessionProvider
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.web.*
import java.net.URLEncoder

/**
 * Base page for the Sone web interface.
 */
open class SoneTemplatePage @JvmOverloads constructor(
		path: String,
		private val webInterface: WebInterface,
		loaders: Loaders,
		template: Template,
		templateRenderer: TemplateRenderer,
		private val pageTitleKey: String? = null,
		private val requiresLogin: Boolean = false,
		private val pageTitle: (FreenetRequest) -> String = { pageTitleKey?.let(webInterface.l10n::getString) ?: "" }
) : FreenetTemplatePage(path, templateRenderer, loaders, template, "noPermission.html") {

	private val core = webInterface.core
	private val sessionProvider: SessionProvider = webInterface

	protected fun getCurrentSone(toadletContext: ToadletContext, createSession: Boolean = true) =
			sessionProvider.getCurrentSone(toadletContext, createSession)

	protected fun setCurrentSone(toadletContext: ToadletContext, sone: Sone?) =
			sessionProvider.setCurrentSone(toadletContext, sone)

	fun requiresLogin() = requiresLogin

	override public fun getPageTitle(freenetRequest: FreenetRequest) = getPageTitle(freenetRequest.toSoneRequest(core, webInterface))

	open fun getPageTitle(soneRequest: SoneRequest) = pageTitle(soneRequest)

	override val styleSheets = listOf("css/sone.css")

	override val shortcutIcon = "images/icon.png"

	override public fun getAdditionalLinkNodes(request: FreenetRequest) =
			listOf(mapOf(
					"rel" to "search",
					"type" to "application/opensearchdescription+xml",
					"title" to "Sone",
					"href" to "http://${request.httpRequest.getHeader("host")}/Sone/OpenSearch.xml"
			))

	final override public fun processTemplate(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		super.processTemplate(freenetRequest, templateContext)
		templateContext["preferences"] = core.preferences
		templateContext["currentSone"] = getCurrentSone(freenetRequest.toadletContext)
		templateContext["localSones"] = core.localSones
		templateContext["request"] = freenetRequest
		templateContext["currentVersion"] = SonePlugin.getPluginVersion()
		templateContext["hasLatestVersion"] = core.updateChecker.hasLatestVersion()
		templateContext["latestEdition"] = core.updateChecker.latestEdition
		templateContext["latestVersion"] = core.updateChecker.latestVersion
		templateContext["latestVersionTime"] = core.updateChecker.latestVersionDate
		webInterface.getNotifications(getCurrentSone(freenetRequest.toadletContext)).sortedBy(Notification::getCreatedTime).run {
			templateContext["notifications"] = this
			templateContext["notificationHash"] = this.hashCode()
		}
		handleRequest(freenetRequest, templateContext)
	}

	open fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		handleRequest(freenetRequest.toSoneRequest(core, webInterface), templateContext)
	}

	open fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
	}

	override public fun getRedirectTarget(freenetRequest: FreenetRequest): String? {
		if (requiresLogin && getCurrentSone(freenetRequest.toadletContext) == null) {
			val parameters = freenetRequest.httpRequest.parameterNames
					.flatMap { name -> freenetRequest.httpRequest.getMultipleParam(name).map { name to it } }
					.joinToString("&") { "${it.first.urlEncode}=${it.second.urlEncode}" }
					.emptyToNull
			return "login.html?target=${freenetRequest.httpRequest.path}${parameters?.let { ("?" + it).urlEncode } ?: ""}"
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
