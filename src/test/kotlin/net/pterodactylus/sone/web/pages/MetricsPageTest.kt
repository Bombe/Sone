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
	fun `metrics page lists stats about sone parsing durations`() {
		createHistogram("sone.parsing.duration")
		page.handleRequest(soneRequest, templateContext)
		verifyHistogram("soneParsingDuration")
	}

	@Test
	fun `metrice pags lists stats about sone insert durations`() {
		createHistogram("sone.insert.duration")
		page.handleRequest(soneRequest, templateContext)
		verifyHistogram("soneInsertDuration")
	}

	@Test
	fun `metrics page delivers correct histogram size`() {
		val histogram = metricRegistry.histogram("sone.parsing.duration")
		(0..4000).forEach(histogram::update)
		page.handleRequest(soneRequest, templateContext)
		assertThat(templateContext["soneParsingDurationCount"] as Long, equalTo(4001L))
	}

	private fun verifyHistogram(name: String) {
		assertThat(templateContext["${name}Count"] as Long, equalTo(5L))
		assertThat(templateContext["${name}Min"] as Long, equalTo(1L))
		assertThat(templateContext["${name}Max"] as Long, equalTo(10L))
		assertThat(templateContext["${name}Median"] as Double, equalTo(8.0))
		assertThat(templateContext["${name}Percentile75"] as Double, equalTo(9.0))
		assertThat(templateContext["${name}Percentile95"] as Double, equalTo(10.0))
		assertThat(templateContext["${name}Percentile98"] as Double, equalTo(10.0))
		assertThat(templateContext["${name}Percentile99"] as Double, equalTo(10.0))
		assertThat(templateContext["${name}Percentile999"] as Double, equalTo(10.0))
	}

	private fun createHistogram(name: String) = metricRegistry.histogram(name).run {
		update(10)
		update(9)
		update(1)
		update(1)
		update(8)
	}

}
