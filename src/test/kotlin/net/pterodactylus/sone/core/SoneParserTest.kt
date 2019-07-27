package net.pterodactylus.sone.core

import com.codahale.metrics.*
import com.google.common.base.Optional.*
import freenet.crypt.*
import freenet.keys.InsertableClientSSK.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.database.memory.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.config.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.mockito.Mockito.*
import java.lang.System.*
import java.util.concurrent.TimeUnit.*
import kotlin.test.*

/**
 * Unit test for [SoneParser].
 */
class SoneParserTest {

	private val database = MemoryDatabase(Configuration(MapConfigurationBackend()))
	private val metricRegistry = MetricRegistry()
	private val soneParser = SoneParser(database, metricRegistry)
	private val sone = mock<Sone>()

	@BeforeTest
	fun setupSone() {
		setupSone(this.sone, Identity::class.java)
		database.storeSone(sone)
	}

	private fun setupSone(sone: Sone, identityClass: Class<out Identity>) {
		val identity = mock(identityClass)
		val clientSSK = createRandom(DummyRandomSource(), "WoT")
		whenever(identity.requestUri).thenReturn(clientSSK.uri.toString())
		whenever(identity.id).thenReturn("identity")
		whenever(sone.id).thenReturn("identity")
		whenever(sone.identity).thenReturn(identity)
		whenever(sone.requestUri).thenAnswer { clientSSK.uri.setKeyType("USK").setDocName("Sone") }
		whenever(sone.time).thenReturn(currentTimeMillis() - DAYS.toMillis(1))
	}

