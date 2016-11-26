package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.web.WebTestUtils.redirectsTo
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
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

	@Test
	fun `get request does not redirect`() {
		request("", GET)
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `a single sone can be followed`() {
		request("", POST)
		val sone = mock<Sone>()
		addSone("sone-id", sone)
		addHttpRequestParameter("sone", "sone-id")
		addHttpRequestParameter("returnPage", "return.html")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
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
		addHttpRequestParameter("sone", "sone-id1,sone-id2")
		addHttpRequestParameter("returnPage", "return.html")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(core).followSone(currentSone, "sone-id1")
			verify(core).followSone(currentSone, "sone-id2")
			verify(core).markSoneKnown(firstSone)
			verify(core).markSoneKnown(secondSone)
		}
	}

	@Test
	fun `a non-existing sone is not followed`() {
		request("", POST)
		addHttpRequestParameter("sone", "sone-id")
		addHttpRequestParameter("returnPage", "return.html")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(core, never()).followSone(ArgumentMatchers.eq(currentSone), anyString())
			verify(core, never()).markSoneKnown(any<Sone>())
		}
	}

}
