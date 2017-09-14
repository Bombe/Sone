package net.pterodactylus.sone.web.ajax

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [GetTranslationAjaxPage].
 */
class GetTranslationAjaxPageTest : JsonPageTest("getTranslation.ajax", requiresLogin = false, needsFormPassword = false, pageSupplier = ::GetTranslationAjaxPage) {

	@Test
	fun `translation is returned correctly`() {
		addTranslation("foo", "bar")
		addRequestParameter("key", "foo")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["value"].asText(), equalTo("bar"))
	}

}
