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
		templateContext["histograms"] = metricsRegistry.histograms
	}

}
