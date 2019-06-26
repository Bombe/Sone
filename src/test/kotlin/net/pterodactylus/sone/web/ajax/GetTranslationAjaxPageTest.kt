package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test

/**
 * Unit test for [GetTranslationAjaxPage].
 */
class GetTranslationAjaxPageTest : JsonPageTest("getTranslation.ajax", requiresLogin = false, needsFormPassword = false, pageSupplier = ::GetTranslationAjaxPage) {

	@Test
	fun `translation is returned correctly`() {
		addTranslation("foo", "bar")
		addRequestParameter("key", "foo")
		assertThatJsonIsSuccessful()
		assertThat(json["value"]?.asText(), equalTo("bar"))
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<GetTranslationAjaxPage>(), notNullValue())
	}

}
