package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

/**
 * Unit test for [KnownSonesPage].
 */
class KnownSonesPageTest : WebPageTest(::KnownSonesPage) {

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

	private fun createSone(time: Long, posts: Int, replies: Int, images: Int, name: String, local: Boolean, new: Boolean) = mock<Sone>().apply {
		whenever(identity).thenReturn(if (local) mock<OwnIdentity>() else mock<Identity>())
		whenever(this.isLocal).thenReturn(local)
		whenever(isKnown).thenReturn(!new)
		whenever(this.time).thenReturn(time)
		whenever(this.posts).thenReturn((0..(posts - 1)).map { mock<Post>() })
		whenever(this.replies).thenReturn((0..(replies - 1)).map { mock<PostReply>() }.toSet())
		val album = AlbumImpl(this)
		repeat(images) {
			ImageImpl().modify().setSone(this).update()
					.also(album::addImage)
		}
		val rootAlbum = AlbumImpl(this).also { it.addAlbum(album) }
		whenever(this.rootAlbum).thenReturn(rootAlbum)
		whenever(this.profile).thenReturn(mock())
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
		addTranslation("Page.KnownSones.Title", "known sones page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("known sones page title"))
	}

	@Test
	fun `default known sones are sorted newest first`() {
		verifyNoRedirect {
			verifySonesAreInOrder(3, 2, 1, 0)
			verifyStoredFields("activity", "desc", "")
		}
	}

	@Test
	fun `known sones can be sorted by oldest first`() {
		addHttpRequestParameter("order", "asc")
		verifyNoRedirect {
			verifySonesAreInOrder(0, 1, 2, 3)
			verifyStoredFields("activity", "asc", "")
		}
	}

	@Test
	fun `known sones can be sorted by posts, most posts first`() {
		addHttpRequestParameter("sort", "posts")
		verifyNoRedirect {
			verifySonesAreInOrder(0, 2, 1, 3)
			verifyStoredFields("posts", "desc", "")
		}
	}

	@Test
	fun `known sones can be sorted by posts, least posts first`() {
		addHttpRequestParameter("sort", "posts")
		addHttpRequestParameter("order", "asc")
		verifyNoRedirect {
			verifySonesAreInOrder(3, 1, 2, 0)
			verifyStoredFields("posts", "asc", "")
		}
	}

	@Test
	fun `known sones can be sorted by images, most images first`() {
		addHttpRequestParameter("sort", "images")
		verifyNoRedirect {
			verifySonesAreInOrder(1, 0, 2, 3)
			verifyStoredFields("images", "desc", "")
		}
	}

	@Test
	fun `known sones can be sorted by images, least images first`() {
		addHttpRequestParameter("sort", "images")
		addHttpRequestParameter("order", "asc")
		verifyNoRedirect {
			verifySonesAreInOrder(3, 2, 0, 1)
			verifyStoredFields("images", "asc", "")
		}
	}

	@Test
	fun `known sones can be sorted by nice name, ascending`() {
		addHttpRequestParameter("sort", "name")
		addHttpRequestParameter("order", "asc")
		verifyNoRedirect {
			verifySonesAreInOrder(3, 1, 0, 2)
			verifyStoredFields("name", "asc", "")
		}
	}

	@Test
	fun `known sones can be sorted by nice name, descending`() {
		addHttpRequestParameter("sort", "name")
		verifyNoRedirect {
			verifySonesAreInOrder(2, 0, 1, 3)
			verifyStoredFields("name", "desc", "")
		}
	}

	@Test
	fun `known sones can be filtered by local sones`() {
		addHttpRequestParameter("filter", "own")
		verifyNoRedirect {
			verifySonesAreInOrder(2, 0)
			verifyStoredFields("activity", "desc", "own")
		}
	}

	@Test
	fun `known sones can be filtered by non-local sones`() {
		addHttpRequestParameter("filter", "not-own")
		verifyNoRedirect {
			verifySonesAreInOrder(3, 1)
			verifyStoredFields("activity", "desc", "not-own")
		}
	}

	@Test
	fun `known sones can be filtered by new sones`() {
		addHttpRequestParameter("filter", "new")
		verifyNoRedirect {
			verifySonesAreInOrder(1, 0)
			verifyStoredFields("activity", "desc", "new")
		}
	}

	@Test
	fun `known sones can be filtered by known sones`() {
		addHttpRequestParameter("filter", "not-new")
		verifyNoRedirect {
			verifySonesAreInOrder(3, 2)
			verifyStoredFields("activity", "desc", "not-new")
		}
	}

	@Test
	fun `known sones can be filtered by followed sones`() {
		addHttpRequestParameter("filter", "followed")
		listOf("sone1", "sone3").forEach { whenever(currentSone.hasFriend(it)).thenReturn(true) }
		verifyNoRedirect {
			verifySonesAreInOrder(2, 1)
			verifyStoredFields("activity", "desc", "followed")
		}
	}

	@Test
	fun `known sones can be filtered by not-followed sones`() {
		addHttpRequestParameter("filter", "not-followed")
		listOf("sone1", "sone3").forEach { whenever(currentSone.hasFriend(it)).thenReturn(true) }
		verifyNoRedirect {
			verifySonesAreInOrder(3, 0)
			verifyStoredFields("activity", "desc", "not-followed")
		}
	}

	@Test
	fun `known sones can not be filtered by followed sones if there is no current sone`() {
		addHttpRequestParameter("filter", "followed")
		unsetCurrentSone()
		verifyNoRedirect {
			verifySonesAreInOrder(3, 2, 1, 0)
			verifyStoredFields("activity", "desc", "followed")
		}
	}

	@Test
	fun `known sones can not be filtered by not-followed sones if there is no current sone`() {
		addHttpRequestParameter("filter", "not-followed")
		unsetCurrentSone()
		verifyNoRedirect {
			verifySonesAreInOrder(3, 2, 1, 0)
			verifyStoredFields("activity", "desc", "not-followed")
		}
	}

	@Test
	fun `pagination is set in template context`() {
		verifyNoRedirect {
			@Suppress("UNCHECKED_CAST")
			assertThat((templateContext["pagination"] as Pagination<Sone>).items, contains(*listOf(3, 2, 1, 0).map { sones[it] }.toTypedArray()))
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<KnownSonesPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with the correct menuname`() {
		assertThat(page.menuName, equalTo("KnownSones"))
	}

	@Test
	fun `page is annotated with corrrect template path`() {
		assertThat(page.templatePath, equalTo("/templates/knownSones.html"))
	}

}
