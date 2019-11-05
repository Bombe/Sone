package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [RescuePage].
 */
class RescuePageTest : WebPageTest(::RescuePage) {

	private val soneRescuer = mock<SoneRescuer>()

	@Before
	fun setupSoneRescuer() {
		whenever(core.getSoneRescuer(currentSone)).thenReturn(soneRescuer)
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("rescue.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.Rescue.Title", "rescue page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("rescue page title"))
	}

	@Test
	fun `get request sets rescuer in template context`() {
		verifyNoRedirect {
			assertThat(templateContext["soneRescuer"], equalTo<Any>(soneRescuer))
		}
	}

	@Test
	fun `post request redirects to rescue page`() {
		setMethod(POST)
		verifyRedirect("rescue.html")
	}

	@Test
	fun `post request with fetch starts next fetch`() {
		setMethod(POST)
		addHttpRequestPart("fetch", "true")
		verifyRedirect("rescue.html") {
			verify(soneRescuer).startNextFetch()
		}
	}

	@Test
	fun `post request with skipping fetch starts next skipping fetch`() {
		setMethod(POST)
		addHttpRequestPart("fetchSkip", "true")
		verifyRedirect("rescue.html") {
			verify(soneRescuer).startNextFetchWithSkip()
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<RescuePage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct menuname`() {
		assertThat(page.menuName, equalTo("Rescue"))
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/rescue.html"))
	}

}
