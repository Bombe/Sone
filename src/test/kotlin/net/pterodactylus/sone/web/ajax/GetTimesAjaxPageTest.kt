package net.pterodactylus.sone.web.ajax

import com.fasterxml.jackson.databind.JsonNode
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.freenet.L10nFilter
import net.pterodactylus.sone.freenet.L10nText
import net.pterodactylus.sone.test.OverrideLocale
import net.pterodactylus.sone.test.get
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.isProvidedByMock
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.text.TimeText
import net.pterodactylus.sone.text.TimeTextConverter
import net.pterodactylus.sone.utils.jsonObject
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import java.util.Locale.ENGLISH
import java.util.TimeZone
import java.util.TimeZone.getTimeZone

/**
 * Unit test for [GetTimesAjaxPage].
 */
class GetTimesAjaxPageTest : JsonPageTest("getTimes.ajax", needsFormPassword = false, requiresLogin = false) {

	@get:Rule
	val overrideLocale = OverrideLocale(ENGLISH)

	private val timeTextConverter = mock<TimeTextConverter>()
	private val l10nFilter = mock<L10nFilter>()
	override val page: JsonPage by lazy { GetTimesAjaxPage(webInterface, timeTextConverter, l10nFilter, getTimeZone("UTC")) }
	private val testPosts = listOf(createPost(1), createPost(2))
	private val testReplies = listOf(createReply(1), createReply(2))

	private fun createPost(index: Int): Post {
		return mock<Post>().apply {
			whenever(id).thenReturn("post$index")
			whenever(time).thenReturn(index.toLong() * 1000)
		}
	}

	private fun createReply(index: Int): PostReply {
		return mock<PostReply>().apply {
			whenever(id).thenReturn("reply$index")
			whenever(time).thenReturn(index.toLong() * 1000)
		}
	}

	@Before
	fun setupMocks() {
		whenever(timeTextConverter.getTimeText(anyLong())).then { TimeText(L10nText(it.get<Long>(0).toString()), it.get<Long>(0) * 2) }
		whenever(l10nFilter.format(any(), any(), any())).then { it.get<L10nText>(1).text }
	}

	@Test
	fun `request without any parameters responds with empty post and reply times`() {
		assertThatJsonIsSuccessful()
		assertThat(json["postTimes"]?.toList(), emptyIterable())
		assertThat(json["replyTimes"]?.toList(), emptyIterable())
	}

	@Test
	fun `request with single post parameter responds with post times and empty reply times`() {
		addPost(testPosts[0])
		addRequestParameter("posts", "post1")
		assertThatJsonIsSuccessful()
		assertThat(json["postTimes"]!!.fields().asSequence().map { it.key to it.value }.toList(), containsInAnyOrder<Pair<String, JsonNode>>(
				"post1" to jsonObject("timeText" to "1000", "refreshTime" to 2L, "tooltip" to "Jan 1, 1970, 00:00:01")
		))
		assertThat(json["replyTimes"]?.toList(), emptyIterable())
	}

	@Test
	fun `request with single reply parameter responds with reply times and empty post times`() {
		addReply(testReplies[0])
		addRequestParameter("replies", "reply1")
		assertThatJsonIsSuccessful()
		assertThat(json["postTimes"]?.toList(), emptyIterable())
		assertThat(json["replyTimes"]!!.fields().asSequence().map { it.key to it.value }.toList(), containsInAnyOrder<Pair<String, JsonNode>>(
				"reply1" to jsonObject("timeText" to "1000", "refreshTime" to 2L, "tooltip" to "Jan 1, 1970, 00:00:01")
		))
	}

	@Test
	fun `request with multiple post parameter responds with post times and empty reply times`() {
		addPost(testPosts[0])
		addPost(testPosts[1])
		addRequestParameter("posts", "post1,post2,post3")
		assertThatJsonIsSuccessful()
		assertThat(json["postTimes"]!!.fields().asSequence().map { it.key to it.value }.toList(), containsInAnyOrder<Pair<String, JsonNode>>(
				"post1" to jsonObject("timeText" to "1000", "refreshTime" to 2L, "tooltip" to "Jan 1, 1970, 00:00:01"),
				"post2" to jsonObject("timeText" to "2000", "refreshTime" to 4L, "tooltip" to "Jan 1, 1970, 00:00:02")
		))
		assertThat(json["replyTimes"]?.toList(), emptyIterable())
	}

	@Test
	fun `request with multiple reply parameters responds with reply times and empty post times`() {
		addReply(testReplies[0])
		addReply(testReplies[1])
		addRequestParameter("replies", "reply1,reply2,reply3")
		assertThatJsonIsSuccessful()
		assertThat(json["postTimes"]?.toList(), emptyIterable())
		assertThat(json["replyTimes"]!!.fields().asSequence().map { it.key to it.value }.toList(), containsInAnyOrder<Pair<String, JsonNode>>(
				"reply1" to jsonObject("timeText" to "1000", "refreshTime" to 2L, "tooltip" to "Jan 1, 1970, 00:00:01"),
				"reply2" to jsonObject("timeText" to "2000", "refreshTime" to 4L, "tooltip" to "Jan 1, 1970, 00:00:02")
		))
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.createChildInjector(
				TimeTextConverter::class.isProvidedByMock(),
				L10nFilter::class.isProvidedByMock(),
				TimeZone::class.isProvidedByMock()
		).getInstance<GetTimesAjaxPage>(), notNullValue())
	}

}
