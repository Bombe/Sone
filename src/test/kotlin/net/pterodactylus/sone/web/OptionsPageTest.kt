package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.ALWAYS
import net.pterodactylus.sone.web.WebTestUtils.redirectsTo
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

/**
 * Unit test for [OptionsPage].
 */
class OptionsPageTest : WebPageTest() {

	private val page = OptionsPage(template, webInterface)

	@Test
	fun `options page sets correct value for load-linked-images`() {
		request("", POST)
		addHttpRequestParameter("show-custom-avatars", "ALWAYS")
		addHttpRequestParameter("load-linked-images", "ALWAYS")
		expectedException.expect(redirectsTo("options.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			assertThat(currentSone.options.loadLinkedImages, `is`(ALWAYS))
			verify(core, times(2)).touchConfiguration()
		}
	}

}
