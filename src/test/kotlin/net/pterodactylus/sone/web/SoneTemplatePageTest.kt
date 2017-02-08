package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.SonePlugin
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.version.Version
import net.pterodactylus.util.web.Method.GET
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [SoneTemplatePage].
 */
class SoneTemplatePageTest : WebPageTest() {

	private val preferences by lazy { core.preferences!! }
	private val page = object : SoneTemplatePage("path.html", template, webInterface, true) {}

	@Test
	fun `current sone is retrieved from web interface`() {
		assertThat(page.getCurrentSone(toadletContext), equalTo(currentSone))
	}

	@Test
	fun `retrieving current sone without creation is forwarded to web interface`() {
		mock<Sone>().let {
			whenever(webInterface.getCurrentSoneWithoutCreatingSession(toadletContext)).thenReturn(it)
			assertThat(page.getCurrentSoneWithoutCreatingSession(toadletContext), equalTo(it))
		}
	}

	@Test
	fun `setting the current sone is forwarded to web interface`() {
		mock<Sone>().let {
			page.setCurrentSone(toadletContext, it)
			verify(webInterface).setCurrentSone(toadletContext, it)
		}
	}

	@Test
	fun `page title is empty string if no page title key was given`() {
		SoneTemplatePage("path.html", template, null, webInterface).let { page ->
			assertThat(page.getPageTitle(freenetRequest), equalTo(""))
		}
	}

	@Test
	fun `page title is retrieved from l10n if page title key is given`() {
		SoneTemplatePage("path.html", template, "page.title", webInterface).let { page ->
			whenever(l10n.getString("page.title")).thenReturn("Page Title")
			assertThat(page.getPageTitle(freenetRequest), equalTo("Page Title"))
		}
	}

	@Test
	fun `additional link nodes contain open search link`() {
		addHttpRequestHeader("Host", "www.example.com")
		assertThat(page.getAdditionalLinkNodes(freenetRequest), contains(mapOf(
				"rel" to "search",
				"type" to "application/opensearchdescription+xml",
				"title" to "Sone",
				"href" to "http://www.example.com/Sone/OpenSearch.xml"
		)))
	}

	@Test
	fun `style sheets contains sone CSS file`() {
		assertThat(page.styleSheets, contains("css/sone.css"))
	}

	@Test
	fun `shortcut icon is the sone icon`() {
		assertThat(page.shortcutIcon, equalTo("images/icon.png"))
	}

	@Test
	fun `page requires login if require login was specified in the constructor`() {
		SoneTemplatePage("path.html", template, webInterface, true).let { page ->
			assertThat(page.requiresLogin(), equalTo(true))
		}
	}

	@Test
	fun `page does not require login if require login was not specified in the constructor`() {
		SoneTemplatePage("path.html", template, webInterface, false).let { page ->
			assertThat(page.requiresLogin(), equalTo(false))
		}
	}

	private fun verifyVariableIsSet(name: String, value: Any) = verifyVariableMatches(name, equalTo<Any>(value))

	private fun <T> verifyVariableMatches(name: String, matcher: Matcher<T>) {
		page.processTemplate(freenetRequest, templateContext)
		@Suppress("UNCHECKED_CAST")
		assertThat(templateContext[name] as T, matcher)
	}

	@Test
	fun `preferences are set in template context`() {
	    verifyVariableIsSet("preferences", preferences)
	}

	@Test
	fun `current sone is set in template context`() {
		verifyVariableIsSet("currentSone", currentSone)
	}

	@Test
	fun `local sones are set in template context`() {
		val localSones = listOf(mock<Sone>(), mock<Sone>())
		whenever(core.localSones).thenReturn(localSones)
		verifyVariableMatches("localSones", containsInAnyOrder(*localSones.toTypedArray()))
	}

	@Test
	fun `freenet request is set in template context`() {
		verifyVariableIsSet("request", freenetRequest)
	}

	@Test
	fun `current version is set in template context`() {
		verifyVariableIsSet("currentVersion", SonePlugin.getPluginVersion())
	}

	@Test
	fun `has latest version is set correctly in template context if true`() {
		whenever(core.updateChecker.hasLatestVersion()).thenReturn(true)
		verifyVariableIsSet("hasLatestVersion", true)
	}

	@Test
	fun `has latest version is set correctly in template context if false`() {
		whenever(core.updateChecker.hasLatestVersion()).thenReturn(false)
		verifyVariableIsSet("hasLatestVersion", false)
	}

