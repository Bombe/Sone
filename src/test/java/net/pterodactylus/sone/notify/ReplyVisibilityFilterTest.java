package net.pterodactylus.sone.notify;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;

import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

/**
 * Unit test for {@link ReplyVisibilityFilterTest}.
 */
public class ReplyVisibilityFilterTest {

	private static final String LOCAL_ID = "local-id";

	private final PostVisibilityFilter postVisibilityFilter = mock(PostVisibilityFilter.class);
	private final ReplyVisibilityFilter replyVisibilityFilter = new ReplyVisibilityFilter(postVisibilityFilter);

	private final Sone localSone = mock(Sone.class);
	private final OwnIdentity localIdentity = mock(OwnIdentity.class);
	private final Post post = mock(Post.class);
	private final PostReply postReply = mock(PostReply.class);

	public ReplyVisibilityFilterTest() {
		when(localSone.getId()).thenReturn(LOCAL_ID);
		when(localSone.isLocal()).thenReturn(true);
		when(localSone.getIdentity()).thenReturn(localIdentity);
		when(post.getRecipientId()).thenReturn(Optional.<String>absent());
	}

	@Test
	public void replyVisibilityFilterIsOnlyCreatedOnce() {
		Injector injector = Guice.createInjector();
		ReplyVisibilityFilter firstFilter = injector.getInstance(ReplyVisibilityFilter.class);
		ReplyVisibilityFilter secondFilter = injector.getInstance(ReplyVisibilityFilter.class);
		assertThat(firstFilter, sameInstance(secondFilter));
	}

	private void makePostPresent() {
		when(postReply.getPost()).thenReturn(Optional.of(post));
	}

	@Test
	public void replyIsNotVisibleIfPostIsNotVisible() {
		makePostPresent();
		assertThat(replyVisibilityFilter.isReplyVisible(localSone, postReply), is(false));
	}

	private void makePostAbsent() {
		when(postReply.getPost()).thenReturn(Optional.<Post>absent());
	}

	@Test
	public void replyIsNotVisibleIfPostIsNotPresent() {
		makePostAbsent();
		assertThat(replyVisibilityFilter.isReplyVisible(localSone, postReply), is(false));
	}

	private void makePostPresentAndVisible() {
		makePostPresent();
		when(postVisibilityFilter.isPostVisible(localSone, post)).thenReturn(true);
	}

	private void makeReplyComeFromFuture() {
		when(postReply.getTime()).thenReturn(System.currentTimeMillis() + 1000);
	}

	@Test
	public void replyIsNotVisibleIfItIsFromTheFuture() {
		makePostPresentAndVisible();
		makeReplyComeFromFuture();
		assertThat(replyVisibilityFilter.isReplyVisible(localSone, postReply), is(false));
	}

	@Test
	public void replyIsVisibleIfItIsNotFromTheFuture() {
		makePostPresentAndVisible();
		assertThat(replyVisibilityFilter.isReplyVisible(localSone, postReply), is(true));
	}

	@Test
	public void predicateCorrectlyRecognizesVisibleReply() {
		makePostPresentAndVisible();
		assertThat(replyVisibilityFilter.isVisible(localSone).apply(postReply), is(true));
	}

	@Test
	public void predicateCorrectlyRecognizesNotVisibleReply() {
		makePostPresentAndVisible();
		makeReplyComeFromFuture();
		assertThat(replyVisibilityFilter.isVisible(localSone).apply(postReply), is(false));
	}

}
