package net.pterodactylus.sone.web

import com.google.inject.Guice
import net.pterodactylus.sone.main.SonePlugin.PluginHomepage
import net.pterodactylus.sone.main.SonePlugin.PluginVersion
import net.pterodactylus.sone.main.SonePlugin.PluginYear
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.isProvidedByMock
import net.pterodactylus.util.template.Template
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.junit.Test

/**
 * Test for [AllPages].
 */
class AllPagesTest {

	private val injector by lazy {
		Guice.createInjector(
				Template::class.isProvidedByMock(),
				WebInterface::class.isProvidedByMock(),
				PluginVersion::class.isProvidedByMock(),
				PluginYear::class.isProvidedByMock(),
				PluginHomepage::class.isProvidedByMock()
		)!!
	}
	private val allPages by lazy { injector.getInstance<AllPages>() }

	@Test
	fun `about page can be injected`() {
		assertThat(allPages.aboutPage, notNullValue())
	}

}
