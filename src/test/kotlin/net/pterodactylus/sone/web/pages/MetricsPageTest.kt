/**
 * Sone - MetricsPageTest.kt - Copyright © 2019 David ‘Bombe’ Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.web.pages

import com.codahale.metrics.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

class MetricsPageTest : WebPageTest() {

	private val metricRegistry = MetricRegistry()
	override val page by lazy { MetricsPage(webInterface, loaders, templateRenderer, metricRegistry) }

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("metrics.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.Metrics.Title", "metrics page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("metrics page title"))
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<MetricsPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with the correct menuname`() {
		assertThat(page.menuName, equalTo("Metrics"))
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/metrics.html"))
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `metrics page stores histograms in template context`() {
		createHistogram("sone.random.duration2")
		createHistogram("sone.random.duration1")
		page.handleRequest(soneRequest, templateContext)
		val histograms = templateContext["histograms"] as Map<String, Histogram>
		assertThat(histograms.entries.map { it.key to it.value }, containsInAnyOrder(
				"sone.random.duration1" to metricRegistry.histogram("sone.random.duration1"),
				"sone.random.duration2" to metricRegistry.histogram("sone.random.duration2")
		))
	}

	private fun createHistogram(name: String) = metricRegistry.histogram(name).run {
		update(10)
		update(9)
		update(1)
		update(1)
		update(8)
	}

}
