package net.pterodactylus.sone.web.page

import net.pterodactylus.util.template.*
import java.io.*
import javax.inject.*

class TemplateRenderer @Inject constructor(private val templateContextFactory: TemplateContextFactory) {

	fun render(template: Template, processor: (TemplateContext) -> Unit = {}): String =
			templateContextFactory.createTemplateContext().let { templateContext ->
				templateContext.mergeContext(template.initialContext)
				processor(templateContext)
				StringWriter().also { template.render(templateContext, it) }.toString()
			}

}