	@Test
	fun `latest edition is set in template context`() {
		whenever(core.updateChecker.latestEdition).thenReturn(1234L)
		verifyVariableIsSet("latestEdition", 1234L)
	}

	@Test
	fun `latest version is set in template context`() {
		whenever(core.updateChecker.latestVersion).thenReturn(Version(1, 2, 3))
		verifyVariableIsSet("latestVersion", Version(1, 2, 3))
	}

	@Test
	fun `latest version time is set in template context`() {
		whenever(core.updateChecker.latestVersionDate).thenReturn(12345L)
		verifyVariableIsSet("latestVersionTime", 12345L)
	}

	private fun createNotification(time: Long) = mock<Notification>().apply {
		whenever(createdTime).thenReturn(time)
	}

	@Test
	fun `notifications are set in template context`() {
		val notifications = listOf(createNotification(3000), createNotification(1000), createNotification(2000))
		whenever(webInterface.getNotifications(currentSone)).thenReturn(notifications)
		verifyVariableMatches("notifications", contains(notifications[1], notifications[2], notifications[0]))
	}

	@Test
	fun `notification hash is set in template context`() {
		val notifications = listOf(createNotification(3000), createNotification(1000), createNotification(2000))
		whenever(webInterface.getNotifications(currentSone)).thenReturn(notifications)
		verifyVariableIsSet("notificationHash", listOf(notifications[1], notifications[2], notifications[0]).hashCode())
	}

	@Test
	fun `handleRequest method is called`() {
		var called = false
		val page = object : SoneTemplatePage("path.html", template, webInterface, true) {
			override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
				called = true
			}
		}
		page.processTemplate(freenetRequest, templateContext)
		assertThat(called, equalTo(true))
	}

	@Test
	fun `redirect does not happen if login is not required`() {
		val page = SoneTemplatePage("page.html", template, webInterface, false)
		assertThat(page.getRedirectTarget(freenetRequest), nullValue())
	}

	@Test
	fun `redirect does not happen if sone is logged in`() {
		assertThat(page.getRedirectTarget(freenetRequest), nullValue())
	}

	@Test
	fun `redirect does happen if sone is not logged in`() {
		unsetCurrentSone()
		request("index.html", GET)
		assertThat(page.getRedirectTarget(freenetRequest), equalTo("login.html?target=index.html"))
	}

	@Test
	fun `redirect does happen with parameters encoded correctly if sone is not logged in`() {
		unsetCurrentSone()
		request("index.html", GET)
		addHttpRequestParameter("foo", "b=r")
		addHttpRequestParameter("baz", "q&o")
		assertThat(page.getRedirectTarget(freenetRequest), anyOf(
				equalTo("login.html?target=index.html%3Ffoo%3Db%253Dr%26baz%3Dq%2526o"),
				equalTo("login.html?target=index.html%3Fbaz%3Dq%2526o%26foo%3Db%253Dr")
		))
	}

	@Test
	fun `full access requirement is correctly forwarded from the preferences if false`() {
		assertThat(page.isFullAccessOnly, equalTo(false))
	}

	@Test
	fun `full access requirement is correctly forwarded from the preferences if true`() {
		core.preferences.isRequireFullAccess = true
		assertThat(page.isFullAccessOnly, equalTo(true))
	}

	@Test
	fun `page is disabled if full access is required but request does not have full access`() {
		core.preferences.isRequireFullAccess = true
		assertThat(page.isEnabled(toadletContext), equalTo(false))
	}

	@Test
	fun `page is disabled if login is required but there is no current sone`() {
		unsetCurrentSone()
		assertThat(page.isEnabled(toadletContext), equalTo(false))
	}

	@Test
	fun `page is enabled if login is required and there is a current sone`() {
		assertThat(page.isEnabled(toadletContext), equalTo(true))
	}

	@Test
	fun `page is enabled if full access is required and request has full access and login is required and there is a current sone`() {
		core.preferences.isRequireFullAccess = true
		whenever(toadletContext.isAllowedFullAccess).thenReturn(true)
		assertThat(page.isEnabled(toadletContext), equalTo(true))
	}

	@Test
	fun `page is enabled if no full access is required and login is not required`() {
		SoneTemplatePage("path.html", template, webInterface, false).let { page ->
			assertThat(page.isEnabled(toadletContext), equalTo(true))
		}
	}

}
