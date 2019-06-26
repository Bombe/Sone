package net.pterodactylus.sone.web.page

import com.google.inject.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.template.*
import net.pterodactylus.util.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.rules.*

class TemplateRendererTest {

	@Rule
	@JvmField
	val expectedException: ExpectedException = ExpectedException.none()
	private val templateContextFactory = TemplateContextFactory()
	private val templateRenderer = TemplateRenderer(templateContextFactory)

	@Test
	fun `renderer can render template`() {
		val template = "foo".asTemplate()
		val rendered = templateRenderer.render(template)
		assertThat(rendered, equalTo("foo"))
	}

	@Test
	fun `renderer merges template contexts from template and context factory`() {
		templateContextFactory.addTemplateObject("a", 1)
		val template = "<%a><%b>".asTemplate()
		template.initialContext.set("b", 2)
		val rendered = templateRenderer.render(template)
		assertThat(rendered, equalTo("12"))
	}

	@Test
	fun `template context can be processed`() {
		templateContextFactory.addTemplateObject("a", 1)
		val template = "<%a><%b><%c>".asTemplate()
		template.initialContext.set("b", 2)
		val rendered = templateRenderer.render(template) { templateContext -> templateContext.set("c", 3) }
		assertThat(rendered, equalTo("123"))
	}

	@Test
	fun `redirect exceptions are thrown`() {
		expectedException.expect(RedirectException::class.java)
		templateRenderer.render(Template()) { _ -> throw RedirectException("foo") }
	}

	@Test
	fun `template renderer can be created by guice`() {
		val injector = Guice.createInjector(
				TemplateContextFactory::class.isProvidedByMock()
		)
		assertThat(injector.getInstance<TemplateRenderer>(), notNullValue())
	}

}
