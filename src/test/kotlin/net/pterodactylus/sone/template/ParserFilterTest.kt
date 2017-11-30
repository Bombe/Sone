package net.pterodactylus.sone.template

import com.google.inject.Guice
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.isProvidedByMock
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.text.SoneTextParser
import net.pterodactylus.sone.text.SoneTextParserContext
import net.pterodactylus.util.template.TemplateContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.sameInstance
import org.junit.Test
import org.mockito.ArgumentCaptor.forClass
import org.mockito.Mockito.`when`
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify

/**
 * Unit test for [ParserFilter].
 */
class ParserFilterTest {

	companion object {
		private const val SONE_IDENTITY = "nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI"
	}

	private val core = mock<Core>()
	private val sone = setupSone(SONE_IDENTITY)
	private val soneTextParser = mock<SoneTextParser>()
	private val templateContext = TemplateContext()
	private val parameters = mutableMapOf<String, Any?>()
	private val filter = ParserFilter(core, soneTextParser)

	private fun setupSone(identity: String): Sone {
		val sone = mock<Sone>()
		`when`(sone.id).thenReturn(identity)
		`when`(core.getSone(identity)).thenReturn(sone)
		return sone
	}

	@Test
	fun `parsing null returns an empty iterable`() {
		assertThat(filter.format(templateContext, null, mutableMapOf()) as Iterable<*>, emptyIterable())
	}

	@Test
	fun `given sone is used to create parser context`() {
		setupSoneAndVerifyItIsUsedInContext(sone, sone)
	}

	@Test
	fun `sone with given sone ID is used to create parser context`() {
		setupSoneAndVerifyItIsUsedInContext(SONE_IDENTITY, sone)
	}

	private fun setupSoneAndVerifyItIsUsedInContext(soneOrSoneId: Any, sone: Sone) {
		parameters.put("sone", soneOrSoneId)
		filter.format(templateContext, "text", parameters)
		val context = forClass(SoneTextParserContext::class.java)
		verify(soneTextParser).parse(eq<String>("text") ?: "", context.capture())
		assertThat(context.value.postingSone, `is`(sone))
	}

	@Test
	fun `parser filter can be created by guice`() {
	    val injector = Guice.createInjector(
			    Core::class.isProvidedByMock(),
			    SoneTextParser::class.isProvidedByMock()
	    )
		assertThat(injector.getInstance<ParserFilter>(), notNullValue())
	}

	@Test
	fun `parser filter is created as singleton`() {
		val injector = Guice.createInjector(
				Core::class.isProvidedByMock(),
				SoneTextParser::class.isProvidedByMock()
		)
		val firstInstance = injector.getInstance<ParserFilter>()
		val secondInstance = injector.getInstance<ParserFilter>()
		assertThat(firstInstance, sameInstance(secondInstance))

	}

}
