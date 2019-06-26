package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

class ErrorPagesTest {

	private val webPageTest = WebPageTest()

	private fun testPage(page: (WebInterface, Loaders, TemplateRenderer) -> FreenetTemplatePage, test: (FreenetTemplatePage) -> Unit) =
			with(webPageTest) {
				test(page(webInterface, loaders, templateRenderer))
			}

	@Test
	fun `invalid page returns correct path`() {
		testPage(::InvalidPage) { page ->
			assertThat(page.path, equalTo("invalid.html"))
		}
	}

	@Test
	fun `invalid page returns correct title`() {
		testPage(::InvalidPage) { page ->
			assertThat(page.getPageTitle(webPageTest.freenetRequest), equalTo("Page.Invalid.Title"))
		}
	}

	@Test
	fun `invalid page is annotated with correct template path`() {
		testPage(::InvalidPage) { page ->
			assertThat(page.templatePath, equalTo("/templates/invalid.html"))
		}
	}

	@Test
	fun `no permission page returns correct path`() {
		testPage(::NoPermissionPage) { page ->
			assertThat(page.path, equalTo("noPermission.html"))
		}
	}

	@Test
	fun `no permission page returns correct title`() {
		testPage(::NoPermissionPage) { page ->
			assertThat(page.getPageTitle(webPageTest.freenetRequest), equalTo("Page.NoPermission.Title"))
		}
	}

	@Test
	fun `no permission page is annotated with correct template path`() {
		testPage(::NoPermissionPage) { page ->
			assertThat(page.templatePath, equalTo("/templates/noPermission.html"))
		}
	}

	@Test
	fun `empty image title page returns correct path`() {
		testPage(::EmptyImageTitlePage) { page ->
			assertThat(page.path, equalTo("emptyImageTitle.html"))
		}
	}

	@Test
	fun `empty image title page returns correct page title`() {
		testPage(::EmptyImageTitlePage) { page ->
			assertThat(page.getPageTitle(webPageTest.freenetRequest), equalTo("Page.EmptyImageTitle.Title"))
		}
	}

	@Test
	fun `empty image title page is annotated with correct template path`() {
		testPage(::EmptyImageTitlePage) { page ->
			assertThat(page.templatePath, equalTo("/templates/emptyImageTitle.html"))
		}
	}

	@Test
	fun `empty album title page returns correct path`() {
		testPage(::EmptyAlbumTitlePage) { page ->
			assertThat(page.path, equalTo("emptyAlbumTitle.html"))
		}
	}

	@Test
	fun `empty album title page returns correct page title`() {
		testPage(::EmptyAlbumTitlePage) { page ->
			assertThat(page.getPageTitle(webPageTest.freenetRequest), equalTo("Page.EmptyAlbumTitle.Title"))
		}
	}

	@Test
	fun `empty album title page is annotated with correct template path`() {
		testPage(::EmptyAlbumTitlePage) { page ->
			assertThat(page.templatePath, equalTo("/templates/emptyAlbumTitle.html"))
		}
	}

}
