package net.pterodactylus.sone.web

import com.google.inject.*
import freenet.clients.http.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.page.*
import org.junit.*
import org.junit.rules.*
import org.mockito.Mockito.*
import kotlin.test.Test

class PageToadletRegistryTest {

	private val pageMaker = mock<PageMaker>()
	private val toadletContainer = mock<ToadletContainer>()
	private val pageToadletFactory = mock<PageToadletFactory>()
	private val sonePlugin = mock<SonePlugin>()

	private val injector = Guice.createInjector(
			PageMaker::class.isProvidedBy(pageMaker),
			ToadletContainer::class.isProvidedBy(toadletContainer),
			PageToadletFactory::class.isProvidedBy(pageToadletFactory),
			SonePlugin::class.isProvidedBy(sonePlugin)
	)

	@JvmField
	@Rule
	val expectedException: ExpectedException = ExpectedException.none()
	private val pageToadletRegistry = injector.getInstance<PageToadletRegistry>()

	@Test
	fun `registry adds navigation category to page maker`() {
		pageToadletRegistry.registerToadlets()
		verify(pageMaker).addNavigationCategory("/Sone/index.html", "Navigation.Menu.Sone.Name", "Navigation.Menu.Sone.Tooltip", sonePlugin)
	}

	private val page = TestPage<FreenetRequest>()

	@Test
	fun `adding a page without menuname will add it correctly`() {
		val toadletWithoutMenuname = createPageToadlet()
		whenever(pageToadletFactory.createPageToadlet(page)).thenReturn(toadletWithoutMenuname)
		pageToadletRegistry.addPage(page)
		pageToadletRegistry.registerToadlets()
		verify(toadletContainer).register(toadletWithoutMenuname, null, "/Sone/", true, false)
	}

	@Test
	fun `adding a page with menuname will add it correctly`() {
		val toadletWithMenuname = createPageToadlet("Test")
		whenever(pageToadletFactory.createPageToadlet(page)).thenReturn(toadletWithMenuname)
		pageToadletRegistry.addPage(page)
		pageToadletRegistry.registerToadlets()
		verify(toadletContainer).register(toadletWithMenuname, "Navigation.Menu.Sone.Name", "/Sone/", true, "Navigation.Menu.Sone.Item.Test.Name", "Navigation.Menu.Sone.Item.Test.Tooltip", false, toadletWithMenuname)
	}

	@Test
	fun `adding a page after registering will throw an exception`() {
		val toadletWithMenuname = createPageToadlet("Test")
		whenever(pageToadletFactory.createPageToadlet(page)).thenReturn(toadletWithMenuname)
		pageToadletRegistry.registerToadlets()
		expectedException.expect(IllegalStateException::class.java)
		pageToadletRegistry.addPage(page)
	}

	@Test
	fun `unregistering toadlets will remove category link`() {
		pageToadletRegistry.unregisterToadlets()
		verify(pageMaker).removeNavigationCategory("Navigation.Menu.Sone.Name")
	}

	@Test
	fun `unregistering toadlets will unregister them from the container`() {
		val toadletWithMenuname = createPageToadlet("Test")
		whenever(pageToadletFactory.createPageToadlet(page)).thenReturn(toadletWithMenuname)
		pageToadletRegistry.addPage(page)
		pageToadletRegistry.registerToadlets()
		pageToadletRegistry.unregisterToadlets()
		verify(toadletContainer).unregister(toadletWithMenuname)
	}

	@Test
	fun `adding a debug page will not add it to the container`() {
		val toadlet = createPageToadlet()
		whenever(pageToadletFactory.createPageToadlet(page)).thenReturn(toadlet)
		pageToadletRegistry.addDebugPage(page)
		pageToadletRegistry.registerToadlets()
		verify(toadletContainer, never()).register(toadlet, null, "/Sone/", true, false)
	}

	@Test
	fun `adding a debug page and activating debug mode will add it to the container`() {
		val toadlet = createPageToadlet()
		whenever(pageToadletFactory.createPageToadlet(page)).thenReturn(toadlet)
		pageToadletRegistry.addDebugPage(page)
		pageToadletRegistry.registerToadlets()
		pageToadletRegistry.activateDebugMode()
		verify(toadletContainer).register(toadlet, null, "/Sone/", true, false)
	}

	@Test
	fun `adding a debug page and activating debug mode twice will add it to the container once`() {
		val toadlet = createPageToadlet()
		whenever(pageToadletFactory.createPageToadlet(page)).thenReturn(toadlet)
		pageToadletRegistry.addDebugPage(page)
		pageToadletRegistry.registerToadlets()
		pageToadletRegistry.activateDebugMode()
		pageToadletRegistry.activateDebugMode()
		verify(toadletContainer, times(1)).register(toadlet, null, "/Sone/", true, false)
	}

	@Test
	fun `debug pages are ungegistered from the container`() {
		val toadlet = createPageToadlet()
		whenever(pageToadletFactory.createPageToadlet(page)).thenReturn(toadlet)
		pageToadletRegistry.addDebugPage(page)
		pageToadletRegistry.registerToadlets()
		pageToadletRegistry.activateDebugMode()
		pageToadletRegistry.unregisterToadlets()
		verify(toadletContainer).unregister(toadlet)
	}

	@Test
	fun `inactive debug pages are not ungegistered from the container`() {
		val toadlet = createPageToadlet()
		whenever(pageToadletFactory.createPageToadlet(page)).thenReturn(toadlet)
		pageToadletRegistry.addDebugPage(page)
		pageToadletRegistry.registerToadlets()
		pageToadletRegistry.unregisterToadlets()
		verify(toadletContainer, never()).unregister(toadlet)
	}

	@Test
	fun `debug page can not be added after registering`() {
		val toadlet = createPageToadlet()
		whenever(pageToadletFactory.createPageToadlet(page)).thenReturn(toadlet)
		pageToadletRegistry.registerToadlets()
		expectedException.expect(IllegalStateException::class.java)
		pageToadletRegistry.addDebugPage(page)
	}

	private fun createPageToadlet(menuName: String? = null) =
			mock<PageToadlet>().apply {
				whenever(this.path()).thenReturn("/Sone/")
				whenever(this.menuName).thenReturn(menuName)
			}

}
