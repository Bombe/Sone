package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [FollowSonePage].
 */
class FollowSonePageTest : WebPageTest() {

	private val page = FollowSonePage(template, webInterface)

	override fun getPage() = page

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
		assertThat(page.getPageTitle(freenetRequest), equalTo("follow sone page title"))
	}

	@Test
	fun `get request does not redirect`() {
		request("", GET)
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `a single sone can be followed`() {
		request("", POST)
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
		request("", POST)
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
		request("", POST)
		addHttpRequestPart("sone", "sone-id")
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core, never()).followSone(ArgumentMatchers.eq(currentSone), anyString())
			verify(core, never()).markSoneKnown(any<Sone>())
		}
	}

}
