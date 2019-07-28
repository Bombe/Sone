package net.pterodactylus.sone.core

import com.google.common.collect.*
import com.google.common.eventbus.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.freenet.wot.event.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.config.*
import org.hamcrest.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.verify
import org.mockito.hamcrest.MockitoHamcrest.*
import kotlin.test.*

/**
 * Unit test for [Core] and its subclasses.
 */
class CoreTest {

	@Test
	fun `mark post known marks post as known`() {
		val core = mock<Core>()
		val post = mock<Post>()
		core.MarkPostKnown(post).run()
		verify(core).markPostKnown(eq(post))
	}

	@Test
	fun `mark reply known marks reply as known`() {
		val core = mock<Core>()
		val postReply = mock<PostReply>()
		core.MarkReplyKnown(postReply).run()
		verify(core).markReplyKnown(eq(postReply))
	}

	@Test
	fun `removing an identity sends removal events for all sone elements`() {
		// given
		val configuration = mock<Configuration>()
		val freenetInterface = mock<FreenetInterface>()
		val identityManager = mock<IdentityManager>()
		val soneDownloader = mock<SoneDownloader>()
		val imageInserter = mock<ImageInserter>()
		val updateChecker = mock<UpdateChecker>()
		val webOfTrustUpdater = mock<WebOfTrustUpdater>()
		val eventBus = mock<EventBus>()
		val database = mock<Database>()
		val core = Core(configuration, freenetInterface, identityManager, soneDownloader, imageInserter, updateChecker, webOfTrustUpdater, eventBus, database)
		val ownIdentity = mock<OwnIdentity>()
		val identity = mock<Identity>()
		whenever(identity.id).thenReturn("sone-id")
		val sone = mock<Sone>()
		whenever(database.getSone("sone-id")).thenReturn(sone)
		val postReply1 = mock<PostReply>()
		val postReply2 = mock<PostReply>()
		whenever(sone.replies).thenReturn(ImmutableSet.of(postReply1, postReply2))
		val post1 = mock<Post>()
		val post2 = mock<Post>()
		whenever(sone.posts).thenReturn(ImmutableList.of(post1, post2))

		// when
		core.identityRemoved(IdentityRemovedEvent(ownIdentity, identity))

		// then
		val inOrder = inOrder(eventBus, database)
		inOrder.verify(eventBus).post(argThat(isPostReplyRemoved(postReply1)))
		inOrder.verify(eventBus).post(argThat(isPostReplyRemoved(postReply2)))
		inOrder.verify(eventBus).post(argThat(isPostRemoved(post1)))
		inOrder.verify(eventBus).post(argThat(isPostRemoved(post2)))
		inOrder.verify(eventBus).post(argThat(isSoneRemoved(sone)))
		inOrder.verify(database).removeSone(sone)
	}

	private fun isPostRemoved(post: Post): Matcher<Any> {
		return object : TypeSafeDiagnosingMatcher<Any>() {
			override fun matchesSafely(item: Any, mismatchDescription: Description): Boolean {
				if (item !is PostRemovedEvent) {
					mismatchDescription.appendText("is not PostRemovedEvent")
					return false
				}
				if (item.post !== post) {
					mismatchDescription.appendText("post is ").appendValue(item.post)
					return false
				}
				return true
			}

			override fun describeTo(description: Description) {
				description.appendText("is PostRemovedEvent and post is ").appendValue(post)
			}
		}
	}

	private fun isPostReplyRemoved(postReply: PostReply): Matcher<Any> {
		return object : TypeSafeDiagnosingMatcher<Any>() {
			override fun matchesSafely(item: Any, mismatchDescription: Description): Boolean {
				if (item !is PostReplyRemovedEvent) {
					mismatchDescription.appendText("is not PostReplyRemovedEvent")
					return false
				}
				if (item.postReply !== postReply) {
					mismatchDescription.appendText("post reply is ").appendValue(item.postReply)
					return false
				}
				return true
			}

			override fun describeTo(description: Description) {
				description.appendText("is PostReplyRemovedEvent and post is ").appendValue(postReply)
			}
		}
	}

	private fun isSoneRemoved(sone: Sone): Matcher<Any> {
		return object : TypeSafeDiagnosingMatcher<Any>() {
			override fun matchesSafely(item: Any, mismatchDescription: Description): Boolean {
				if (item !is SoneRemovedEvent) {
					mismatchDescription.appendText("is not SoneRemovedEvent")
					return false
				}
				if (item.sone !== sone) {
					mismatchDescription.appendText("sone is ").appendValue(item.sone)
					return false
				}
				return true
			}

			override fun describeTo(description: Description) {
				description.appendText("is SoneRemovedEvent and sone is ").appendValue(sone)
			}
		}
	}

	@Test
	fun `core starts with debug set to false`() {
		val core = createCore()
		assertThat(core.debug, equalTo(false))
	}

	@Test
	fun `debug flag can be set`() {
		val core = createCore()
		core.setDebug()
		assertThat(core.debug, equalTo(true))
	}

	@Test
	fun `setting debug flag posts event to event bus`() {
		val eventBus = mock<EventBus>()
		val core = createCore(eventBus)
		core.setDebug()
		verify(eventBus).post(argThat(instanceOf(DebugActivatedEvent::class.java)))
	}

	private fun createCore(eventBus: EventBus = mock()): Core {
		val configuration = mock<Configuration>()
		val freenetInterface = mock<FreenetInterface>()
		val identityManager = mock<IdentityManager>()
		val soneDownloader = mock<SoneDownloader>()
		val imageInserter = mock<ImageInserter>()
		val updateChecker = mock<UpdateChecker>()
		val webOfTrustUpdater = mock<WebOfTrustUpdater>()
		val database = mock<Database>()
		return Core(configuration, freenetInterface, identityManager, soneDownloader, imageInserter, updateChecker, webOfTrustUpdater, eventBus, database)
	}

}
