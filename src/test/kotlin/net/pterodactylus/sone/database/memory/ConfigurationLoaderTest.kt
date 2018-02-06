package net.pterodactylus.sone.database.memory

import net.pterodactylus.sone.test.TestValue.from
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.config.Configuration
import net.pterodactylus.util.config.Value
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * Unit test for [ConfigurationLoader].
 *
 * @author [David ‘Bombe’ Roden](mailto:bombe@pterodactylus.net)
 */
class ConfigurationLoaderTest {

	private val configuration = mock<Configuration>()
	private val configurationLoader = ConfigurationLoader(configuration)

	private fun setupStringValue(attribute: String, value: String? = null): Value<String?> =
			from(value).apply {
				whenever(configuration.getStringValue(attribute)).thenReturn(this)
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

}
