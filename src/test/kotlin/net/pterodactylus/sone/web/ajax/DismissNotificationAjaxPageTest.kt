package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.notify.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

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
