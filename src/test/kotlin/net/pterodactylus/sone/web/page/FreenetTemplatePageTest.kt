package net.pterodactylus.sone.web.page

import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

class FreenetTemplatePageTest {

	@Test
	fun `path is exposed correctly`() {
		val page = FreenetTemplatePage("/test/path", null, null, null)
		assertThat(page.path, equalTo("/test/path"))
	}

	@Test
	fun `getPageTitle() default implementation returns null`() {
		val page = FreenetTemplatePage("/test/path", null, null, null)
		assertThat(page.getPageTitle(mock()), nullValue())
	}

	@Test
	fun `isPrefixPage() default implementation returns false`() {
		val page = FreenetTemplatePage("/test/path", null, null, null)
		assertThat(page.isPrefixPage, equalTo(false))
	}

	@Test
	fun `getStylesheets() default implementation returns empty collection`() {
		val page = FreenetTemplatePage("/test/path", null, null, null)
		assertThat(page.styleSheets, empty())
	}

	@Test
	fun `getShortcutIcon() default implementation returns null`() {
		val page = FreenetTemplatePage("/test/path", null, null, null)
		assertThat(page.shortcutIcon, nullValue())
	}

	@Test
	fun `getRedirectTarget() default implementation returns null`() {
		val page = FreenetTemplatePage("/test/path", null, null, null)
		assertThat(page.getRedirectTarget(mock()), nullValue())
	}

	@Test
	fun `getAdditionalLinkNodes() default implementation returns empty collection`() {
		val page = FreenetTemplatePage("/test/path", null, null, null)
		assertThat(page.getAdditionalLinkNodes(mock()), empty())
	}

	@Test
	fun `isFullAccessOnly() default implementation returns false`() {
		val page = FreenetTemplatePage("/test/path", null, null, null)
		assertThat(page.isFullAccessOnly, equalTo(false))
	}

	@Test
	fun `isLinkExcepted() default implementation returns false`() {
		val page = FreenetTemplatePage("/test/path", null, null, null)
		assertThat(page.isLinkExcepted(mock()), equalTo(false))
	}

	@Test
	fun `isEnabled() returns true if full access only is false`() {
		val page = FreenetTemplatePage("/test/path", null, null, null)
		assertThat(page.isEnabled(mock()), equalTo(true))
	}

	@Test
	fun `isEnabled() returns false if full access only is true`() {
		val page = object : FreenetTemplatePage("/test/path", null, null, null) {
			override fun isFullAccessOnly() = true
		}
		assertThat(page.isEnabled(mock()), equalTo(false))
	}

}
