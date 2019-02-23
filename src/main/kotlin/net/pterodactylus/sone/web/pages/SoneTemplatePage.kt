package net.pterodactylus.sone.web.pages

import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.SonePlugin
import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.web.SessionProvider
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.sone.web.page.FreenetTemplatePage
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import java.net.URLEncoder

/**
 * Base page for the Sone web interface.
 */
open class SoneTemplatePage @JvmOverloads constructor(
		path: String,
		protected val webInterface: WebInterface,
		template: Template,
		private val pageTitleKey: String? = null,
		private val requiresLogin: Boolean = false,
		private val pageTitle: (FreenetRequest) -> String = { pageTitleKey?.let(webInterface.l10n::getString) ?: "" }
) : FreenetTemplatePage(path, webInterface.templateContextFactory, template, "noPermission.html") {

	private val core = webInterface.core
	protected val sessionProvider: SessionProvider = webInterface

	protected fun getCurrentSone(toadletContext: ToadletContext, createSession: Boolean = true) =
			sessionProvider.getCurrentSone(toadletContext, createSession)

	protected fun setCurrentSone(toadletContext: ToadletContext, sone: Sone?) =
			sessionProvider.setCurrentSone(toadletContext, sone)

	fun requiresLogin() = requiresLogin

	override public fun getPageTitle(freenetRequest: FreenetRequest) = pageTitle(freenetRequest)

	override public fun getStyleSheets() =
			listOf("css/sone.css")

	override public fun getShortcutIcon() = "images/icon.png"

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

	internal open fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
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

	override fun isEnabled(toadletContext: ToadletContext) = when {
		requiresLogin && getCurrentSone(toadletContext) == null -> false
		core.preferences.requireFullAccess && !toadletContext.isAllowedFullAccess -> false
		else -> true
	}

}
