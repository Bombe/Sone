package net.pterodactylus.sone.web.ajax

import com.fasterxml.jackson.databind.JsonNode
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.Sone.SoneStatus.downloading
import net.pterodactylus.sone.data.Sone.SoneStatus.inserting
import net.pterodactylus.sone.freenet.L10nFilter
import net.pterodactylus.sone.freenet.L10nText
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.text.TimeText
import net.pterodactylus.sone.text.TimeTextConverter
import net.pterodactylus.sone.utils.jsonArray
import net.pterodactylus.util.notify.Notification
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasEntry
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import java.util.TimeZone

/**
 * Unit test for [GetStatusAjaxPage].
 */
class GetStatusAjaxPageTest: JsonPageTest("getStatus.ajax", requiresLogin = false, needsFormPassword = false) {

	private val timeTextConverter = mock<TimeTextConverter>()
	private val l10nFilter = mock<L10nFilter>()
	override var page: JsonPage = GetStatusAjaxPage(webInterface, elementLoader, timeTextConverter, l10nFilter, TimeZone.getTimeZone("UTC"))

	@Before
	fun setupTimeTextConverter() {
		whenever(timeTextConverter.getTimeText(anyLong())).thenAnswer { TimeText(L10nText(it.getArgument<Long>(0).toString()), it.getArgument(0)) }
		whenever(l10nFilter.format(any(), any(), any())).thenAnswer { it.getArgument<L10nText>(1).text }
	}

	@Test
	fun `page returns correct attribute “loggedIn” if sone is logged in`() {
		assertThat(json.get("loggedIn")?.asText(), equalTo("true"))
	}

	@Test
	fun `page returns correct attribute “loggedIn” if sone is not logged in`() {
		unsetCurrentSone()
		assertThat(json.get("loggedIn")?.asText(), equalTo("false"))
	}

	@Test
	fun `page returns options for sone if sone is logged in`() {
		assertThat(json.get("options")?.toMap(), allOf(
				hasEntry("ShowNotification/NewSones", "true"),
				hasEntry("ShowNotification/NewPosts", "true"),
				hasEntry("ShowNotification/NewReplies", "true")
		))
	}

	@Test
	fun `page returns empty options if sone is not logged in`() {
		unsetCurrentSone()
		assertThat(json.get("options"), emptyIterable())
	}

	@Test
	fun `page returns empty sones object if no sone is logged in and no sones parameter is given`() {
		unsetCurrentSone()
		assertThat(json.get("sones"), emptyIterable())
	}

	@Test
	fun `page returns a sones object with the current sone if not other sones parameter is given`() {
		assertThat(json.get("sones")!!.elements().asSequence().map { it.toMap() }.toList(), containsInAnyOrder(
				mapOf<String, String?>("id" to "soneId", "name" to "Sone_Id", "local" to "true", "status" to "idle", "modified" to "false", "locked" to "false", "lastUpdatedUnknown" to "false", "lastUpdated" to "Jan 1, 1970, 00:00:01", "lastUpdatedText" to "1000")
		))
	}

	@Test
	fun `page returns some sones objects with the current sone and some sones given as sones parameter`() {
		addSone(deepMock<Sone>().mock("sone1", "Sone 1", false, 2000, downloading))
		addSone(deepMock<Sone>().mock("sone3", "Sone 3", true, 3000, inserting))
		addRequestParameter("soneIds", "sone1,sone2,sone3")
		assertThat(json.get("sones")!!.elements().asSequence().map { it.toMap() }.toList(), containsInAnyOrder(
				mapOf<String, String?>("id" to "soneId", "name" to "Sone_Id", "local" to "true", "status" to "idle", "modified" to "false", "locked" to "false", "lastUpdatedUnknown" to "false", "lastUpdated" to "Jan 1, 1970, 00:00:01", "lastUpdatedText" to "1000"),
				mapOf("id" to "sone1", "name" to "Sone 1", "local" to "false", "status" to "downloading", "modified" to "false", "locked" to "false", "lastUpdatedUnknown" to "false", "lastUpdated" to "Jan 1, 1970, 00:00:02", "lastUpdatedText" to "2000"),
				mapOf("id" to "sone3", "name" to "Sone 3", "local" to "true", "status" to "inserting", "modified" to "false", "locked" to "false", "lastUpdatedUnknown" to "false", "lastUpdated" to "Jan 1, 1970, 00:00:03", "lastUpdatedText" to "3000")
		))
	}

	@Test
	fun `page returns correct notifications hash`() {
		val notifications = listOf(
				mock<Notification>().apply { whenever(this.createdTime).thenReturn(2000) },
				mock<Notification>().apply { whenever(this.createdTime).thenReturn(1000) }
		)
		notifications.forEachIndexed { index, notification -> addNotification(notification, "notification$index")}
		assertThat(json.get("notificationHash")?.asInt(), equalTo(notifications.sortedBy { it.createdTime }.hashCode()))
	}

	@Test
	fun `page returns new posts`() {
		addNewPost("post1", "sone1", 1000)
		addNewPost("post2", "sone2", 2000, "sone1")
		assertThat(json.get("newPosts")!!.elements().asSequence().map { it.toMap() }.toList(), containsInAnyOrder(
				mapOf("id" to "post1", "sone" to "sone1", "time" to "1000", "recipient" to null),
				mapOf("id" to "post2", "sone" to "sone2", "time" to "2000", "recipient" to "sone1")
		))
	}

	@Test
	fun `page returns new replies`() {
		addNewReply("reply1", "sone1", "post1", "sone11")
		addNewReply("reply2", "sone2", "post2", "sone22")
		assertThat(json.get("newReplies")!!.elements().asSequence().map { it.toMap() }.toList(), containsInAnyOrder(
				mapOf<String, String?>("id" to "reply1", "sone" to "sone1", "post" to "post1", "postSone" to "sone11"),
				mapOf("id" to "reply2", "sone" to "sone2", "post" to "post2", "postSone" to "sone22")
		))
	}

	@Test
	fun `page returns information about loaded elements`() {
		addLinkedElement("KSK@test.png", loading = false, failed = false)
		addLinkedElement("KSK@test.html", loading = true, failed = false)
		addLinkedElement("KSK@test.jpeg", loading = false, failed = true)
		addRequestParameter("elements", jsonArray("KSK@test.png", "KSK@test.html", "KSK@test.jpeg").toString())
		assertThat(json.get("linkedElements")!!.elements().asSequence().map { it.toMap() }.toList(), containsInAnyOrder(
				mapOf<String, String?>("link" to "KSK@test.png", "loading" to "false", "failed" to "false"),
				mapOf("link" to "KSK@test.html", "loading" to "true", "failed" to "false"),
				mapOf("link" to "KSK@test.jpeg", "loading" to "false", "failed" to "true")
		))
	}

	private fun JsonNode.toMap() = fields().asSequence().map { it.key!! to if (it.value.isNull) null else it.value.asText()!! }.toMap()

}
