package net.pterodactylus.sone.notify

import com.google.inject.Guice
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.freenet.wot.Trust
import net.pterodactylus.sone.test.createLocalSone
import net.pterodactylus.sone.test.createPost
import net.pterodactylus.sone.test.createRemoteSone
import net.pterodactylus.sone.test.verifySingletonInstance
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [DefaultPostVisibilityFilter].
 */
class DefaultPostVisibilityFilterTest {

	private val postVisibilityFilter = DefaultPostVisibilityFilter()
	private val localSone = createLocalSone()
	private val remoteSone = createRemoteSone()

	@Test
	fun `post visibility filter is only created once`() {
		val injector = Guice.createInjector()
		injector.verifySingletonInstance<PostVisibilityFilter>()
	}

	@Test
	fun `post is not visible if it is not loaded`() {
		val post = createPost(loaded = false)
		assertThat(postVisibilityFilter.isPostVisible(null, post), equalTo(false))
	}

	@Test
	fun `loaded post is visible without sone and in the past`() {
		val post = createPost(sone = null)
		assertThat(postVisibilityFilter.isPostVisible(null, post), equalTo(true))
	}

	@Test
	fun `loaded post from the future is not visible`() {
		// the offset for the future must be large enough to survive loading freenet.crypt.Util.
		val post = createPost(time = System.currentTimeMillis() + 100000, sone = null)
		assertThat(postVisibilityFilter.isPostVisible(null, post), equalTo(false))
	}

	@Test
	fun `loaded post from explicitely not trusted sone is not visible`() {
		remoteSone.identity.setTrust(localSone.identity as OwnIdentity, Trust(-1, null, null))
		val post = createPost(sone = remoteSone)
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(false))
	}

	@Test
	fun `loaded post from implicitely untrusted sone is not visible`() {
		remoteSone.identity.setTrust(localSone.identity as OwnIdentity, Trust(null, -1, null))
		val post = createPost(sone = remoteSone)
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(false))
	}

	@Test
	fun `loaded post from implicitely untrusted but followed sone is visible`() {
		localSone.friends.add(remoteSone.id)
		remoteSone.identity.setTrust(localSone.identity as OwnIdentity, Trust(1, -1, null))
		val post = createPost(sone = remoteSone)
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	@Test
	fun `loaded post from implicitely trusted and followed sone is visible`() {
		localSone.friends.add(remoteSone.id)
		remoteSone.identity.setTrust(localSone.identity as OwnIdentity, Trust(null, 1, null))
		val post = createPost(sone = remoteSone)
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	@Test
	fun `loaded post from followed sone with unknown trust is visible`() {
		localSone.friends.add(remoteSone.id)
		remoteSone.identity.setTrust(localSone.identity as OwnIdentity, Trust(null, null, null))
		val post = createPost(sone = remoteSone)
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	@Test
	fun `loaded post from unfollowed remote sone that is not directed at local sone is not visible`() {
		val post = createPost(sone = remoteSone)
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(false))
	}

	@Test
	fun `loaded post from local sone is visible`() {
		val post = createPost(sone = localSone)
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	@Test
	fun `loaded post from followed remote sone that is not directed at local sone is visible`() {
		localSone.friends.add(remoteSone.id)
		val post = createPost(sone = remoteSone)
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	@Test
	fun `loaded post from remote sone that is directed at local sone is visible`() {
		val post = createPost(sone = remoteSone, recipient = localSone)
		assertThat(postVisibilityFilter.isPostVisible(localSone, post), equalTo(true))
	}

	@Test
	fun `predicate will correctly recognize visible post`() {
		val post = createPost(sone = localSone)
		assertThat(postVisibilityFilter.isVisible(null).invoke(post), equalTo(true))
	}

	@Test
	fun `predicate will correctly recognize not visible post`() {
		val post = createPost(loaded = false)
		assertThat(postVisibilityFilter.isVisible(null).invoke(post), equalTo(false))
	}

}
