package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.core.SoneRescuer
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [RescuePage].
 */
class RescuePageTest : WebPageTest() {

	private val page = RescuePage(template, webInterface)

	private val soneRescuer = mock<SoneRescuer>()

	override fun getPage() = page

	@Before
	fun setupSoneRescuer() {
		whenever(core.getSoneRescuer(currentSone)).thenReturn(soneRescuer)
	}

	@Test
	fun `get request sets rescuer in template context`() {
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["soneRescuer"], equalTo<Any>(soneRescuer))
	}

	@Test
	fun `post request redirects to rescue page`() {
		setMethod(POST)
		verifyRedirect("rescue.html")
	}

	@Test
	fun `post request with fetch and invalid edition starts next fetch`() {
		setMethod(POST)
		addHttpRequestPart("fetch", "true")
		verifyRedirect("rescue.html") {
			verify(soneRescuer, never()).setEdition(anyLong())
			verify(soneRescuer).startNextFetch()
		}
	}

	@Test
	fun `post request with fetch and valid edition sets edition and starts next fetch`() {
		setMethod(POST)
		addHttpRequestPart("fetch", "true")
		addHttpRequestPart("edition", "123")
		verifyRedirect("rescue.html") {
			verify(soneRescuer).setEdition(123L)
			verify(soneRescuer).startNextFetch()
		}
	}

}
