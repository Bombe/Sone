package net.pterodactylus.sone.template

import com.codahale.metrics.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.template.*

/**
 * [Filter] that renders a [Histogram] as a table row.
 */
class HistogramRenderer : Filter {

	override fun format(templateContext: TemplateContext, data: Any?, parameters: Map<String, Any?>?): Any? {
		templateContext["metricName"] = (parameters?.get("name") as String?)?.dotToCamel()?.let { "Page.Metrics.$it.Title" }
		(data as? Histogram)?.snapshot?.run {
			templateContext["count"] = data.count
			templateContext["min"] = min
			templateContext["max"] = max
			templateContext["mean"] = mean
			templateContext["median"] = median
			templateContext["percentile75"] = get75thPercentile()
			templateContext["percentile95"] = get95thPercentile()
			templateContext["percentile98"] = get98thPercentile()
			templateContext["percentile99"] = get99thPercentile()
			templateContext["percentile999"] = get999thPercentile()
		}
		return template.render(templateContext)
	}

}

private val template = """<tr>
	<td><% metricName|l10n|html></td>
	<td class="numeric"><% count|html></td>
	<td class="numeric"><% min|duration scale=='μs'|html></td>
	<td class="numeric"><% max|duration scale=='μs'|html></td>
	<td class="numeric"><% mean|duration scale=='μs'|html></td>
	<td class="numeric"><% median|duration scale=='μs'|html></td>
	<td class="numeric"><% percentile75|duration scale=='μs'|html></td>
	<td class="numeric"><% percentile95|duration scale=='μs'|html></td>
	<td class="numeric"><% percentile98|duration scale=='μs'|html></td>
	<td class="numeric"><% percentile99|duration scale=='μs'|html></td>
	<td class="numeric"><% percentile999|duration scale=='μs'|html></td>
</tr>""".asTemplate()

private fun String.dotToCamel() =
		split(".").joinToString("", transform = String::capitalize)
