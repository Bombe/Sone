package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.WebInterface
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import java.lang.System.currentTimeMillis

/**
 * Unit test for [GetTimesAjaxPage].
 */
class GetTimesAjaxPageTest {

	private val webInterface = deepMock<WebInterface>()

	@Test
	fun timestampInTheFutureIsTranslatedCorrectly() {
		whenever(webInterface.l10n.getString("View.Time.InTheFuture")).thenReturn("in the future")
		val time = GetTimesAjaxPage.getTime(webInterface, currentTimeMillis() + 1000)
		assertThat(time.text, equalTo("in the future"))
	}

	@Test
	fun timestampAFewSecondsAgoIsTranslatedCorrectly() {
		whenever(webInterface.l10n.getString("View.Time.AFewSecondsAgo")).thenReturn("a few seconds ago")
		val time = GetTimesAjaxPage.getTime(webInterface, currentTimeMillis() - 1000)
		assertThat(time.text, equalTo("a few seconds ago"))
	}

}
