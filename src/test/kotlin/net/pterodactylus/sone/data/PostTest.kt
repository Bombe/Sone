package net.pterodactylus.sone.data

import net.pterodactylus.sone.test.createPost
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import java.util.concurrent.TimeUnit.DAYS
import kotlin.test.Test

/**
 * Unit test for the utilities in `Post.kt`.
 */
class PostTest {

	@Test
	fun `noFuturePost filter recognizes post from future`() {
		val post = createPost(time = System.currentTimeMillis() + DAYS.toMillis(1))
		assertThat(noFuturePost(post), equalTo(false))
	}

	@Test
	fun `noFuturePost filter recognizes post not from future`() {
		val post = createPost(time = System.currentTimeMillis())
		assertThat(noFuturePost(post), equalTo(true))
	}

}
