package net.pterodactylus.sone.notify

import com.google.common.base.Optional
import com.google.inject.Guice
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.wot.Identity
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.freenet.wot.Trust
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.verifySingletonInstance
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [PostVisibilityFilterTest].
 */
class PostVisibilityFilterTest {

	private val postVisibilityFilter = PostVisibilityFilter()
	private val localSone = mock<Sone>()
	private val localIdentity = mock<OwnIdentity>()
	private val post = mock<Post>()
	private val remoteSone = mock<Sone>()
	private val remoteIdentity = mock<Identity>()

	init {
		whenever(localSone.id).thenReturn(LOCAL_ID)
		whenever(localSone.isLocal).thenReturn(true)
		whenever(localSone.identity).thenReturn(localIdentity)
		whenever(localIdentity.id).thenReturn(LOCAL_ID)
		whenever(remoteSone.id).thenReturn(REMOTE_ID)
		whenever(remoteSone.identity).thenReturn(remoteIdentity)
		whenever(remoteIdentity.id).thenReturn(REMOTE_ID)
		whenever(post.recipientId).thenReturn(Optional.absent())
	}

	@Test
	fun `post visibility filter is only created once`() {
		val injector = Guice.createInjector()
		injector.verifySingletonInstance<PostVisibilityFilter>()
	}

	@Test
	fun `post is not visible if it is not loaded`() {
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(false))
	}

	@Test
	fun `loaded post is visible without sone`() {
		makePostLoaded(post)
		assertThat(postVisibilityFilter.isPostVisible(null, post), equalTo(true))
	}

	private fun makePostComeFromTheFuture() {
		whenever(post.time).thenReturn(System.currentTimeMillis() + 1000)
	}

	@Test
	fun `loaded post from the future is not visible`() {
		makePostLoaded(post)
		makePostComeFromTheFuture()
		assertThat(postVisibilityFilter.isPostVisible(null, post), equalTo(false))
	}

	private fun makePostFromRemoteSone() {
		whenever(post.sone).thenReturn(remoteSone)
	}

	private fun giveRemoteIdentityNegativeExplicitTrust() {
		whenever(remoteIdentity.getTrust(localIdentity)).thenReturn(Trust(-1, null, null))
	}

	@Test
	fun `loaded post from explicitely not trusted sone is not visible`() {
		makePostLoaded(post)
		makePostFromRemoteSone()
		giveRemoteIdentityNegativeExplicitTrust()
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(false))
	}

	private fun giveRemoteIdentityNegativeImplicitTrust() {
		whenever(remoteIdentity.getTrust(localIdentity)).thenReturn(Trust(null, -1, null))
	}

	@Test
	fun `loaded post from implicitely untrusted sone is not visible`() {
		makePostLoaded(post)
		makePostFromRemoteSone()
		giveRemoteIdentityNegativeImplicitTrust()
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(false))
	}

	private fun makeLocalSoneFollowRemoteSone() {
		whenever(localSone.hasFriend(REMOTE_ID)).thenReturn(true)
	}

	private fun giveRemoteIdentityPositiveExplicitTrustButNegativeImplicitTrust() {
		whenever(remoteIdentity.getTrust(localIdentity)).thenReturn(Trust(1, -1, null))
	}

	@Test
	fun `loaded post from explicitely trusted but implicitely untrusted sone is visible`() {
		makePostLoaded(post)
		makePostFromRemoteSone()
		makeLocalSoneFollowRemoteSone()
		giveRemoteIdentityPositiveExplicitTrustButNegativeImplicitTrust()
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	private fun giveTheRemoteIdentityPositiveImplicitTrust() {
		whenever(remoteIdentity.getTrust(localIdentity)).thenReturn(Trust(null, 1, null))
	}

	@Test
	fun `loaded post from implicitely trusted sone is visible`() {
		makePostLoaded(post)
		makePostFromRemoteSone()
		makeLocalSoneFollowRemoteSone()
		giveTheRemoteIdentityPositiveImplicitTrust()
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	private fun giveTheRemoteIdentityUnknownTrust() {
		whenever(remoteIdentity.getTrust(localIdentity)).thenReturn(Trust(null, null, null))
	}

	@Test
	fun `loaded post from sone with unknown trust is visible`() {
		makePostLoaded(post)
		makePostFromRemoteSone()
		makeLocalSoneFollowRemoteSone()
		giveTheRemoteIdentityUnknownTrust()
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	@Test
	fun `loaded post from unfollowed remote sone that is not directed at local sone is not visible`() {
		makePostLoaded(post)
		makePostFromRemoteSone()
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(false))
	}

	private fun makePostFromLocalSone() {
		makePostLoaded(post)
		whenever(post.sone).thenReturn(localSone)
	}

	@Test
	fun `loaded post from local sone is visible`() {
		makePostFromLocalSone()
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	@Test
	fun `loaded post from followed remote sone that is not directed at local sone is visible`() {
		makePostLoaded(post)
		makePostFromRemoteSone()
		makeLocalSoneFollowRemoteSone()
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	private fun makePostDirectedAtLocalId() {
		whenever(post.recipientId).thenReturn(Optional.of(LOCAL_ID))
	}

	@Test
	fun `loaded post from remote sone that is directed at local sone is visible`() {
		makePostLoaded(post)
		makePostFromRemoteSone()
		makePostDirectedAtLocalId()
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	@Test
	fun `predicate will correctly recognize visible post`() {
		makePostFromLocalSone()
		assertThat(postVisibilityFilter.isVisible(null).apply(post), equalTo(true))
	}

	@Test
	fun `predicate will correctly recognize not visible post`() {
		assertThat(postVisibilityFilter.isVisible(null).apply(post), equalTo(false))
	}

}

private const val LOCAL_ID = "local-id"
private const val REMOTE_ID = "remote-id"

private fun makePostLoaded(post: Post) {
	whenever(post.isLoaded).thenReturn(true)
}
