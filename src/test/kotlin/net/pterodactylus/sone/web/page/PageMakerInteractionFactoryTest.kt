package net.pterodactylus.sone.web.page

import com.google.inject.*
import freenet.clients.http.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

class PageMakerInteractionFactoryTest {

	private val pageMakerInteractionFactory: PageMakerInteractionFactory = DefaultPageMakerInteractionFactory()

	@Test
	fun `page maker interaction factory can be created by guice`() {
		val injector = Guice.createInjector()
		assertThat(injector.getInstance<PageMakerInteractionFactory>(), notNullValue())
	}

	@Test
	fun `page maker interaction sets page title correctly`() {
		val toadletContext = deepMock<ToadletContext>()
		pageMakerInteractionFactory.createPageMaker(toadletContext, "page title")
		verify(toadletContext.pageMaker).getPageNode("page title", toadletContext)
	}

}
