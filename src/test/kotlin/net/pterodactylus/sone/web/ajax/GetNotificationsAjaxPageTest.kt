package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.main.SonePlugin
import net.pterodactylus.sone.test.argumentCaptor
import net.pterodactylus.sone.test.get
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.notify.TemplateNotification
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.template.TemplateContextFactory
import net.pterodactylus.util.version.Version
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import java.io.Writer

/**
 * Unit test for [GetNotificationsAjaxPage].
 */
class GetNotificationsAjaxPageTest : JsonPageTest("getNotifications.ajax", requiresLogin = false, needsFormPassword = false, pageSupplier = ::GetNotificationsAjaxPage) {

	private val testNotifications = listOf(
			createNotification("n1", 2000, "t1", 5000, true),
			createNotification("n2", 1000, "t2", 6000, false),
			createNotification("n3", 3000, "t3", 7000, true)
	)

	private fun createNotification(id: String, createdTime: Long, text: String, lastUpdatedTime: Long, dismissable: Boolean): Notification {
		return mock<Notification>().apply {
			whenever(this.id).thenReturn(id)
			whenever(this.createdTime).thenReturn(createdTime)
			whenever(this.lastUpdatedTime).thenReturn(lastUpdatedTime)
			whenever(this.isDismissable).thenReturn(dismissable)
			whenever(render(any())).then { it.get<Writer>(0).write(text) }
		}
	}

	@Test
	fun `notification hash is calculated correctly`() {
		testNotifications.forEach { addNotification(it) }
		assertThatJsonIsSuccessful()
		assertThat(json["notificationHash"]?.asInt(), equalTo(listOf(1, 0, 2).map(testNotifications::get).hashCode()))
	}

	@Test
	fun `options are included correctly`() {
		assertThatJsonIsSuccessful()
		assertThat(json["options"]!!["ShowNotification/NewSones"].asBoolean(), equalTo(true))
		assertThat(json["options"]!!["ShowNotification/NewPosts"].asBoolean(), equalTo(true))
		assertThat(json["options"]!!["ShowNotification/NewReplies"].asBoolean(), equalTo(true))
	}

	@Test
	fun `options are included correctly when all false`() {
		currentSone.options.isShowNewSoneNotifications = false
		currentSone.options.isShowNewPostNotifications = false
		currentSone.options.isShowNewReplyNotifications = false
		assertThatJsonIsSuccessful()
		assertThat(json["options"]!!["ShowNotification/NewSones"].asBoolean(), equalTo(false))
		assertThat(json["options"]!!["ShowNotification/NewPosts"].asBoolean(), equalTo(false))
		assertThat(json["options"]!!["ShowNotification/NewReplies"].asBoolean(), equalTo(false))
	}

	@Test
	fun `options are not included if user is not logged in`() {
		unsetCurrentSone()
		assertThatJsonIsSuccessful()
		assertThat(json["options"]?.toList(), empty())
	}

	@Test
	fun `notifications are rendered correctly`() {
		testNotifications.forEach { addNotification(it) }
		assertThatJsonIsSuccessful()
		assertThat(json["notifications"]!!.toList().map { node -> listOf("id", "text", "createdTime", "lastUpdatedTime", "dismissable").map { it to node.get(it).asText() }.toMap() }, containsInAnyOrder(
				mapOf("id" to "n1", "createdTime" to "2000", "lastUpdatedTime" to "5000", "dismissable" to "true", "text" to "t1"),
				mapOf("id" to "n2", "createdTime" to "1000", "lastUpdatedTime" to "6000", "dismissable" to "false", "text" to "t2"),
				mapOf("id" to "n3", "createdTime" to "3000", "lastUpdatedTime" to "7000", "dismissable" to "true", "text" to "t3")
		))
	}

	@Test
	fun `template notifications are rendered correctly`() {
		whenever(webInterface.templateContextFactory).thenReturn(TemplateContextFactory())
		whenever(updateChecker.hasLatestVersion()).thenReturn(true)
		whenever(updateChecker.latestEdition).thenReturn(999)
		whenever(updateChecker.latestVersion).thenReturn(Version(0, 1, 2))
		whenever(updateChecker.latestVersionDate).thenReturn(998)
		val templateNotification = mock<TemplateNotification>().apply {
			whenever(id).thenReturn("n4")
			whenever(createdTime).thenReturn(4000)
			whenever(templateContext).thenReturn(TemplateContext())
			whenever(render(any(), any())).then { it.get<Writer>(1).write("t4") }
		}
		testNotifications.forEach { addNotification(it) }
		addNotification(templateNotification)
		assertThatJsonIsSuccessful()
		assertThat(json["notifications"]!!.last()["text"].asText(), equalTo("t4"))
		val templateContext = argumentCaptor<TemplateContext>()
		verify(templateNotification).render(templateContext.capture(), any())
		assertThat(templateContext.value["core"], equalTo<Any>(core))
		assertThat(templateContext.value["currentSone"], equalTo<Any>(currentSone))
		assertThat(templateContext.value["localSones"], equalTo<Any>(core.localSones))
		assertThat(templateContext.value["request"], equalTo<Any>(freenetRequest))
		assertThat(templateContext.value["currentVersion"], equalTo<Any>(SonePlugin.getPluginVersion()))
		assertThat(templateContext.value["hasLatestVersion"], equalTo<Any>(true))
		assertThat(templateContext.value["latestEdition"], equalTo<Any>(999L))
		assertThat(templateContext.value["latestVersion"], equalTo<Any>(Version(0, 1, 2)))
		assertThat(templateContext.value["latestVersionTime"], equalTo<Any>(998L))
		assertThat(templateContext.value["notification"], equalTo<Any>(templateNotification))
	}

}
