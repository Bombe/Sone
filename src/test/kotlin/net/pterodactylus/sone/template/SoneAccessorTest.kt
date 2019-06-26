package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.Sone.SoneStatus
import net.pterodactylus.sone.data.Sone.SoneStatus.downloading
import net.pterodactylus.sone.data.Sone.SoneStatus.idle
import net.pterodactylus.sone.data.Sone.SoneStatus.inserting
import net.pterodactylus.sone.data.Sone.SoneStatus.unknown
import net.pterodactylus.sone.freenet.L10nText
import net.pterodactylus.sone.freenet.wot.Identity
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.freenet.wot.Trust
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.text.TimeText
import net.pterodactylus.sone.text.TimeTextConverter
import net.pterodactylus.util.template.TemplateContext
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test

/**
 * Unit test for [SoneAccessor].
 */
class SoneAccessorTest {

	private val core = mock<Core>()
	private val timeTextConverter = mock<TimeTextConverter>()
	private val accessor = SoneAccessor(core, timeTextConverter)
	private val templateContext = mock<TemplateContext>()
	private val currentSone = mock<Sone>()
	private val currentIdentity = mock<OwnIdentity>()
	private val sone = mock<Sone>()
	private val remoteIdentity = mock<Identity>()

	@Before
	fun setupSone() {
		whenever(sone.id).thenReturn("sone-id")
		whenever(sone.name).thenReturn("sone-name")
		whenever(sone.profile).thenReturn(Profile(sone))
		whenever(sone.identity).thenReturn(remoteIdentity)
		whenever(currentSone.identity).thenReturn(currentIdentity)
	}

	@Before
	fun setupTemplateContext() {
		whenever(templateContext["currentSone"]).thenReturn(currentSone)
	}

	private fun assertAccessorReturnValue(member: String, expected: Any?) {
		assertThat(accessor.get(templateContext, sone, member), equalTo(expected))
	}

	@Suppress("UNCHECKED_CAST")
	private fun <T : Any> assertAccessorReturnValueMatches(member: String, matcher: Matcher<in T>) {
		assertThat(accessor.get(templateContext, sone, member) as T, matcher)
	}

	@Test
	fun `accessor returns nice name of a sone`() {
		assertAccessorReturnValue("niceName", "sone-name")
	}

	@Test
	fun `accessor returns that given sone is not a friend of the current sone if there is no current sone`() {
		whenever(templateContext["currentSone"]).thenReturn(null)
		assertAccessorReturnValue("friend", false)
	}

	@Test
	fun `accessor returns that given sone is not a friend of the current sone if the given sone is not a friend`() {
		assertAccessorReturnValue("friend", false)
	}

	@Test
	fun `accessor returns that given sone is a friend of the current sone if the given sone is a friend`() {
		whenever(currentSone.hasFriend("sone-id")).thenReturn(true)
		assertAccessorReturnValue("friend", true)
	}

	@Test
	fun `accessor returns that the given sone is not the current sone if there is no current sone`() {
		whenever(templateContext["currentSone"]).thenReturn(null)
		assertAccessorReturnValue("current", false)
	}

	@Test
	fun `accessor returns that the given sone is not the current sone if it is not`() {
		assertAccessorReturnValue("current", false)
	}

	@Test
	fun `accessor returns that the given sone is the current sone if it is `() {
		whenever(templateContext["currentSone"]).thenReturn(sone)
		assertAccessorReturnValue("current", true)
	}

	@Test
	fun `accessor returns that a sone was not modified if the sone was not modified`() {
		assertAccessorReturnValue("modified", false)
	}

	@Test
	fun `accessor returns that a sone was modified if the sone was modified`() {
		whenever(core.isModifiedSone(sone)).thenReturn(true)
		assertAccessorReturnValue("modified", true)
	}

	@Test
	fun `accessor returns the sone’s status`() {
		val soneStatus = mock<SoneStatus>()
		whenever(sone.status).thenReturn(soneStatus)
		assertAccessorReturnValue("status", soneStatus)
	}

	@Test
	fun `accessor returns that the sone’s status is unknown if it is unknown`() {
		whenever(sone.status).thenReturn(unknown)
		assertAccessorReturnValue("unknown", true)
	}

