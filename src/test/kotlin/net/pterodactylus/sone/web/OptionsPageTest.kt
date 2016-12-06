package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.SoneOptions.DefaultSoneOptions
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.FOLLOWED
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.TRUSTED
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.WRITING
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test

/**
 * Unit test for [OptionsPage].
 */
class OptionsPageTest : WebPageTest() {

	private val page = OptionsPage(template, webInterface)

	override fun getPage() = page

	@Before
	fun setupPreferences() {
		core.preferences.insertionDelay = 1
		core.preferences.charactersPerPost = 50
		core.preferences.fcpFullAccessRequired = WRITING
		core.preferences.imagesPerPage = 4
		core.preferences.isFcpInterfaceActive = true
		core.preferences.isRequireFullAccess = true
		core.preferences.negativeTrust = 7
		core.preferences.positiveTrust = 8
		core.preferences.postCutOffLength = 51
		core.preferences.postsPerPage = 10
		core.preferences.trustComment = "11"
	}

	@Before
	fun setupSoneOptions() {
		whenever(currentSone.options).thenReturn(DefaultSoneOptions().apply {
			isAutoFollow = true
			isShowNewPostNotifications = true
			isShowNewReplyNotifications = true
			isShowNewSoneNotifications = true
			isSoneInsertNotificationEnabled = true
			loadLinkedImages = FOLLOWED
			showCustomAvatars = FOLLOWED
		})
	}

