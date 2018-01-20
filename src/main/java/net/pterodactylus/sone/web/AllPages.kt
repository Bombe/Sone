package net.pterodactylus.sone.web

import net.pterodactylus.sone.web.pages.AboutPage
import javax.inject.Inject

/**
 * Container for all web pages. This uses field injection because there are way too many pages
 * to sensibly use constructor injection.
 */
class AllPages {

	@Inject lateinit var aboutPage: AboutPage

}
