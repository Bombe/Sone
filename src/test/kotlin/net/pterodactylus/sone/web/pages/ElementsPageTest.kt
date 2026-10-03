package net.pterodactylus.sone.web.pages

import com.google.inject.Guice.createInjector
import kotlin.test.Test
import net.pterodactylus.sone.core.DefaultElementLoader
import net.pterodactylus.sone.core.DefaultElementLoader.Rejected
import net.pterodactylus.sone.core.DefaultElementLoader.RejectionReason.InvalidMediaType
import net.pterodactylus.sone.core.DefaultElementLoader.RejectionReason.SizeTooLarge
import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.isProvidedByDeepMock
import net.pterodactylus.sone.test.isProvidedByMock
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.TemplateRenderer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasEntry

class ElementsPageTest : WebPageTest() {

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("elements.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.ElementLoader.Title", "elements page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("elements page title"))
	}

	@Test
	fun `page lists loading elements`() {
		whenever(elementsLoader.loadingElements).thenReturn(setOf("key 1", "key 2"))
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["loadingElements"] as Collection<*>, containsInAnyOrder("key 1", "key 2"))
	}

	@Test
	fun `page lists loaded elements`() {
		whenever(elementsLoader.loadedElements).thenReturn(setOf(LinkedElement("key 3"), LinkedElement("key 4")))
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["loadedElements"] as Collection<*>, containsInAnyOrder(LinkedElement("key 3"), LinkedElement("key 4")))
	}

	@Test
	fun `page lists rejected elements`() {
		whenever(elementsLoader.rejectedElements).thenReturn(mapOf(
			"key 5" to Rejected(InvalidMediaType, "application/test"),
			"key 6" to Rejected(SizeTooLarge, 1234567L),
		))
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["rejectedElements"] as Map<*, *>, allOf(
			hasEntry("key 5", Rejected(InvalidMediaType, "application/test")),
			hasEntry("key 6", Rejected(SizeTooLarge, 1234567L)),
		))
	}

	@Test
	fun `page lists failed elements`() {
		whenever(elementsLoader.failedElements).thenReturn(setOf("key 7", "key 8"))
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["failedElements"] as Collection<*>, containsInAnyOrder("key 7", "key 8"))
	}

	@Test
	fun `page can be created by Guice`() {
		val injector = createInjector(
			WebInterface::class.isProvidedByDeepMock(),
			TemplateRenderer::class.isProvidedByMock(),
			DefaultElementLoader::class.isProvidedByMock(),
		)
		injector.getInstance<ElementsPage>()
	}

	private val elementsLoader: DefaultElementLoader = mock<DefaultElementLoader>()
	override val page: SoneTemplatePage
		get() = ElementsPage(webInterface, loaders, templateRenderer, elementsLoader)

}
