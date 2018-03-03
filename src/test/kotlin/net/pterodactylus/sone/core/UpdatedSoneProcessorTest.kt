package net.pterodactylus.sone.core

import com.google.common.eventbus.EventBus
import net.pterodactylus.sone.core.event.NewPostFoundEvent
import net.pterodactylus.sone.core.event.NewPostReplyFoundEvent
import net.pterodactylus.sone.core.event.PostRemovedEvent
import net.pterodactylus.sone.core.event.PostReplyRemovedEvent
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.database.Database
import net.pterodactylus.sone.test.argumentCaptor
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.isProvidedByMock
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

/**
 * Unit test for all [UpdatedSoneProcessor] implementations.
 */
class UpdatedSoneProcessorTest {

	private val database = mock<Database>()
	private val eventBus = mock<EventBus>()
	private val updatedSoneProcessor = DefaultUpdateSoneProcessor(database, eventBus)
	private val storedSone = mock<Sone>()
	private val newSone = mock<Sone>()
	private val posts = listOf(mock<Post>(), mock(), mock())
	private val postReplies = listOf(mock<PostReply>(), mock(), mock())

	private val events = argumentCaptor<Any>()

	@Before
	fun setupPostsAndReplies() {
		posts.forEachIndexed { index, post -> whenever(post.time).thenReturn((index + 1) * 1000L + 100) }
		postReplies.forEachIndexed { index, postReply -> whenever(postReply.time).thenReturn((index + 1) * 1000L + 200) }
	}

	@Before
	fun setupSones() {
		whenever(storedSone.time).thenReturn(1000L)
		whenever(storedSone.posts).thenReturn(posts.slice(0..1))
		whenever(storedSone.replies).thenReturn(postReplies.slice(0..1).toSet())
		whenever(newSone.id).thenReturn("sone")
		whenever(newSone.time).thenReturn(2000L)
		whenever(newSone.posts).thenReturn(posts.slice(1..2))
		whenever(newSone.replies).thenReturn(postReplies.slice(1..2).toSet())
	}

	@Before
	fun setupDatabase() {
		whenever(database.getSone("sone")).thenReturn(storedSone)
		whenever(database.getFollowingTime("sone")).thenReturn(500L)
	}

	@Test
	fun `updated Sone processor emits no event if no stored sone exists`() {
		whenever(database.getSone("sone")).thenReturn(null)
		updatedSoneProcessor.updateSone(newSone)
		verify(eventBus, never()).post(any())
	}

	@Test
	fun `updated Sone processor emits no event if new Sone is older than stored Sone`() {
		whenever(newSone.time).thenReturn(500L)
		updatedSoneProcessor.updateSone(newSone)
		verify(eventBus, never()).post(any())
	}

	@Test
	fun `updated Sone processor emits correct events when new Sone is newer`() {
		updatedSoneProcessor.updateSone(newSone)
		verify(eventBus, times(4)).post(events.capture())
		assertThat(events.allValues, containsInAnyOrder(
				NewPostFoundEvent(posts[2]),
				PostRemovedEvent(posts[0]),
				NewPostReplyFoundEvent(postReplies[2]),
				PostReplyRemovedEvent(postReplies[0])
		))
	}

	@Test
	fun `updated Sone processor does not mark new post as known if sone was not followed after post`() {
		updatedSoneProcessor.updateSone(newSone)
		verify(posts[2], never()).isKnown = true
	}

	@Test
	fun `updated Sone processor does not mark new posts as known if Sone is not followed`() {
		whenever(database.getFollowingTime("sone")).thenReturn(null)
		updatedSoneProcessor.updateSone(newSone)
		posts.forEach { verify(it, never()).isKnown = true }
	}

	@Test
	fun `updated Sone processor marks new post as known if sone was followed after post`() {
		whenever(database.getFollowingTime("sone")).thenReturn(3500L)
		updatedSoneProcessor.updateSone(newSone)
		verify(posts[2]).isKnown = true
	}

	@Test
	fun `updated Sone processor does not emit event for post if it is already known`() {
		whenever(posts[2].isKnown).thenReturn(true)
		updatedSoneProcessor.updateSone(newSone)
		verify(eventBus, atLeastOnce()).post(events.capture())
		assertThat(events.allValues, not(contains<Any>(NewPostFoundEvent(posts[2]))))
	}

	@Test
	fun `updated Sone processor does not mark new reply as known if sone was not followed after reply`() {
		updatedSoneProcessor.updateSone(newSone)
		verify(postReplies[2], never()).isKnown = true
	}

	@Test
	fun `updated Sone processor marks new reply as known if sone was followed after reply`() {
		whenever(database.getFollowingTime("sone")).thenReturn(3500L)
		updatedSoneProcessor.updateSone(newSone)
		verify(postReplies[2]).isKnown = true
	}

	@Test
	fun `updated Sone processor does not emit event for reply if it is already known`() {
		whenever(postReplies[2].isKnown).thenReturn(true)
		updatedSoneProcessor.updateSone(newSone)
		verify(eventBus, atLeastOnce()).post(events.capture())
		assertThat(events.allValues, not(contains<Any>(NewPostReplyFoundEvent(postReplies[2]))))
	}

	@Test
	fun `default updated Sone processor can be created by dependency injection`() {
		assertThat(baseInjector.createChildInjector(
				Database::class.isProvidedByMock()
		).getInstance<UpdatedSoneProcessor>(), notNullValue())
	}

}
