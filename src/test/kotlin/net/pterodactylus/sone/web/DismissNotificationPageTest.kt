package net.pterodactylus.sone.web

import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.web.Method.GET
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [DismissNotificationPage].
 */
class DismissNotificationPageTest: WebPageTest() {

	private val page = DismissNotificationPage(template, webInterface)
	private val notification = mock<Notification>()

	override fun getPage() = page

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
		assertThat(page.getPageTitle(freenetRequest), equalTo("dismiss notification page"))
	}

	@Test
	fun `get request with invalid notification ID redirects to return page`() {
		request("", GET)
		addHttpRequestParameter("returnPage", "return.html")
		verifyRedirect("return.html")
	}

	@Test
	fun `get request with non-dismissible notification never dismisses the notification but redirects to return page`() {
		request("", GET)
		addNotification("notification-id", notification)
		addHttpRequestParameter("notification", "notification-id")
		addHttpRequestParameter("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(notification, never()).dismiss()
		}
	}

	@Test
	fun `get request with dismissible notification dismisses the notification and redirects to return page`() {
		request("", GET)
		whenever(notification.isDismissable).thenReturn(true)
		addNotification("notification-id", notification)
		addHttpRequestParameter("notification", "notification-id")
		addHttpRequestParameter("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(notification).dismiss()
		}
	}

}
