package net.pterodactylus.sone.web.pages

import com.codahale.metrics.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

@MenuName("Metrics")
@TemplatePath("/templates/metrics.html")
@ToadletPath("metrics.html")
class MetricsPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer, private val metricsRegistry: MetricRegistry) : SoneTemplatePage(webInterface, loaders, templateRenderer, "Page.Metrics.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		addHistogram(templateContext, "sone.parsing.duration", "soneParsingDuration")
		addHistogram(templateContext, "sone.insert.duration", "soneInsertDuration")
	}

	private fun addHistogram(templateContext: TemplateContext, metricName: String, variablePrefix: String) {
		metricsRegistry.histogram(metricName).also { histogram ->
			templateContext["${variablePrefix}Count"] = histogram.count
			histogram.snapshot.also { snapshot ->
				templateContext["${variablePrefix}Min"] = snapshot.min
				templateContext["${variablePrefix}Max"] = snapshot.max
				templateContext["${variablePrefix}Median"] = snapshot.median
				templateContext["${variablePrefix}Mean"] = snapshot.mean
				templateContext["${variablePrefix}Percentile75"] = snapshot.get75thPercentile()
				templateContext["${variablePrefix}Percentile95"] = snapshot.get95thPercentile()
				templateContext["${variablePrefix}Percentile98"] = snapshot.get98thPercentile()
				templateContext["${variablePrefix}Percentile99"] = snapshot.get99thPercentile()
				templateContext["${variablePrefix}Percentile999"] = snapshot.get999thPercentile()
			}
		}
	}

}
