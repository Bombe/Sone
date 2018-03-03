package net.pterodactylus.sone.database.memory

import net.pterodactylus.sone.test.TestValue.from
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.config.Configuration
import net.pterodactylus.util.config.Value
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * Unit test for [ConfigurationLoader].
 */
class ConfigurationLoaderTest {

	private val configuration = mock<Configuration>()
	private val configurationLoader = ConfigurationLoader(configuration)

	private fun setupStringValue(attribute: String, value: String? = null): Value<String?> =
			from(value).apply {
				whenever(configuration.getStringValue(attribute)).thenReturn(this)
			}

	private fun setupLongValue(attribute: String, value: Long? = null): Value<Long?> =
			from(value).apply {
				whenever(configuration.getLongValue(attribute)).thenReturn(this)
			}

	@Test
	fun `loader can load known posts`() {
		setupStringValue("KnownPosts/0/ID", "Post2")
		setupStringValue("KnownPosts/1/ID", "Post1")
		setupStringValue("KnownPosts/2/ID")
		val knownPosts = configurationLoader.loadKnownPosts()
		assertThat(knownPosts, containsInAnyOrder("Post1", "Post2"))
	}

	@Test
	fun `loader can load known post replies`() {
		setupStringValue("KnownReplies/0/ID", "PostReply2")
		setupStringValue("KnownReplies/1/ID", "PostReply1")
		setupStringValue("KnownReplies/2/ID")
		val knownPosts = configurationLoader.loadKnownPostReplies()
		assertThat(knownPosts, containsInAnyOrder("PostReply1", "PostReply2"))
	}

	@Test
	fun `loader can load bookmarked posts`() {
		setupStringValue("Bookmarks/Post/0/ID", "Post2")
		setupStringValue("Bookmarks/Post/1/ID", "Post1")
		setupStringValue("Bookmarks/Post/2/ID")
		val knownPosts = configurationLoader.loadBookmarkedPosts()
		assertThat(knownPosts, containsInAnyOrder("Post1", "Post2"))
	}

	@Test
	fun `loader can save bookmarked posts`() {
		val post1 = setupStringValue("Bookmarks/Post/0/ID")
		val post2 = setupStringValue("Bookmarks/Post/1/ID")
		val post3 = setupStringValue("Bookmarks/Post/2/ID")
		val originalPosts = setOf("Post1", "Post2")
		configurationLoader.saveBookmarkedPosts(originalPosts)
		val extractedPosts = setOf(post1.value, post2.value)
		assertThat(extractedPosts, containsInAnyOrder("Post1", "Post2"))
		assertThat(post3.value, nullValue())
	}

	@Test
	fun `loader can load Sone following times`() {
		setupStringValue("SoneFollowingTimes/0/Sone", "Sone1")
		setupLongValue("SoneFollowingTimes/0/Time", 1000L)
		setupStringValue("SoneFollowingTimes/1/Sone", "Sone2")
		setupLongValue("SoneFollowingTimes/1/Time", 2000L)
		setupStringValue("SoneFollowingTimes/2/Sone")
		assertThat(configurationLoader.getSoneFollowingTime("Sone1"), equalTo(1000L))
		assertThat(configurationLoader.getSoneFollowingTime("Sone2"), equalTo(2000L))
		assertThat(configurationLoader.getSoneFollowingTime("Sone3"), nullValue())
	}

	@Test
	fun `loader can overwrite existing Sone following time`() {
		val sone1Id = setupStringValue("SoneFollowingTimes/0/Sone", "Sone1")
		val sone1Time = setupLongValue("SoneFollowingTimes/0/Time", 1000L)
		val sone2Id = setupStringValue("SoneFollowingTimes/1/Sone", "Sone2")
		val sone2Time = setupLongValue("SoneFollowingTimes/1/Time", 2000L)
		setupStringValue("SoneFollowingTimes/2/Sone")
		configurationLoader.setSoneFollowingTime("Sone1", 3000L)
		assertThat(listOf(sone1Id.value to sone1Time.value, sone2Id.value to sone2Time.value), containsInAnyOrder<Pair<String?, Long?>>(
				"Sone1" to 3000L,
				"Sone2" to 2000L
		))
	}

	@Test
	fun `loader can remove Sone following time`() {
		val sone1Id = setupStringValue("SoneFollowingTimes/0/Sone", "Sone1")
		val sone1Time = setupLongValue("SoneFollowingTimes/0/Time", 1000L)
		val sone2Id = setupStringValue("SoneFollowingTimes/1/Sone", "Sone2")
		val sone2Time = setupLongValue("SoneFollowingTimes/1/Time", 2000L)
		setupStringValue("SoneFollowingTimes/2/Sone")
		configurationLoader.removeSoneFollowingTime("Sone1")
		assertThat(sone1Id.value, equalTo("Sone2"))
		assertThat(sone1Time.value, equalTo(2000L))
		assertThat(sone2Id.value, nullValue())
	}

	@Test
	fun `sone with missing following time is not loaded`() {
		setupStringValue("SoneFollowingTimes/0/Sone", "Sone1")
		setupLongValue("SoneFollowingTimes/0/Time", 1000L)
		setupStringValue("SoneFollowingTimes/1/Sone", "Sone2")
		setupLongValue("SoneFollowingTimes/1/Time")
		setupStringValue("SoneFollowingTimes/2/Sone")
		assertThat(configurationLoader.getSoneFollowingTime("Sone1"), equalTo(1000L))
		assertThat(configurationLoader.getSoneFollowingTime("Sone2"), nullValue())
		assertThat(configurationLoader.getSoneFollowingTime("Sone3"), nullValue())
	}

}
