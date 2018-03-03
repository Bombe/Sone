package net.pterodactylus.sone.core;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import net.pterodactylus.sone.core.Core.MarkPostKnown;
import net.pterodactylus.sone.core.Core.MarkReplyKnown;
import net.pterodactylus.sone.core.event.PostRemovedEvent;
import net.pterodactylus.sone.core.event.PostReplyRemovedEvent;
import net.pterodactylus.sone.core.event.SoneRemovedEvent;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.Database;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.IdentityManager;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.freenet.wot.event.IdentityRemovedEvent;
import net.pterodactylus.util.config.Configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.mockito.InOrder;

/**
 * Unit test for {@link Core} and its subclasses.
 */
public class CoreTest {

	@Test
	public void markPostKnownMarksPostAsKnown() {
		Core core = mock(Core.class);
		Post post = mock(Post.class);
		MarkPostKnown markPostKnown = core.new MarkPostKnown(post);
		markPostKnown.run();
		verify(core).markPostKnown(eq(post));
	}

	@Test
	public void markReplyKnownMarksReplyAsKnown() {
		Core core = mock(Core.class);
		PostReply postReply = mock(PostReply.class);
		MarkReplyKnown markReplyKnown = core.new MarkReplyKnown(postReply);
		markReplyKnown.run();
		verify(core).markReplyKnown(eq(postReply));
	}

	@Test
	public void removingAnIdentitySendsRemovalEventsForAllSoneElements() {
		// given
		Configuration configuration = mock(Configuration.class);
		FreenetInterface freenetInterface = mock(FreenetInterface.class);
		IdentityManager identityManager = mock(IdentityManager.class);
		SoneDownloader soneDownloader = mock(SoneDownloader.class);
		ImageInserter imageInserter = mock(ImageInserter.class);
		UpdateChecker updateChecker = mock(UpdateChecker.class);
		WebOfTrustUpdater webOfTrustUpdater = mock(WebOfTrustUpdater.class);
		EventBus eventBus = mock(EventBus.class);
		Database database = mock(Database.class);
		Core core = new Core(configuration, freenetInterface, identityManager, soneDownloader, imageInserter, updateChecker, webOfTrustUpdater, eventBus, database);
		OwnIdentity ownIdentity = mock(OwnIdentity.class);
		Identity identity = mock(Identity.class);
		when(identity.getId()).thenReturn("sone-id");
		Sone sone = mock(Sone.class);
		when(database.getSone("sone-id")).thenReturn(sone);
		PostReply postReply1 = mock(PostReply.class);
		PostReply postReply2 = mock(PostReply.class);
		when(sone.getReplies()).thenReturn(ImmutableSet.of(postReply1, postReply2));
		Post post1 = mock(Post.class);
		Post post2 = mock(Post.class);
		when(sone.getPosts()).thenReturn(ImmutableList.of(post1, post2));

		// when
		core.identityRemoved(new IdentityRemovedEvent(ownIdentity, identity));

		// then
		InOrder inOrder = inOrder(eventBus, database);
		inOrder.verify(eventBus).post(argThat(isPostReplyRemoved(postReply1)));
		inOrder.verify(eventBus).post(argThat(isPostReplyRemoved(postReply2)));
		inOrder.verify(eventBus).post(argThat(isPostRemoved(post1)));
		inOrder.verify(eventBus).post(argThat(isPostRemoved(post2)));
		inOrder.verify(eventBus).post(argThat(isSoneRemoved(sone)));
		inOrder.verify(database).removeSone(sone);
	}

	private Matcher<Object> isPostRemoved(final Post post) {
		return new TypeSafeDiagnosingMatcher<Object>() {
			@Override
			protected boolean matchesSafely(Object item, Description mismatchDescription) {
				if (!(item instanceof PostRemovedEvent)) {
					mismatchDescription.appendText("is not PostRemovedEvent");
					return false;
				}
				if (((PostRemovedEvent) item).post() != post) {
					mismatchDescription.appendText("post is ").appendValue(((PostRemovedEvent) item).post());
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is PostRemovedEvent and post is ").appendValue(post);
			}
		};
	}

	private Matcher<Object> isPostReplyRemoved(final PostReply postReply) {
		return new TypeSafeDiagnosingMatcher<Object>() {
			@Override
			protected boolean matchesSafely(Object item, Description mismatchDescription) {
				if (!(item instanceof PostReplyRemovedEvent)) {
					mismatchDescription.appendText("is not PostReplyRemovedEvent");
					return false;
				}
				if (((PostReplyRemovedEvent) item).postReply() != postReply) {
					mismatchDescription.appendText("post reply is ").appendValue(((PostReplyRemovedEvent) item).postReply());
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is PostReplyRemovedEvent and post is ").appendValue(postReply);
			}
		};
	}

	private Matcher<Object> isSoneRemoved(final Sone sone) {
		return new TypeSafeDiagnosingMatcher<Object>() {
			@Override
			protected boolean matchesSafely(Object item, Description mismatchDescription) {
				if (!(item instanceof SoneRemovedEvent)) {
					mismatchDescription.appendText("is not SoneRemovedEvent");
					return false;
				}
				if (((SoneRemovedEvent) item).sone() != sone) {
					mismatchDescription.appendText("sone is ").appendValue(((SoneRemovedEvent) item).sone());
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is SoneRemovedEvent and sone is ").appendValue(sone);
			}
		};
	}

}
