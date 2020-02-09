/**
 * Sone - HistogramRendererTest.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.template

import com.codahale.metrics.*
import net.pterodactylus.sone.freenet.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.jsoup.*
import org.jsoup.nodes.*
import org.junit.*
import java.util.*

/**
 * Unit test for [HistogramRenderer].
 */
class HistogramRendererTest {

	private val translation = object : Translation {
		override val currentLocale = Locale.ENGLISH
		override fun translate(key: String) = "Metric Name".takeIf { key == "Page.Metrics.TestHistogram.Title" } ?: ""
	}
	private val metricRenderer = HistogramRenderer()
	private val templateContext = TemplateContext().apply {
		addFilter("html", HtmlFilter())
		addFilter("duration", DurationFormatFilter())
		addFilter("l10n", L10nFilter(translation))
	}

	@Test
	fun `histogram is rendered as table row`() {
		createAndVerifyTableRow {
			assertThat(it.nodeName(), equalTo("tr"))
		}
	}

	@Test
	fun `histogram has eleven columns`() {
		createAndVerifyTableRow {
			assertThat(it.getElementsByTag("td"), hasSize(11))
		}
	}

	@Test
	fun `first column contains translated metric name`() {
		createAndVerifyTableRow(mapOf("name" to "test.histogram")) {
			assertThat(it.getElementsByTag("td")[0].text(), equalTo("Metric Name"))
		}
	}

	@Test
	fun `second column is numeric`() {
		verifyColumnIsNumeric(1)
	}

	@Test
	fun `second column contains count`() {
		createAndVerifyTableRow {
			assertThat(it.getElementsByTag("td")[1].text(), equalTo("2001"))
		}
	}

	@Test
	fun `third column is numeric`() {
		verifyColumnIsNumeric(2)
	}

	@Test
	fun `third column contains min value`() {
		createAndVerifyTableRow {
			assertThat(it.getElementsByTag("td")[2].text(), equalTo("2.0ms"))
		}
	}

	@Test
	fun `fourth column is numeric`() {
		verifyColumnIsNumeric(3)
	}

	@Test
	fun `fourth column contains max value`() {
		createAndVerifyTableRow {
			assertThat(it.getElementsByTag("td")[3].text(), equalTo("998.0ms"))
		}
	}

	@Test
	fun `fifth column is numeric`() {
		verifyColumnIsNumeric(4)
	}

	@Test
	fun `fifth column contains mean value`() {
		createAndVerifyTableRow {
			assertThat(it.getElementsByTag("td")[4].text(), equalTo("492.7ms"))
		}
	}

	@Test
	fun `sixth column is numeric`() {
		verifyColumnIsNumeric(5)
	}

	@Test
	fun `sixth column contains median value`() {
		createAndVerifyTableRow {
			assertThat(it.getElementsByTag("td")[5].text(), equalTo("483.6ms"))
		}
	}

	@Test
	fun `seventh column is numeric`() {
		verifyColumnIsNumeric(6)
	}

	@Test
	fun `seventh column contains 75th percentile`() {
		createAndVerifyTableRow {
			assertThat(it.getElementsByTag("td")[6].text(), equalTo("740.9ms"))
		}
	}

	@Test
	fun `eighth column is numeric`() {
		verifyColumnIsNumeric(7)
	}

	@Test
	fun `eighth column contains 95th percentile`() {
		createAndVerifyTableRow {
			assertThat(it.getElementsByTag("td")[7].text(), equalTo("940.9ms"))
		}
	}

	@Test
	fun `ninth column is numeric`() {
		verifyColumnIsNumeric(8)
	}

	@Test
	fun `ninth column contains 98th percentile`() {
		createAndVerifyTableRow {
			assertThat(it.getElementsByTag("td")[8].text(), equalTo("975.6ms"))
		}
	}

	@Test
	fun `tenth column is numeric`() {
		verifyColumnIsNumeric(9)
	}

	@Test
	fun `tenth column contains 99th percentile`() {
		createAndVerifyTableRow {
			assertThat(it.getElementsByTag("td")[9].text(), equalTo("991.6ms"))
		}
	}

	@Test
	fun `eleventh column is numeric`() {
		verifyColumnIsNumeric(10)
	}

	@Test
	fun `eleventh column contains 99,9th percentile`() {
		createAndVerifyTableRow {
			assertThat(it.getElementsByTag("td")[10].text(), equalTo("998.0ms"))
		}
	}

	private fun createAndVerifyTableRow(parameters: Map<String, Any?>? = null, verify: (Element) -> Unit) =
			metricRenderer.format(templateContext, histogram, parameters)
					.let { "<table id='t'>$it</table>" }
					.let(Jsoup::parseBodyFragment)
					.getElementById("t").child(0).child(0)
					.let(verify)

	private fun verifyColumnIsNumeric(column: Int) =
			createAndVerifyTableRow {
				assertThat(it.getElementsByTag("td")[column].classNames(), hasItem("numeric"))
			}

}

private val random = Random(1)
private val histogram = MetricRegistry().histogram("test.histogram") { Histogram(SlidingWindowReservoir(1028)) }.apply {
	(0..2000).map { random.nextInt(1_000_000) }.forEach(this::update)
}