	@Test
	fun `get request stores all preferences in the template context`() {
		request("", GET)
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["auto-follow"], equalTo<Any>(true))
		assertThat(templateContext["show-notification-new-sones"], equalTo<Any>(true))
		assertThat(templateContext["show-notification-new-posts"], equalTo<Any>(true))
		assertThat(templateContext["show-notification-new-replies"], equalTo<Any>(true))
		assertThat(templateContext["enable-sone-insert-notifications"], equalTo<Any>(true))
		assertThat(templateContext["load-linked-images"], equalTo<Any>("FOLLOWED"))
		assertThat(templateContext["show-custom-avatars"], equalTo<Any>("FOLLOWED"))
		assertThat(templateContext["insertion-delay"], equalTo<Any>(1))
		assertThat(templateContext["characters-per-post"], equalTo<Any>(50))
		assertThat(templateContext["fcp-full-access-required"], equalTo<Any>(1))
		assertThat(templateContext["images-per-page"], equalTo<Any>(4))
		assertThat(templateContext["fcp-interface-active"], equalTo<Any>(true))
		assertThat(templateContext["require-full-access"], equalTo<Any>(true))
		assertThat(templateContext["negative-trust"], equalTo<Any>(7))
		assertThat(templateContext["positive-trust"], equalTo<Any>(8))
		assertThat(templateContext["post-cut-off-length"], equalTo<Any>(51))
		assertThat(templateContext["posts-per-page"], equalTo<Any>(10))
		assertThat(templateContext["trust-comment"], equalTo<Any>("11"))
	}

	@Test
	fun `get request without sone does not store sone-specific preferences in the template context`() {
		request("", GET)
		unsetCurrentSone()
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["auto-follow"], nullValue())
		assertThat(templateContext["show-notification-new-sones"], nullValue())
		assertThat(templateContext["show-notification-new-posts"], nullValue())
		assertThat(templateContext["show-notification-new-replies"], nullValue())
		assertThat(templateContext["enable-sone-insert-notifications"], nullValue())
		assertThat(templateContext["load-linked-images"], nullValue())
		assertThat(templateContext["show-custom-avatars"], nullValue())
	}

	private fun <T> verifyThatOptionCanBeSet(option: String, setValue: Any?, expectedValue: T, getter: () -> T) {
		request("", POST)
		addHttpRequestParameter("show-custom-avatars", "ALWAYS")
		addHttpRequestParameter("load-linked-images", "ALWAYS")
		addHttpRequestParameter(option, setValue.toString())
		verifyRedirect("options.html") {
			assertThat(getter(), equalTo(expectedValue))
		}
	}

	@Test
	fun `auto-follow option can be set`() {
		verifyThatOptionCanBeSet("auto-follow", "checked", true) { currentSone.options.isAutoFollow }
	}

	@Test
	fun `show new sone notification option can be set`() {
		verifyThatOptionCanBeSet("show-notification-new-sones", "checked", true) { currentSone.options.isShowNewSoneNotifications }
	}

	@Test
	fun `show new post notification option can be set`() {
		verifyThatOptionCanBeSet("show-notification-new-posts", "checked", true) { currentSone.options.isShowNewPostNotifications }
	}

	@Test
	fun `show new reply notification option can be set`() {
		verifyThatOptionCanBeSet("show-notification-new-replies", "checked", true) { currentSone.options.isShowNewReplyNotifications }
	}

	@Test
	fun `enable sone insert notifications option can be set`() {
		verifyThatOptionCanBeSet("enable-sone-insert-notifications", "checked", true) { currentSone.options.isSoneInsertNotificationEnabled }
	}

	@Test
	fun `load linked images option can be set`() {
		verifyThatOptionCanBeSet("load-linked-images", "TRUSTED", TRUSTED) { currentSone.options.loadLinkedImages }
	}

	@Test
	fun `show custom avatar option can be set`() {
		verifyThatOptionCanBeSet("show-custom-avatars", "TRUSTED", TRUSTED) { currentSone.options.showCustomAvatars }
	}

	private fun verifyThatWrongValueForPreferenceIsDetected(name: String, value: String) {
		unsetCurrentSone()
		request("", POST)
		addHttpRequestParameter(name, value)
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["fieldErrors"] as Iterable<*>, hasItem(name))
	}

	private fun <T> verifyThatPreferencesCanBeSet(name: String, setValue: String?, expectedValue: T, getter: () -> T) {
		unsetCurrentSone()
		request("", POST)
		addHttpRequestParameter(name, setValue)
		verifyRedirect("options.html") {
			assertThat(getter(), equalTo(expectedValue))
		}
	}

	@Test
	fun `insertion delay can not be set to less than 0 seconds`() {
		verifyThatWrongValueForPreferenceIsDetected("insertion-delay", "-1")
	}

	@Test
	fun `insertion delay can be set to 0 seconds`() {
		verifyThatPreferencesCanBeSet("insertion-delay", "0", 0) { core.preferences.insertionDelay }
	}

	@Test
	fun `setting insertion to an invalid value will reset it`() {
		verifyThatPreferencesCanBeSet("insertion-delay", "foo", 60) { core.preferences.insertionDelay }
	}

	@Test
	fun `characters per post can not be set to less than -1`() {
		verifyThatWrongValueForPreferenceIsDetected("characters-per-post", "-2")
	}

	@Test
	fun `characters per post can be set to -1`() {
		verifyThatPreferencesCanBeSet("characters-per-post", "-1", -1) { core.preferences.charactersPerPost }
	}

	@Test
	fun `characters per post can not be set to 0`() {
		verifyThatWrongValueForPreferenceIsDetected("characters-per-post", "0")
	}

	@Test
	fun `characters per post can not be set to 49`() {
		verifyThatWrongValueForPreferenceIsDetected("characters-per-post", "49")
	}

	@Test
	fun `characters per post can be set to 50`() {
		verifyThatPreferencesCanBeSet("characters-per-post", "50", 50) { core.preferences.charactersPerPost }
	}

	@Test
	fun `fcp full acess required option can be set to always`() {
		verifyThatPreferencesCanBeSet("fcp-full-access-required", "2", FullAccessRequired.ALWAYS) { core.preferences.fcpFullAccessRequired }
	}

	@Test
	fun `fcp full acess required option can be set to writing`() {
		verifyThatPreferencesCanBeSet("fcp-full-access-required", "1", FullAccessRequired.WRITING) { core.preferences.fcpFullAccessRequired }
	}

	@Test
	fun `fcp full acess required option can be set to no`() {
		verifyThatPreferencesCanBeSet("fcp-full-access-required", "0", FullAccessRequired.NO) { core.preferences.fcpFullAccessRequired }
	}

	@Test
	fun `fcp full acess required option is not changed if invalid value is set`() {
		verifyThatPreferencesCanBeSet("fcp-full-access-required", "foo", FullAccessRequired.WRITING) { core.preferences.fcpFullAccessRequired }
	}

	@Test
	fun `images per page can not be set to 0`() {
		verifyThatWrongValueForPreferenceIsDetected("images-per-page", "0")
	}

	@Test
	fun `images per page can be set to 1`() {
		verifyThatPreferencesCanBeSet("images-per-page", "1", 1) { core.preferences.imagesPerPage }
	}

	@Test
	fun `images per page is set to 9 if invalid value is requested`() {
		verifyThatPreferencesCanBeSet("images-per-page", "foo", 9) { core.preferences.imagesPerPage }
	}

	@Test
	fun `fcp interface can be set to true`() {
		verifyThatPreferencesCanBeSet("fcp-interface-active", "checked", true) { core.preferences.isFcpInterfaceActive }
	}

	@Test
	fun `fcp interface can be set to false`() {
		verifyThatPreferencesCanBeSet("fcp-interface-active", null, false) { core.preferences.isFcpInterfaceActive }
	}

	@Test
	fun `require full access can be set to true`() {
		verifyThatPreferencesCanBeSet("require-full-access", "checked", true) { core.preferences.isRequireFullAccess }
	}

	@Test
	fun `require full access can be set to false`() {
		verifyThatPreferencesCanBeSet("require-full-access", null, false) { core.preferences.isRequireFullAccess }
	}

	@Test
	fun `negative trust can not be set to -101`() {
		verifyThatWrongValueForPreferenceIsDetected("negative-trust", "-101")
	}

	@Test
	fun `negative trust can be set to -100`() {
		verifyThatPreferencesCanBeSet("negative-trust", "-100", -100) { core.preferences.negativeTrust }
	}

	@Test
	fun `negative trust can be set to 100`() {
		verifyThatPreferencesCanBeSet("negative-trust", "100", 100) { core.preferences.negativeTrust }
	}

	@Test
	fun `negative trust can not be set to 101`() {
		verifyThatWrongValueForPreferenceIsDetected("negative-trust", "101")
	}

	@Test
	fun `negative trust is set to default on invalid value`() {
		verifyThatPreferencesCanBeSet("negative-trust", "invalid", -25) { core.preferences.negativeTrust }
	}

	@Test
	fun `positive trust can not be set to -1`() {
		verifyThatWrongValueForPreferenceIsDetected("positive-trust", "-1")
	}

	@Test
	fun `positive trust can be set to 0`() {
		verifyThatPreferencesCanBeSet("positive-trust", "0", 0) { core.preferences.positiveTrust }
	}

	@Test
	fun `positive trust can be set to 100`() {
		verifyThatPreferencesCanBeSet("positive-trust", "100", 100) { core.preferences.positiveTrust }
	}

	@Test
	fun `positive trust can not be set to 101`() {
		verifyThatWrongValueForPreferenceIsDetected("positive-trust", "101")
	}

	@Test
	fun `positive trust is set to default on invalid value`() {
		verifyThatPreferencesCanBeSet("positive-trust", "invalid", 75) { core.preferences.positiveTrust }
	}

	@Test
	fun `post cut off length can not be set to -49`() {
		verifyThatWrongValueForPreferenceIsDetected("post-cut-off-length", "-49")
	}

	@Test
	fun `post cut off length can be set to 50`() {
		verifyThatPreferencesCanBeSet("post-cut-off-length", "50", 50) { core.preferences.postCutOffLength }
	}

	@Test
	fun `post cut off length is set to default on invalid value`() {
		verifyThatPreferencesCanBeSet("post-cut-off-length", "invalid", 200) { core.preferences.postCutOffLength }
	}

	@Test
	fun `posts per page can not be set to 0`() {
		verifyThatWrongValueForPreferenceIsDetected("posts-per-page", "-49")
	}

	@Test
	fun `posts per page can be set to 1`() {
		verifyThatPreferencesCanBeSet("posts-per-page", "1", 1) { core.preferences.postsPerPage }
	}

	@Test
	fun `posts per page is set to default on invalid value`() {
		verifyThatPreferencesCanBeSet("posts-per-page", "invalid", 10) { core.preferences.postsPerPage }
	}

	@Test
	fun `trust comment can be set`() {
		verifyThatPreferencesCanBeSet("trust-comment", "trust", "trust") { core.preferences.trustComment }
	}

	@Test
	fun `trust comment is set to default when set to empty value`() {
		verifyThatPreferencesCanBeSet("trust-comment", "", "Set from Sone Web Interface") { core.preferences.trustComment }
	}

}
