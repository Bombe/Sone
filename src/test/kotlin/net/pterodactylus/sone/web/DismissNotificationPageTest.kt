package net.pterodactylus.sone.web

import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.WebTestUtils.redirectsTo
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.web.Method.GET
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import kotlin.test.fail

/**
 * Unit test for [DismissNotificationPage].
 */
class DismissNotificationPageTest : WebPageTest() {

	private val page = DismissNotificationPage(template, webInterface)
	private val notification = mock<Notification>()

	@Test
	fun `get request with invalid notification ID redirects to return page`() {
		request("", GET)
		addHttpRequestParameter("returnPage", "return.html")
		expectedException.expect(redirectsTo("return.html"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `get request with dismissible notification dismisses the notification and redirects to return page`() {
		request("", GET)
		addNotification("notification-id", notification)
		addHttpRequestParameter("notification", "notification-id")
		addHttpRequestParameter("returnPage", "return.html")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
			fail()
		} finally {
			verify(notification, never()).dismiss()
		}
	}

	@Test
	fun `get request with non dismissible notification redirects to return page`() {
		request("", GET)
		whenever(notification.isDismissable).thenReturn(true)
		addNotification("notification-id", notification)
		addHttpRequestParameter("notification", "notification-id")
		addHttpRequestParameter("returnPage", "return.html")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
			fail()
		} finally {
			verify(notification).dismiss()
		}
	}

}
