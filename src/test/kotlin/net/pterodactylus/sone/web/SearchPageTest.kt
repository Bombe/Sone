package net.pterodactylus.sone.web

import com.google.common.base.Optional.absent
import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Test

/**
 * Unit test for [SearchPage].
 */
class SearchPageTest : WebPageTest() {

	private val page = SearchPage(template, webInterface)

	override fun getPage() = page

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
		addSone("sone-id", mock<Sone>())
		addHttpRequestParameter("query", "sone://sone-id")
		verifyRedirect("viewSone.html?sone=sone-id")
	}

	@Test
	fun `searching for sone link without prefix redirects to view sone page`() {
		addSone("sone-id", mock<Sone>())
		addHttpRequestParameter("query", "sone-id")
		verifyRedirect("viewSone.html?sone=sone-id")
	}

	@Test
	fun `searching for a post link redirects to post page`() {
		addPost("post-id", mock<Post>())
		addHttpRequestParameter("query", "post://post-id")
		verifyRedirect("viewPost.html?post=post-id")
	}

	@Test
	fun `searching for a post ID without prefix redirects to post page`() {
		addPost("post-id", mock<Post>())
		addHttpRequestParameter("query", "post-id")
		verifyRedirect("viewPost.html?post=post-id")
	}

	@Test
	fun `searching for a reply link redirects to the post page`() {
		val postReply = mock<PostReply>().apply { whenever(postId).thenReturn("post-id") }
		addPostReply("reply-id", postReply)
		addHttpRequestParameter("query", "reply://reply-id")
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
		addAlbum("album-id", mock<Album>())
		addHttpRequestParameter("query", "album://album-id")
		verifyRedirect("imageBrowser.html?album=album-id")
	}

	@Test
	fun `searching for an album ID redirects to the image browser`() {
		addAlbum("album-id", mock<Album>())
		addHttpRequestParameter("query", "album-id")
		verifyRedirect("imageBrowser.html?album=album-id")
	}

	@Test
	fun `searching for an image link redirects to the image browser`() {
		addImage("image-id", mock<Image>())
		addHttpRequestParameter("query", "image://image-id")
		verifyRedirect("imageBrowser.html?image=image-id")
	}

	@Test
	fun `searching for an image ID redirects to the image browser`() {
		addImage("image-id", mock<Image>())
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

	private fun createSoneWithPost(post: Post) = mock<Sone>().apply {
		whenever(posts).thenReturn(listOf(post))
		whenever(profile).thenReturn(Profile(this))
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `searching for a single word finds the post`() {
		val postWithMatch = createPost("post-with-match", "the word here")
		val postWithoutMatch = createPost("post-without-match", "no match here")
		val soneWithMatch = createSoneWithPost(postWithMatch)
		val soneWithoutMatch = createSoneWithPost(postWithoutMatch)
		addSone("sone-with-match", soneWithMatch)
		addSone("sone-without-match", soneWithoutMatch)
		addHttpRequestParameter("query", "word")
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["postHits"] as Collection<Post>, contains<Post>(postWithMatch))
	}

	@Test
	@Suppress("UNCHECKED_CAST")
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
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["postHits"] as Collection<Post>, contains<Post>(postWithMatch))
	}

	private fun createSoneWithPost(idPostfix: String, text: String) =
			createPost("post-$idPostfix", text).apply {
				addSone("sone-$idPostfix", createSoneWithPost(this))
			}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `earlier matches score higher than later matches`() {
		val postWithEarlyMatch = createSoneWithPost("with-early-match", "optional match")
		val postWithLaterMatch = createSoneWithPost("with-later-match", "match that is optional")
		addHttpRequestParameter("query", "optional ")
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["postHits"] as Collection<Post>, contains<Post>(postWithEarlyMatch, postWithLaterMatch))
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `searching for required word does not return posts without that word`() {
		val postWithRequiredMatch = createSoneWithPost("with-required-match", "required match")
		createPost("without-required-match", "not a match")
		addHttpRequestParameter("query", "+required ")
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["postHits"] as Collection<Post>, contains<Post>(postWithRequiredMatch))
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `searching for forbidden word does not return posts with that word`() {
		createSoneWithPost("with-forbidden-match", "forbidden match")
		val postWithoutForbiddenMatch = createSoneWithPost("without-forbidden-match", "not a match")
		addHttpRequestParameter("query", "match -forbidden")
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["postHits"] as Collection<Post>, contains<Post>(postWithoutForbiddenMatch))
	}

}
