package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.baseInjector
import net.pterodactylus.util.notify.Notification
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [DismissNotificationAjaxPage].
 */
class DismissNotificationAjaxPageTest : JsonPageTest("dismissNotification.ajax", requiresLogin = false, pageSupplier = ::DismissNotificationAjaxPage) {

	@Test
	fun `request without notification returns invalid-notification-id`() {
		assertThatJsonFailed("invalid-notification-id")
	}

	@Test
	fun `request to dismiss non-dismissable notification results in not-dismissable`() {
		val notification = mock<Notification>()
		addNotification(notification, "foo")
		addRequestParameter("notification", "foo")
		assertThatJsonFailed("not-dismissable")
	}

	@Test
	fun `request to dismiss dismissable notification dismisses notification`() {
		val notification = mock<Notification>().apply { whenever(isDismissable).thenReturn(true) }
		addNotification(notification, "foo")
		addRequestParameter("notification", "foo")
		assertThatJsonIsSuccessful()
		verify(notification).dismiss()
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<DismissNotificationAjaxPage>(), notNullValue())
	}

}
