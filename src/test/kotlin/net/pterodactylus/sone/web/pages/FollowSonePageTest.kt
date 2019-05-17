package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.*
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [FollowSonePage].
 */
class FollowSonePageTest: WebPageTest(::FollowSonePage) {

	@Test
	fun `page returns correct path`() {
	    assertThat(page.path, equalTo("followSone.html"))
	}

	@Test
	fun `page requires login`() {
	    assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
	    whenever(l10n.getString("Page.FollowSone.Title")).thenReturn("follow sone page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("follow sone page title"))
	}

	@Test
	fun `get request does not redirect`() {
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `a single sone can be followed`() {
		setMethod(POST)
		val sone = mock<Sone>()
		addSone("sone-id", sone)
		addHttpRequestPart("sone", "sone-id")
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core).followSone(currentSone, "sone-id")
			verify(core).markSoneKnown(sone)
		}
	}

	@Test
	fun `multiple sones can be followed`() {
		setMethod(POST)
		val firstSone = mock<Sone>()
		addSone("sone-id1", firstSone)
		val secondSone = mock<Sone>()
		addSone("sone-id2", secondSone)
		addHttpRequestPart("sone", "sone-id1,sone-id2")
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core).followSone(currentSone, "sone-id1")
			verify(core).followSone(currentSone, "sone-id2")
			verify(core).markSoneKnown(firstSone)
			verify(core).markSoneKnown(secondSone)
		}
	}

	@Test
	fun `a non-existing sone is not followed`() {
		setMethod(POST)
		addHttpRequestPart("sone", "sone-id")
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core, never()).followSone(ArgumentMatchers.eq(currentSone), anyString())
			verify(core, never()).markSoneKnown(any<Sone>())
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<FollowSonePage>(), notNullValue())
	}

}
