package net.pterodactylus.sone.web

import freenet.clients.http.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.web.*
import java.util.concurrent.atomic.*
import javax.inject.*

private const val soneMenu = "Navigation.Menu.Sone"
private const val soneMenuName = "$soneMenu.Name"

class PageToadletRegistry @Inject constructor(
		private val pageMaker: PageMaker,
		private val toadletContainer: ToadletContainer,
		private val sonePlugin: SonePlugin,
		private val pageToadletFactory: PageToadletFactory
) {

	private val pages = mutableListOf<Page<FreenetRequest>>()
	private val debugPages = mutableListOf<Page<FreenetRequest>>()
	private val registeredToadlets = mutableListOf<PageToadlet>()
	private val registered = AtomicBoolean(false)
	private val debugActivated = AtomicBoolean(false)

	fun addPage(page: Page<FreenetRequest>) {
		if (registered.get()) throw IllegalStateException()
		pages += page
	}

	fun addDebugPage(page: Page<FreenetRequest>) {
		if (registered.get()) throw IllegalStateException()
		debugPages += page
	}

	fun registerToadlets() {
		registered.set(true)
		pageMaker.addNavigationCategory("/Sone/index.html", soneMenuName, "$soneMenu.Tooltip", sonePlugin)
		addPages()
	}

	private fun addPages(pages: List<Page<FreenetRequest>> = this.pages) =
			pages
					.map { pageToadletFactory.createPageToadlet(it) }
					.onEach(registeredToadlets::plusAssign)
					.forEach { pageToadlet ->
						if (pageToadlet.menuName == null) {
							registerToadletWithoutMenuname(pageToadlet)
						} else {
							registerToadletWithMenuname(pageToadlet)
						}
					}

	private fun registerToadletWithoutMenuname(pageToadlet: PageToadlet) =
			toadletContainer.register(pageToadlet, null, pageToadlet.path(), true, false)

	private fun registerToadletWithMenuname(pageToadlet: PageToadlet) =
			toadletContainer.register(pageToadlet, soneMenuName, pageToadlet.path(), true, "$soneMenu.Item.${pageToadlet.menuName}.Name", "$soneMenu.Item.${pageToadlet.menuName}.Tooltip", false, pageToadlet)

	fun unregisterToadlets() {
		pageMaker.removeNavigationCategory(soneMenuName)
		registeredToadlets.forEach(toadletContainer::unregister)
	}

	fun activateDebugMode() {
		if (!debugActivated.get()) {
			addPages(debugPages)
			debugActivated.set(true)
		}
	}

}
