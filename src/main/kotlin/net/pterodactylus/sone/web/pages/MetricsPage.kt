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
		metricsRegistry.histogram("sone.parsing.duration").snapshot.also { snapshot ->
			templateContext["soneParsingDurationCount"] = snapshot.size()
			templateContext["soneParsingDurationMin"] = snapshot.min
			templateContext["soneParsingDurationMax"] = snapshot.max
			templateContext["soneParsingDurationMedian"] = snapshot.median
			templateContext["soneParsingDurationMean"] = snapshot.mean
			templateContext["soneParsingDurationPercentile75"] = snapshot.get75thPercentile()
			templateContext["soneParsingDurationPercentile95"] = snapshot.get95thPercentile()
			templateContext["soneParsingDurationPercentile98"] = snapshot.get98thPercentile()
			templateContext["soneParsingDurationPercentile99"] = snapshot.get99thPercentile()
			templateContext["soneParsingDurationPercentile999"] = snapshot.get999thPercentile()
		}
	}

}
