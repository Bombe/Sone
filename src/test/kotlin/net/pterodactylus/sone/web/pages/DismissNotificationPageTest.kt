package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.baseInjector
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [DismissNotificationPage].
 */
class DismissNotificationPageTest: WebPageTest(::DismissNotificationPage) {

	private val notification = mock<Notification>()

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("dismissNotification.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page returns correct title`() {
		whenever(l10n.getString("Page.DismissNotification.Title")).thenReturn("dismiss notification page")
		assertThat(page.getPageTitle(soneRequest), equalTo("dismiss notification page"))
	}

	@Test
	fun `get request with invalid notification ID redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html")
	}

	@Test
	fun `get request with non-dismissible notification never dismisses the notification but redirects to return page`() {
		setMethod(POST)
		addNotification("notification-id", notification)
		addHttpRequestPart("notification", "notification-id")
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(notification, never()).dismiss()
		}
	}

	@Test
	fun `post request with dismissible notification dismisses the notification and redirects to return page`() {
		setMethod(POST)
		whenever(notification.isDismissable).thenReturn(true)
		addNotification("notification-id", notification)
		addHttpRequestPart("notification", "notification-id")
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(notification).dismiss()
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<DismissNotificationPage>(), notNullValue())
	}

}
