package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.wot.Identity
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.utils.Pagination
import net.pterodactylus.util.web.Method.GET
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test

/**
 * Unit test for [KnownSonesPage].
 */
class KnownSonesPageTest : WebPageTest() {

	private val page = KnownSonesPage(template, webInterface)

	private val sones = listOf(
			createSone(1000, 4, 7, 2, "sone2", true, true),
			createSone(2000, 3, 2, 3, "Sone1", false, true),
			createSone(3000, 3, 8, 1, "Sone3", true, false),
			createSone(4000, 1, 6, 0, "sone0", false, false)
	)

	@Before
	fun setupSones() {
		addSone("sone1", sones[0])
		addSone("sone2", sones[1])
		addSone("sone3", sones[2])
		addSone("sone4", sones[3])
	}

	@Before
	fun setupGetRequests() {
		setMethod(GET)
	}

	private fun createSone(time: Long, posts: Int, replies: Int, images: Int, name: String, local: Boolean, new: Boolean) = mock<Sone>().apply {
		whenever(identity).thenReturn(if (local) mock<OwnIdentity>() else mock<Identity>())
		whenever(this.isLocal).thenReturn(local)
		whenever(isKnown).thenReturn(!new)
		whenever(this.time).thenReturn(time)
		whenever(this.posts).thenReturn((0..(posts - 1)).map { mock<Post>() })
		whenever(this.replies).thenReturn((0..(replies - 1)).map { mock<PostReply>() }.toSet())
		val album = mock<Album>()
		whenever(album.images).thenReturn(((0..(images - 1)).map { mock<Image>() }))
		val rootAlbum = mock<Album>().apply {
			whenever(albums).thenReturn(listOf(album))
		}
		whenever(this.rootAlbum).thenReturn(rootAlbum)
		whenever(this.profile).thenReturn(mock<Profile>())
		whenever(id).thenReturn(name.toLowerCase())
		whenever(this.name).thenReturn(name)
	}

	private fun verifySonesAreInOrder(vararg indices: Int) {
		@Suppress("UNCHECKED_CAST")
		assertThat(templateContext["knownSones"] as Iterable<Sone>, contains(
				*indices.map { sones[it] }.toTypedArray()
		))
	}

	private fun verifyStoredFields(sort: String, order: String, filter: String) {
		assertThat(templateContext["sort"], equalTo<Any>(sort))
		assertThat(templateContext["order"], equalTo<Any>(order))
		assertThat(templateContext["filter"], equalTo<Any>(filter))
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("knownSones.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page returns correct title`() {
		whenever(l10n.getString("Page.KnownSones.Title")).thenReturn("known sones page title")
		assertThat(page.getPageTitle(freenetRequest), equalTo("known sones page title"))
	}

	@Test
	fun `default known sones are sorted newest first`() {
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(3, 2, 1, 0)
		verifyStoredFields("activity", "desc", "")
	}

	@Test
	fun `known sones can be sorted by oldest first`() {
		addHttpRequestParameter("order", "asc")
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(0, 1, 2, 3)
		verifyStoredFields("activity", "asc", "")
	}

	@Test
	fun `known sones can be sorted by posts, most posts first`() {
		addHttpRequestParameter("sort", "posts")
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(0, 2, 1, 3)
		verifyStoredFields("posts", "desc", "")
	}

	@Test
	fun `known sones can be sorted by posts, least posts first`() {
		addHttpRequestParameter("sort", "posts")
		addHttpRequestParameter("order", "asc")
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(3, 1, 2, 0)
		verifyStoredFields("posts", "asc", "")
	}

	@Test
	fun `known sones can be sorted by images, most images first`() {
		addHttpRequestParameter("sort", "images")
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(1, 0, 2, 3)
		verifyStoredFields("images", "desc", "")
	}

	@Test
	fun `known sones can be sorted by images, least images first`() {
		addHttpRequestParameter("sort", "images")
		addHttpRequestParameter("order", "asc")
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(3, 2, 0, 1)
		verifyStoredFields("images", "asc", "")
	}

	@Test
	fun `known sones can be sorted by nice name, ascending`() {
		addHttpRequestParameter("sort", "name")
		addHttpRequestParameter("order", "asc")
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(3, 1, 0, 2)
		verifyStoredFields("name", "asc", "")
	}

	@Test
	fun `known sones can be sorted by nice name, descending`() {
		addHttpRequestParameter("sort", "name")
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(2, 0, 1, 3)
		verifyStoredFields("name", "desc", "")
	}

	@Test
	fun `known sones can be filtered by local sones`() {
		addHttpRequestParameter("filter", "own")
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(2, 0)
		verifyStoredFields("activity", "desc", "own")
	}

	@Test
	fun `known sones can be filtered by non-local sones`() {
		addHttpRequestParameter("filter", "not-own")
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(3, 1)
		verifyStoredFields("activity", "desc", "not-own")
	}

	@Test
	fun `known sones can be filtered by new sones`() {
		addHttpRequestParameter("filter", "new")
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(1, 0)
		verifyStoredFields("activity", "desc", "new")
	}

	@Test
	fun `known sones can be filtered by known sones`() {
		addHttpRequestParameter("filter", "not-new")
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(3, 2)
		verifyStoredFields("activity", "desc", "not-new")
	}

	@Test
	fun `known sones can be filtered by followed sones`() {
		addHttpRequestParameter("filter", "followed")
		listOf("sone1", "sone3").forEach { whenever(currentSone.hasFriend(it)).thenReturn(true) }
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(2, 1)
		verifyStoredFields("activity", "desc", "followed")
	}

	@Test
	fun `known sones can be filtered by not-followed sones`() {
		addHttpRequestParameter("filter", "not-followed")
		listOf("sone1", "sone3").forEach { whenever(currentSone.hasFriend(it)).thenReturn(true) }
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(3, 0)
		verifyStoredFields("activity", "desc", "not-followed")
	}

	@Test
	fun `known sones can not be filtered by followed sones if there is no current sone`() {
		addHttpRequestParameter("filter", "followed")
		unsetCurrentSone()
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(3, 2, 1, 0)
		verifyStoredFields("activity", "desc", "followed")
	}

	@Test
	fun `known sones can not be filtered by not-followed sones if there is no current sone`() {
		addHttpRequestParameter("filter", "not-followed")
		unsetCurrentSone()
		page.processTemplate(freenetRequest, templateContext)
		verifySonesAreInOrder(3, 2, 1, 0)
		verifyStoredFields("activity", "desc", "not-followed")
	}

	@Test
	fun `pagination is set in template context`() {
		page.processTemplate(freenetRequest, templateContext)
		@Suppress("UNCHECKED_CAST")
		assertThat((templateContext["pagination"] as Pagination<Sone>).items, contains(*listOf(3, 2, 1, 0).map { sones[it] }.toTypedArray()))
	}

}
