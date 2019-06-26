package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.SoneOptions.*
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.*
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.*
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.ALWAYS
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

/**
 * Unit test for [OptionsPage].
 */
class OptionsPageTest : WebPageTest(::OptionsPage) {

	@Before
	fun setupPreferences() {
		core.preferences.newInsertionDelay = 1
		core.preferences.newCharactersPerPost = 50
		core.preferences.newFcpFullAccessRequired = WRITING
		core.preferences.newImagesPerPage = 4
		core.preferences.newFcpInterfaceActive = true
		core.preferences.newRequireFullAccess = true
		core.preferences.newNegativeTrust = 7
		core.preferences.newPositiveTrust = 8
		core.preferences.newPostCutOffLength = 51
		core.preferences.newPostsPerPage = 10
		core.preferences.newTrustComment = "11"
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
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("options.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.Options.Title", "options page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("options page title"))
	}

	@Test
	fun `get request stores all preferences in the template context`() {
		verifyNoRedirect {
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
	}

	@Test
	fun `get request without sone does not store sone-specific preferences in the template context`() {
		unsetCurrentSone()
		verifyNoRedirect {
			assertThat(templateContext["auto-follow"], nullValue())
			assertThat(templateContext["show-notification-new-sones"], nullValue())
			assertThat(templateContext["show-notification-new-posts"], nullValue())
			assertThat(templateContext["show-notification-new-replies"], nullValue())
			assertThat(templateContext["enable-sone-insert-notifications"], nullValue())
			assertThat(templateContext["load-linked-images"], nullValue())
			assertThat(templateContext["show-custom-avatars"], nullValue())
		}
	}

	private fun <T> verifyThatOptionCanBeSet(option: String, setValue: Any?, expectedValue: T, getter: () -> T) {
		setMethod(POST)
		addHttpRequestPart("show-custom-avatars", "ALWAYS")
		addHttpRequestPart("load-linked-images", "ALWAYS")
		setValue?.also { addHttpRequestPart(option, it.toString()) }
		verifyRedirect("options.html") {
			assertThat(getter(), equalTo(expectedValue))
		}
	}

	@Test
	fun `auto-follow option can be set`() {
		verifyThatOptionCanBeSet("auto-follow", "checked", true) { currentSone.options.isAutoFollow }
	}

	@Test
	fun `auto-follow option can be unset`() {
		verifyThatOptionCanBeSet("auto-follow", null, false) { currentSone.options.isAutoFollow }
	}

	@Test
	fun `show new sone notification option can be set`() {
		verifyThatOptionCanBeSet("show-notification-new-sones", "checked", true) { currentSone.options.isShowNewSoneNotifications }
	}

	@Test
	fun `show new sone notification option can be unset`() {
		verifyThatOptionCanBeSet("" +
				"", null, false) { currentSone.options.isShowNewSoneNotifications }
	}

	@Test
	fun `show new post notification option can be set`() {
		verifyThatOptionCanBeSet("show-notification-new-posts", "checked", true) { currentSone.options.isShowNewPostNotifications }
	}

	@Test
	fun `show new post notification option can be unset`() {
		verifyThatOptionCanBeSet("show-notification-new-posts", null, false) { currentSone.options.isShowNewPostNotifications }
	}

	@Test
	fun `show new reply notification option can be set`() {
		verifyThatOptionCanBeSet("show-notification-new-replies", "checked", true) { currentSone.options.isShowNewReplyNotifications }
	}

	@Test
	fun `show new reply notification option can be unset`() {
		verifyThatOptionCanBeSet("show-notification-new-replies", null, false) { currentSone.options.isShowNewReplyNotifications }
	}

	@Test
	fun `enable sone insert notifications option can be set`() {
		verifyThatOptionCanBeSet("enable-sone-insert-notifications", "checked", true) { currentSone.options.isSoneInsertNotificationEnabled }
	}

	@Test
	fun `enable sone insert notifications option can be unset`() {
		verifyThatOptionCanBeSet("enable-sone-insert-notifications", null, false) { currentSone.options.isSoneInsertNotificationEnabled }
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
		setMethod(POST)
		addHttpRequestPart(name, value)
		verifyNoRedirect {
			assertThat(templateContext["fieldErrors"] as Iterable<*>, hasItem(name))
		}
	}

	private fun <T> verifyThatPreferencesCanBeSet(name: String, setValue: String?, expectedValue: T, getter: () -> T) {
		unsetCurrentSone()
		setMethod(POST)
		setValue?.also { addHttpRequestPart(name, it) }
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
		verifyThatPreferencesCanBeSet("fcp-full-access-required", "2", ALWAYS) { core.preferences.fcpFullAccessRequired }
	}

	@Test
	fun `fcp full acess required option can be set to writing`() {
		verifyThatPreferencesCanBeSet("fcp-full-access-required", "1", WRITING) { core.preferences.fcpFullAccessRequired }
	}

	@Test
	fun `fcp full acess required option can be set to no`() {
		verifyThatPreferencesCanBeSet("fcp-full-access-required", "0", NO) { core.preferences.fcpFullAccessRequired }
	}

	@Test
	fun `fcp full acess required option is not changed if invalid value is set`() {
		verifyThatPreferencesCanBeSet("fcp-full-access-required", "foo", WRITING) { core.preferences.fcpFullAccessRequired }
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
		verifyThatPreferencesCanBeSet("fcp-interface-active", "checked", true) { core.preferences.fcpInterfaceActive }
	}

	@Test
	fun `fcp interface can be set to false`() {
		verifyThatPreferencesCanBeSet("fcp-interface-active", null, false) { core.preferences.fcpInterfaceActive }
	}

	@Test
	fun `require full access can be set to true`() {
		verifyThatPreferencesCanBeSet("require-full-access", "checked", true) { core.preferences.requireFullAccess }
	}

	@Test
	fun `require full access can be set to false`() {
		verifyThatPreferencesCanBeSet("require-full-access", null, false) { core.preferences.requireFullAccess }
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

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<OptionsPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct menuname`() {
		assertThat(page.menuName, equalTo("Options"))
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/options.html"))
	}

}
