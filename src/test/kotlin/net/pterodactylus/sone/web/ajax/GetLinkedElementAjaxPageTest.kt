package net.pterodactylus.sone.web.ajax

import com.fasterxml.jackson.databind.JsonNode
import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.template.LinkedElementRenderFilter
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.utils.jsonArray
import net.pterodactylus.util.template.TemplateContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test
import org.mockito.ArgumentMatchers

/**
 * Unit test for [GetLinkedElementAjaxPage].
 */
class GetLinkedElementAjaxPageTest: JsonPageTest("getLinkedElement.ajax", requiresLogin = false, needsFormPassword = false) {

	private val linkedElementRenderFilter = mock<LinkedElementRenderFilter>()
	override var page: JsonPage = GetLinkedElementAjaxPage(webInterface, elementLoader, linkedElementRenderFilter)

	@Test
	fun `only loaded linked elements are returned`() {
	    addRequestParameter("elements", jsonArray("KSK@foo.png", "KSK@foo.jpg", "KSK@foo.html").toString())
		addLinkedElement("KSK@foo.png", true, false)
		addLinkedElement("KSK@foo.jpg", false, false)
		addLinkedElement("KSK@foo.html", false, true)
		whenever(linkedElementRenderFilter.format(ArgumentMatchers.any<TemplateContext>(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenAnswer { invocation ->
			when (invocation.getArgument<LinkedElement>(1).link) {
				"KSK@foo.jpg" -> "jpeg-image"
				"KSK@foo.html" -> "html-page"
				else -> null
			}
		}
		assertThat(json.get("linkedElements")!!.elements().asSequence().map { it.toMap() }.toList(), Matchers.containsInAnyOrder(
				mapOf<String, String?>("link" to "KSK@foo.jpg", "html" to "jpeg-image"),
				mapOf("link" to "KSK@foo.html", "html" to "html-page")
		))
	}

	private fun JsonNode.toMap() = fields().asSequence().map { it.key!! to if (it.value.isNull) null else it.value.asText()!! }.toMap()

}