	@Test
	fun `parsing a sone fails when document is not xml`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-not-xml.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails when document has negative protocol version`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-negative-protocol-version.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails when protocol version is too large`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-too-large-protocol-version.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails when there is no time`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-no-time.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails when time is not numeric`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-time-not-numeric.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails when profile is missing`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-no-profile.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails when profile field is missing afield name`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-profile-missing-field-name.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails when profile field name is empty`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-profile-empty-field-name.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails when profile field name is not unique`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-profile-duplicate-field-name.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone succeeds without payload`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-no-payload.xml")
		assertThat(soneParser.parseSone(sone, inputStream)!!.time, equalTo(1407197508000L))
	}

	@Test
	fun `parsing a local sone succeeds without payload`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-no-payload.xml")
		val localSone = mock<Sone>()
		setupSone(localSone, OwnIdentity::class.java)
		whenever(localSone.isLocal).thenReturn(true)
		val parsedSone = soneParser.parseSone(localSone, inputStream)
		assertThat(parsedSone!!.time, equalTo(1407197508000L))
		assertThat(parsedSone.isLocal, equalTo(true))
	}

	@Test
	fun `parsing a sone succeeds without protocol version`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-missing-protocol-version.xml")
		assertThat(soneParser.parseSone(sone, inputStream), notNullValue())
	}

	@Test
	fun `parsing a sone fails with missing client name`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-missing-client-name.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails with missing client version`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-missing-client-version.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone succeeds with client info`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-client-info.xml")
		assertThat(soneParser.parseSone(sone, inputStream)!!.client, equalTo(Client("some-client", "some-version")))
	}

	@Test
	fun `parsing a sone succeeds with profile`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-profile.xml")
		val profile = soneParser.parseSone(sone, inputStream)!!.profile
		assertThat(profile.firstName, equalTo("first"))
		assertThat(profile.middleName, equalTo("middle"))
		assertThat(profile.lastName, equalTo("last"))
		assertThat(profile.birthDay, equalTo(18))
		assertThat(profile.birthMonth, equalTo(12))
		assertThat(profile.birthYear, equalTo(1976))
	}

	@Test
	fun `parsing a sone succeeds without profile fields`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-fields.xml")
		assertThat(soneParser.parseSone(sone, inputStream), notNullValue())
	}

	@Test
	fun `parsing a sone fails without post id`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-post-id.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails without post time`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-post-time.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails without post text`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-post-text.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails with invalid post time`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-invalid-post-time.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone succeeds with valid post time`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-valid-post-time.xml")
		val posts = soneParser.parseSone(sone, inputStream)!!.posts
		assertThat(posts, hasSize(1))
		assertThat(posts[0].sone.id, equalTo(sone.id))
		assertThat(posts[0].id, equalTo("3de12680-afef-11e9-a124-e713cf8912fe"))
		assertThat(posts[0].time, equalTo(1407197508000L))
		assertThat(posts[0].recipientId, equalTo(absent()))
		assertThat(posts[0].text, equalTo("text"))
	}

	@Test
	fun `parsing a sone succeeds with recipient`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-recipient.xml")
		val posts = soneParser.parseSone(sone, inputStream)!!.posts
		assertThat(posts, hasSize(1))
		assertThat(posts[0].sone.id, equalTo(sone.id))
		assertThat(posts[0].id, equalTo("3de12680-afef-11e9-a124-e713cf8912fe"))
		assertThat(posts[0].time, equalTo(1407197508000L))
		assertThat(posts[0].recipientId, equalTo(of("1234567890123456789012345678901234567890123")))
		assertThat(posts[0].text, equalTo("text"))
	}

	@Test
	fun `parsing a sone succeeds with invalid recipient`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-invalid-recipient.xml")
		val posts = soneParser.parseSone(sone, inputStream)!!.posts
		assertThat(posts, hasSize(1))
		assertThat(posts[0].sone.id, equalTo(sone.id))
		assertThat(posts[0].id, equalTo("3de12680-afef-11e9-a124-e713cf8912fe"))
		assertThat(posts[0].time, equalTo(1407197508000L))
		assertThat(posts[0].recipientId, equalTo(absent()))
		assertThat(posts[0].text, equalTo("text"))
	}

	@Test
	fun `parsing a sone fails without post reply id`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-post-reply-id.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails without post reply post id`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-post-reply-post-id.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails without post reply time`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-post-reply-time.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails without post reply text`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-post-reply-text.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails with invalid post reply time`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-invalid-post-reply-time.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone succeeds with valid post reply time`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-valid-post-reply-time.xml")
		val postReplies = soneParser.parseSone(sone, inputStream)!!.replies
		assertThat(postReplies, hasSize(1))
		val postReply = postReplies.first()
		assertThat(postReply.id, equalTo("5ccba7f4-aff0-11e9-b176-a7b9db60ce98"))
		assertThat(postReply.postId, equalTo("3de12680-afef-11e9-a124-e713cf8912fe"))
		assertThat(postReply.sone.id, equalTo("identity"))
		assertThat(postReply.time, equalTo(1407197508000L))
		assertThat(postReply.text, equalTo("reply-text"))
	}

	@Test
	fun `parsing a sone succeeds without liked post ids`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-liked-post-ids.xml")
		assertThat(soneParser.parseSone(sone, inputStream), notNullValue())
	}

	@Test
	fun `parsing a sone succeeds with liked post ids`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-liked-post-ids.xml")
		assertThat(soneParser.parseSone(sone, inputStream)!!.likedPostIds, equalTo(setOf("liked-post-id")))
	}

	@Test
	fun `parsing a sone succeeds without liked post reply ids`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-liked-post-reply-ids.xml")
		assertThat(soneParser.parseSone(sone, inputStream), notNullValue())
	}

	@Test
	fun `parsing a sone succeeds with liked post reply ids`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-liked-post-reply-ids.xml")
		assertThat(soneParser.parseSone(sone, inputStream)!!.likedReplyIds, equalTo(setOf("liked-post-reply-id")))
	}

	@Test
	fun `parsing a sone succeeds without albums`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-albums.xml")
		assertThat(soneParser.parseSone(sone, inputStream), notNullValue())
	}

	@Test
	fun `parsing a sone fails without album id`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-album-id.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails without album title`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-album-title.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone succeeds with nested albums`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-multiple-albums.xml")
		val parsedSone = soneParser.parseSone(sone, inputStream)
		assertThat(parsedSone, notNullValue())
		assertThat(parsedSone!!.rootAlbum.albums, hasSize(1))
		val album = parsedSone.rootAlbum.albums[0]
		assertThat(album.id, equalTo("album-id-1"))
		assertThat(album.title, equalTo("album-title"))
		assertThat(album.description, equalTo("album-description"))
		assertThat(album.albums, hasSize(1))
		val nestedAlbum = album.albums[0]
		assertThat(nestedAlbum.id, equalTo("album-id-2"))
		assertThat(nestedAlbum.title, equalTo("album-title-2"))
		assertThat(nestedAlbum.description, equalTo("album-description-2"))
		assertThat(nestedAlbum.albums, hasSize(0))
	}

	@Test
	fun `parsing a sone fails with invalid parent album id`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-invalid-parent-album-id.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone succeeds without images`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-images.xml")
		assertThat(soneParser.parseSone(sone, inputStream), notNullValue())
	}

	@Test
	fun `parsing a sone fails without image id`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-image-id.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails without image time`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-image-time.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails without image key`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-image-key.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails without image title`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-image-title.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails without image width`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-image-width.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails without image height`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-image-height.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails with invalid image width`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-invalid-image-width.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone fails with invalid image height`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-invalid-image-height.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
	}

	@Test
	fun `parsing a sone succeeds with image`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-image.xml")
		val sone = soneParser.parseSone(this.sone, inputStream)
		assertThat(sone, notNullValue())
		assertThat(sone!!.rootAlbum.albums, hasSize(1))
		assertThat(sone.rootAlbum.albums[0].images, hasSize(1))
		val image = sone.rootAlbum.albums[0].images[0]
		assertThat(image.id, equalTo("image-id"))
		assertThat(image.creationTime, equalTo(1407197508000L))
		assertThat(image.key, equalTo("KSK@GPLv3.txt"))
		assertThat(image.title, equalTo("image-title"))
		assertThat(image.description, equalTo("image-description"))
		assertThat(image.width, equalTo(1920))
		assertThat(image.height, equalTo(1080))
		assertThat(sone.profile.avatar, equalTo("image-id"))
	}

	@Test
	fun `unsuccessful parsing does not add a histogram entry`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-with-invalid-image-height.xml")
		assertThat(soneParser.parseSone(sone, inputStream), nullValue())
		val histogram = metricRegistry.histogram("sone.parsing.duration")
		assertThat(histogram.count, equalTo(0L))
	}

	@Test
	fun `successful parsing adds histogram entry`() {
		val inputStream = javaClass.getResourceAsStream("sone-parser-without-images.xml")
		assertThat(soneParser.parseSone(sone, inputStream), notNullValue())
		val histogram = metricRegistry.histogram("sone.parsing.duration")
		assertThat(histogram.count, equalTo(1L))
	}

}
