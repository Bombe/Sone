package net.pterodactylus.sone.data

import net.pterodactylus.sone.test.createPost
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.lessThan
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

	@Test
	fun `newestFirst comparator returns less-than 0 if first is newer than second`() {
		val newerPost = createPost(time = 2000)
		val olderPost = createPost(time = 1000)
		assertThat(newestPostFirst.compare(newerPost, olderPost), lessThan(0))
	}

	@Test
	fun `newestFirst comparator returns greater-than 0 if first is older than second`() {
		val newerPost = createPost(time = 2000)
		val olderPost = createPost(time = 1000)
		assertThat(newestPostFirst.compare(olderPost, newerPost), greaterThan(0))
	}

	@Test
	fun `newestFirst comparator returns 0 if first and second are the same age`() {
		val post1 = createPost(time = 1000)
		val post2 = createPost(time = 1000)
		assertThat(newestPostFirst.compare(post2, post1), equalTo(0))
	}

}