	@Test
	fun `accessor returns that the sone’s status is not unknown if it is not unknown`() {
		whenever(sone.status).thenReturn(mock())
		assertAccessorReturnValue("unknown", false)
	}

	@Test
	fun `accessor returns that the sone’s status is idle if it is idle`() {
		whenever(sone.status).thenReturn(idle)
		assertAccessorReturnValue("idle", true)
	}

	@Test
	fun `accessor returns that the sone’s status is not idle if it is not idle`() {
		whenever(sone.status).thenReturn(mock())
		assertAccessorReturnValue("idle", false)
	}

	@Test
	fun `accessor returns that the sone’s status is inserting if it is inserting`() {
		whenever(sone.status).thenReturn(inserting)
		assertAccessorReturnValue("inserting", true)
	}

	@Test
	fun `accessor returns that the sone’s status is not inserting if it is not inserting`() {
		whenever(sone.status).thenReturn(mock())
		assertAccessorReturnValue("inserting", false)
	}

	@Test
	fun `accessor returns that the sone’s status is downloading if it is downloading`() {
		whenever(sone.status).thenReturn(downloading)
		assertAccessorReturnValue("downloading", true)
	}

	@Test
	fun `accessor returns that the sone’s status is not downloading if it is not downloading`() {
		whenever(sone.status).thenReturn(mock())
		assertAccessorReturnValue("downloading", false)
	}

	@Test
	fun `accessor returns that the sone is new if it is not known`() {
		assertAccessorReturnValue("new", true)
	}

	@Test
	fun `accessor returns that the sone is not new if it is known`() {
		whenever(sone.isKnown).thenReturn(true)
		assertAccessorReturnValue("new", false)
	}

	@Test
	fun `accessor returns that the sone is not locked if it is not locked`() {
		assertAccessorReturnValue("locked", false)
	}

	@Test
	fun `accessor returns that the sone is locked if it is locked`() {
		whenever(core.isLocked(sone)).thenReturn(true)
		assertAccessorReturnValue("locked", true)
	}

	@Test
	fun `accessor returns l10n text for last update time`() {
		whenever(sone.time).thenReturn(12345)
		whenever(timeTextConverter.getTimeText(12345L)).thenReturn(TimeText(L10nText("l10n.key", listOf(3L)), 23456))
		assertAccessorReturnValue("lastUpdatedText", L10nText("l10n.key", listOf(3L)))
	}

	@Test
	fun `accessor returns null trust if there is no current sone`() {
		whenever(templateContext["currentSone"]).thenReturn(null)
		assertAccessorReturnValue("trust", null)
	}

	@Test
	fun `accessor returns trust with null values if there is no trust from the current sone`() {
		assertAccessorReturnValue("trust", Trust(null, null, null))
	}

	@Test
	fun `accessor returns trust if there is trust from the current sone`() {
		val trust = mock<Trust>()
		whenever(remoteIdentity.getTrust(currentIdentity)).thenReturn(trust)
		assertAccessorReturnValue("trust", trust)
	}

	@Test
	fun `accessor returns all images in the correct order`() {
		val images = listOf(mock<Image>(), mock(), mock(), mock(), mock())
		val firstAlbum = createAlbum(listOf(), listOf(images[0], images[3]))
		val secondAlbum = createAlbum(listOf(), listOf(images[1], images[4], images[2]))
		val rootAlbum = createAlbum(listOf(firstAlbum, secondAlbum), listOf())
		whenever(sone.rootAlbum).thenReturn(rootAlbum)
		assertAccessorReturnValueMatches("allImages", contains(images[0], images[3], images[1], images[4], images[2]))
	}

	private fun createAlbum(albums: List<Album>, images: List<Image>) =
			mock<Album>().apply {
				whenever(this.albums).thenReturn(albums)
				whenever(this.images).thenReturn(images)
			}

	@Test
	fun `accessor returns all albums in the correct order`() {
		val albums = listOf(mock<Album>(), mock(), mock(), mock(), mock())
		val rootAlbum = createAlbum(albums, listOf())
		whenever(sone.rootAlbum).thenReturn(rootAlbum)
		assertAccessorReturnValueMatches("albums", contains(*albums.toTypedArray()))
	}

	@Test
	fun `reflection accessor is used for other members`() {
	    assertAccessorReturnValue("hashCode", sone.hashCode())
	}

}
