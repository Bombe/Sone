package net.pterodactylus.sone.web.pages

import com.google.common.base.*
import com.google.common.base.Optional.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * Unit test for [SearchPage].
 */
class SearchPageTest : WebPageTest({ webInterface, loaders, templateRenderer -> SearchPage(webInterface, loaders, templateRenderer, ticker) }) {

	companion object {
		val ticker = mock<Ticker>()
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("search.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.Search.Title", "search page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("search page title"))
	}

	@Test
	fun `empty query redirects to index page`() {
		verifyRedirect("index.html")
	}

	@Test
	fun `empty search phrases redirect to index page`() {
		addHttpRequestParameter("query", "\"\"")
		verifyRedirect("index.html")
	}

	@Test
	fun `invalid search phrases redirect to index page`() {
		addHttpRequestParameter("query", "\"")
		verifyRedirect("index.html")
	}

	@Test
	fun `searching for sone link redirects to view sone page`() {
		addSone("Sone-ID", mock())
		addHttpRequestParameter("query", "sone://Sone-ID")
		verifyRedirect("viewSone.html?sone=Sone-ID")
	}

	@Test
	fun `searching for sone link without prefix redirects to view sone page`() {
		addSone("sone-id", mock())
		addHttpRequestParameter("query", "sone-id")
		verifyRedirect("viewSone.html?sone=sone-id")
	}

	@Test
	fun `searching for a post link redirects to post page`() {
		addPost("Post-id", mock())
		addHttpRequestParameter("query", "post://Post-id")
		verifyRedirect("viewPost.html?post=Post-id")
	}

	@Test
	fun `searching for a post ID without prefix redirects to post page`() {
		addPost("post-id", mock())
		addHttpRequestParameter("query", "post-id")
		verifyRedirect("viewPost.html?post=post-id")
	}

	@Test
	fun `searching for a reply link redirects to the post page`() {
		val postReply = mock<PostReply>().apply { whenever(postId).thenReturn("post-id") }
		addPostReply("Reply-id", postReply)
		addHttpRequestParameter("query", "reply://Reply-id")
		verifyRedirect("viewPost.html?post=post-id")
	}

	@Test
	fun `searching for a reply ID redirects to the post page`() {
		val postReply = mock<PostReply>().apply { whenever(postId).thenReturn("post-id") }
		addPostReply("reply-id", postReply)
		addHttpRequestParameter("query", "reply-id")
		verifyRedirect("viewPost.html?post=post-id")
	}

	@Test
	fun `searching for an album link redirects to the image browser`() {
		addAlbum("album-id", mock())
		addHttpRequestParameter("query", "album://album-id")
		verifyRedirect("imageBrowser.html?album=album-id")
	}

	@Test
	fun `searching for an album ID redirects to the image browser`() {
		addAlbum("album-id", mock())
		addHttpRequestParameter("query", "album-id")
		verifyRedirect("imageBrowser.html?album=album-id")
	}

	@Test
	fun `searching for an image link redirects to the image browser`() {
		addImage("image-id", mock())
		addHttpRequestParameter("query", "image://image-id")
		verifyRedirect("imageBrowser.html?image=image-id")
	}

	@Test
	fun `searching for an image ID redirects to the image browser`() {
		addImage("image-id", mock())
		addHttpRequestParameter("query", "image-id")
		verifyRedirect("imageBrowser.html?image=image-id")
	}

	private fun createReply(text: String, postId: String? = null, sone: Sone? = null) = mock<PostReply>().apply {
		whenever(this.text).thenReturn(text)
		postId?.run { whenever(this@apply.postId).thenReturn(postId) }
		sone?.run { whenever(this@apply.sone).thenReturn(sone) }
	}

	private fun createPost(id: String, text: String) = mock<Post>().apply {
		whenever(this.id).thenReturn(id)
		whenever(recipient).thenReturn(absent())
		whenever(this.text).thenReturn(text)
	}

	private fun createSoneWithPost(post: Post, sone: Sone? = null) = sone?.apply {
		whenever(posts).thenReturn(listOf(post))
	} ?: mock<Sone>().apply {
		whenever(posts).thenReturn(listOf(post))
		whenever(profile).thenReturn(Profile(this))
	}

	@Test
	fun `searching for a single word finds the post`() {
		val postWithMatch = createPost("post-with-match", "the word here")
		val postWithoutMatch = createPost("post-without-match", "no match here")
		val soneWithMatch = createSoneWithPost(postWithMatch)
		val soneWithoutMatch = createSoneWithPost(postWithoutMatch)
		addSone("sone-with-match", soneWithMatch)
		addSone("sone-without-match", soneWithoutMatch)
		addHttpRequestParameter("query", "word")
		verifyNoRedirect {
			assertThat(this["postHits"], contains<Post>(postWithMatch))
		}
	}

	@Test
	fun `searching for a single word locates word in reply`() {
		val postWithMatch = createPost("post-with-match", "no match here")
		val postWithoutMatch = createPost("post-without-match", "no match here")
		val soneWithMatch = createSoneWithPost(postWithMatch)
		val soneWithoutMatch = createSoneWithPost(postWithoutMatch)
		val replyWithMatch = createReply("the word here", "post-with-match", soneWithMatch)
		val replyWithoutMatch = createReply("no match here", "post-without-match", soneWithoutMatch)
		addPostReply("reply-with-match", replyWithMatch)
		addPostReply("reply-without-match", replyWithoutMatch)
		addSone("sone-with-match", soneWithMatch)
		addSone("sone-without-match", soneWithoutMatch)
		addHttpRequestParameter("query", "word")
		verifyNoRedirect {
			assertThat(this["postHits"], contains<Post>(postWithMatch))
		}
	}

	private fun createSoneWithPost(idPostfix: String, text: String, recipient: Sone? = null, sender: Sone? = null) =
			createPost("post-$idPostfix", text, recipient).apply {
				addSone("sone-$idPostfix", createSoneWithPost(this, sender))
			}

	@Test
	fun `earlier matches score higher than later matches`() {
		val postWithEarlyMatch = createSoneWithPost("with-early-match", "optional match")
		val postWithLaterMatch = createSoneWithPost("with-later-match", "match that is optional")
		addHttpRequestParameter("query", "optional ")
		verifyNoRedirect {
			assertThat(this["postHits"], contains<Post>(postWithEarlyMatch, postWithLaterMatch))
		}
	}

	@Test
	fun `searching for required word does not return posts without that word`() {
		val postWithRequiredMatch = createSoneWithPost("with-required-match", "required match")
		createPost("without-required-match", "not a match")
		addHttpRequestParameter("query", "+required ")
		verifyNoRedirect {
			assertThat(this["postHits"], contains<Post>(postWithRequiredMatch))
		}
	}

	@Test
	fun `searching for forbidden word does not return posts with that word`() {
		createSoneWithPost("with-forbidden-match", "forbidden match")
		val postWithoutForbiddenMatch = createSoneWithPost("without-forbidden-match", "not a match")
		addHttpRequestParameter("query", "match -forbidden")
		verifyNoRedirect {
			assertThat(this["postHits"], contains<Post>(postWithoutForbiddenMatch))
		}
	}

	@Test
	fun `searching for a plus sign searches for optional plus sign`() {
		val postWithMatch = createSoneWithPost("with-match", "with + match")
		createSoneWithPost("without-match", "without match")
		addHttpRequestParameter("query", "+")
		verifyNoRedirect {
			assertThat(this["postHits"], contains<Post>(postWithMatch))
		}
	}

	@Test
	fun `searching for a minus sign searches for optional minus sign`() {
		val postWithMatch = createSoneWithPost("with-match", "with - match")
		createSoneWithPost("without-match", "without match")
		addHttpRequestParameter("query", "-")
		verifyNoRedirect {
			assertThat(this["postHits"], contains<Post>(postWithMatch))
		}
	}

	private fun createPost(id: String, text: String, recipient: Sone?) = mock<Post>().apply {
		whenever(this.id).thenReturn(id)
		val recipientId = recipient?.id
		whenever(this.recipientId).thenReturn(recipientId.asOptional())
		whenever(this.recipient).thenReturn(recipient.asOptional())
		whenever(this.text).thenReturn(text)
	}

	private fun createSone(id: String, firstName: String? = null, middleName: String? = null, lastName: String? = null) = mock<Sone>().apply {
		whenever(this.id).thenReturn(id)
		whenever(this.name).thenReturn(id)
		whenever(this.profile).thenReturn(Profile(this).apply {
			this.firstName = firstName
			this.middleName = middleName
			this.lastName = lastName
		})
	}

	@Test
	fun `searching for a recipient finds the correct post`() {
		val recipient = createSone("recipient", "reci", "pi", "ent")
		val postWithMatch = createSoneWithPost("with-match", "test", recipient)
		createSoneWithPost("without-match", "no match")
		addHttpRequestParameter("query", "recipient")
		verifyNoRedirect {
			assertThat(this["postHits"], contains<Post>(postWithMatch))
		}
	}

	@Test
	fun `searching for a field value finds the correct sone`() {
		val soneWithProfileField = createSone("sone", "s", "o", "ne")
		soneWithProfileField.profile.addField("field").value = "value"
		createSoneWithPost("with-match", "test", sender = soneWithProfileField)
		createSoneWithPost("without-match", "no match")
		addHttpRequestParameter("query", "value")
		verifyNoRedirect {
			assertThat(this["soneHits"], contains(soneWithProfileField))
		}
	}

	@Test
	fun `sone hits are paginated correctly`() {
		core.preferences.newPostsPerPage = 2
		val sones = listOf(createSone("1Sone"), createSone("Other1"), createSone("22Sone"), createSone("333Sone"), createSone("Other2"))
				.onEach { addSone(it.id, it) }
		addHttpRequestParameter("query", "sone")
		verifyNoRedirect {
			assertThat(this["sonePagination"], isOnPage(0).hasPages(2))
			assertThat(this["soneHits"], contains(sones[0], sones[2]))
		}
	}

	@Test
	fun `sone hits page 2 is shown correctly`() {
		core.preferences.newPostsPerPage = 2
		val sones = listOf(createSone("1Sone"), createSone("Other1"), createSone("22Sone"), createSone("333Sone"), createSone("Other2"))
				.onEach { addSone(it.id, it) }
		addHttpRequestParameter("query", "sone")
		addHttpRequestParameter("sonePage", "1")
		verifyNoRedirect {
			assertThat(this["sonePagination"], isOnPage(1).hasPages(2))
			assertThat(this["soneHits"], contains(sones[3]))
		}
	}

	@Test
	fun `post hits are paginated correctly`() {
		core.preferences.newPostsPerPage = 2
		val sones = listOf(createSoneWithPost("match1", "1Sone"), createSoneWithPost("no-match1", "Other1"), createSoneWithPost("match2", "22Sone"), createSoneWithPost("match3", "333Sone"), createSoneWithPost("no-match2", "Other2"))
		addHttpRequestParameter("query", "sone")
		verifyNoRedirect {
			assertThat(this["postPagination"], isOnPage(0).hasPages(2))
			assertThat(this["postHits"], contains(sones[0], sones[2]))
		}
	}

	@Test
	fun `post hits page 2 is shown correctly`() {
		core.preferences.newPostsPerPage = 2
		val sones = listOf(createSoneWithPost("match1", "1Sone"), createSoneWithPost("no-match1", "Other1"), createSoneWithPost("match2", "22Sone"), createSoneWithPost("match3", "333Sone"), createSoneWithPost("no-match2", "Other2"))
		addHttpRequestParameter("query", "sone")
		addHttpRequestParameter("postPage", "1")
		verifyNoRedirect {
			assertThat(this["postPagination"], isOnPage(1).hasPages(2))
			assertThat(this["postHits"], contains(sones[3]))
		}
	}

	@Test
	fun `post search results are cached`() {
		val post = createPost("with-match", "text")
		val callCounter = AtomicInteger()
		whenever(post.text).thenAnswer { callCounter.incrementAndGet(); "text" }
		val sone = createSoneWithPost(post)
		addSone("sone", sone)
		addHttpRequestParameter("query", "text")
		verifyNoRedirect {
			assertThat(this["postHits"], contains(post))
		}
		verifyNoRedirect {
			assertThat(callCounter.get(), equalTo(1))
		}
	}

	@Test
	fun `post search results are cached for five minutes`() {
		val post = createPost("with-match", "text")
		val callCounter = AtomicInteger()
		whenever(post.text).thenAnswer { callCounter.incrementAndGet(); "text" }
		val sone = createSoneWithPost(post)
		addSone("sone", sone)
		addHttpRequestParameter("query", "text")
		verifyNoRedirect {
			assertThat(this["postHits"], contains(post))
		}
		whenever(ticker.read()).thenReturn(TimeUnit.MINUTES.toNanos(5) + 1)
		verifyNoRedirect {
			assertThat(callCounter.get(), equalTo(2))
		}
	}

	@Suppress("UNCHECKED_CAST")
	private operator fun <T> get(key: String): T? = templateContext[key] as? T

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<SearchPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/search.html"))
	}

}
