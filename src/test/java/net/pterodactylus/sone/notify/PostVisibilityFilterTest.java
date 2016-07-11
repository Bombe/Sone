package net.pterodactylus.sone.notify;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.freenet.wot.Trust;

import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

/**
 * Unit test for {@link PostVisibilityFilterTest}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostVisibilityFilterTest {

	private static final String LOCAL_ID = "local-id";
	private static final String REMOTE_ID = "remote-id";

	private final PostVisibilityFilter postVisibilityFilter = new PostVisibilityFilter();

	private final Sone localSone = mock(Sone.class);
	private final OwnIdentity localIdentity = mock(OwnIdentity.class);
	private final Post post = mock(Post.class);
	private final Sone remoteSone = mock(Sone.class);
	private final Identity remoteIdentity = mock(Identity.class);

	public PostVisibilityFilterTest() {
		when(localSone.getId()).thenReturn(LOCAL_ID);
		when(localSone.isLocal()).thenReturn(true);
		when(localSone.getIdentity()).thenReturn(localIdentity);
		when(localIdentity.getId()).thenReturn(LOCAL_ID);
		when(remoteSone.getId()).thenReturn(REMOTE_ID);
		when(remoteSone.getIdentity()).thenReturn(remoteIdentity);
		when(remoteIdentity.getId()).thenReturn(REMOTE_ID);
		when(post.getRecipientId()).thenReturn(Optional.<String>absent());
	}

	@Test
	public void postVisibilityFilterIsOnlyCreatedOnce() {
		Injector injector = Guice.createInjector();
		PostVisibilityFilter firstFilter = injector.getInstance(PostVisibilityFilter.class);
		PostVisibilityFilter secondFilter = injector.getInstance(PostVisibilityFilter.class);
		assertThat(firstFilter, sameInstance(secondFilter));
	}

	@Test
	public void postIsNotVisibleIfItIsNotLoaded() {
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), is(false));
	}

	private static void makePostLoaded(Post post) {
		when(post.isLoaded()).thenReturn(true);
	}

	@Test
	public void loadedPostIsVisibleWithoutSone() {
		makePostLoaded(post);
		assertThat(postVisibilityFilter.isPostVisible(null, post), is(true));
	}

	private void makePostComeFromTheFuture() {
		when(post.getTime()).thenReturn(System.currentTimeMillis() + 1000);
	}

	@Test
	public void loadedPostFromTheFutureIsNotVisible() {
		makePostLoaded(post);
		makePostComeFromTheFuture();
		assertThat(postVisibilityFilter.isPostVisible(null, post), is(false));
	}

	private void makePostFromRemoteSone() {
		when(post.getSone()).thenReturn(remoteSone);
	}

	private void giveRemoteIdentityNegativeExplicitTrust() {
		when(remoteIdentity.getTrust(localIdentity)).thenReturn(new Trust(-1, null, null));
	}

	@Test
	public void loadedPostFromExplicitelyNotTrustedSoneIsNotVisible() {
		makePostLoaded(post);
		makePostFromRemoteSone();
		giveRemoteIdentityNegativeExplicitTrust();
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), is(false));
	}

	private void giveRemoteIdentityNegativeImplicitTrust() {
		when(remoteIdentity.getTrust(localIdentity)).thenReturn(new Trust(null, -1, null));
	}

	@Test
	public void loadedPostFromImplicitelyUntrustedSoneIsNotVisible() {
		makePostLoaded(post);
		makePostFromRemoteSone();
		giveRemoteIdentityNegativeImplicitTrust();
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), is(false));
	}

	private void makeLocalSoneFollowRemoteSone() {
		when(localSone.hasFriend(REMOTE_ID)).thenReturn(true);
	}

	private void giveRemoteIdentityPositiveExplicitTrustButNegativeImplicitTrust() {
		when(remoteIdentity.getTrust(localIdentity)).thenReturn(new Trust(1, -1, null));
	}

	@Test
	public void loadedPostFromExplicitelyTrustedButImplicitelyUntrustedSoneIsVisible() {
		makePostLoaded(post);
		makePostFromRemoteSone();
		makeLocalSoneFollowRemoteSone();
		giveRemoteIdentityPositiveExplicitTrustButNegativeImplicitTrust();
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), is(true));
	}

	private void giveTheRemoteIdentityPositiveImplicitTrust() {
		when(remoteIdentity.getTrust(localIdentity)).thenReturn(new Trust(null, 1, null));
	}

	@Test
	public void loadedPostFromImplicitelyTrustedSoneIsVisible() {
		makePostLoaded(post);
		makePostFromRemoteSone();
		makeLocalSoneFollowRemoteSone();
		giveTheRemoteIdentityPositiveImplicitTrust();
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), is(true));
	}

	private void giveTheRemoteIdentityUnknownTrust() {
		when(remoteIdentity.getTrust(localIdentity)).thenReturn(new Trust(null, null, null));
	}

	@Test
	public void loadedPostFromSoneWithUnknownTrustIsVisible() {
		makePostLoaded(post);
		makePostFromRemoteSone();
		makeLocalSoneFollowRemoteSone();
		giveTheRemoteIdentityUnknownTrust();
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), is(true));
	}

	@Test
	public void loadedPostFromUnfollowedRemoteSoneThatIsNotDirectedAtLocalSoneIsNotVisible() {
		makePostLoaded(post);
		makePostFromRemoteSone();
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), is(false));
	}

	private void makePostFromLocalSone() {
		makePostLoaded(post);
		when(post.getSone()).thenReturn(localSone);
	}

	@Test
	public void loadedPostFromLocalSoneIsVisible() {
		makePostFromLocalSone();
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), is(true));
	}

	@Test
	public void loadedPostFromFollowedRemoteSoneThatIsNotDirectedAtLocalSoneIsVisible() {
		makePostLoaded(post);
		makePostFromRemoteSone();
		makeLocalSoneFollowRemoteSone();
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), is(true));
	}

	private void makePostDirectedAtLocalId() {
		when(post.getRecipientId()).thenReturn(Optional.of(LOCAL_ID));
	}

	@Test
	public void loadedPostFromRemoteSoneThatIsDirectedAtLocalSoneIsVisible() {
		makePostLoaded(post);
		makePostFromRemoteSone();
		makePostDirectedAtLocalId();
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), is(true));
	}

	@Test
	public void predicateWillCorrectlyRecognizeVisiblePost() {
		makePostFromLocalSone();
		assertThat(postVisibilityFilter.isVisible(null).apply(post), is(true));
	}

	@Test
	public void predicateWillCorrectlyRecognizeNotVisiblePost() {
		assertThat(postVisibilityFilter.isVisible(null).apply(post), is(false));
	}

}
