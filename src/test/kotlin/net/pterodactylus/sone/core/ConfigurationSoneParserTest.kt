package net.pterodactylus.sone.core

import com.google.common.base.Optional.*
import net.pterodactylus.sone.core.ConfigurationSoneParser.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.test.Matchers.*
import net.pterodactylus.util.config.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.rules.*
import org.mockito.ArgumentMatchers.*
import org.mockito.ArgumentMatchers.eq

/**
 * Unit test for [ConfigurationSoneParser].
 */
class ConfigurationSoneParserTest {

	@Rule
	@JvmField
	val expectedException = ExpectedException.none()!!

	private val configuration = mock<Configuration>()
	private val sone = mock<Sone>().apply {
		whenever(this.id).thenReturn("1")
	}
	private val configurationSoneParser = ConfigurationSoneParser(configuration, sone)

	@Test
	fun emptyProfileIsLoadedCorrectly() {
		setupEmptyProfile()
		val profile = configurationSoneParser.parseProfile()
		assertThat(profile, notNullValue())
		assertThat(profile.firstName, nullValue())
		assertThat(profile.middleName, nullValue())
		assertThat(profile.lastName, nullValue())
		assertThat(profile.birthDay, nullValue())
		assertThat(profile.birthMonth, nullValue())
		assertThat(profile.birthYear, nullValue())
		assertThat(profile.fields, emptyIterable())
	}

	private fun setupEmptyProfile() {
		whenever(configuration.getStringValue(anyString())).thenReturn(TestValue.from(null))
		whenever(configuration.getIntValue(anyString())).thenReturn(TestValue.from(null))
	}

	@Test
	fun filledProfileWithFieldsIsParsedCorrectly() {
		setupFilledProfile()
		val profile = configurationSoneParser.parseProfile()
		assertThat(profile, notNullValue())
		assertThat(profile.firstName, equalTo("First"))
		assertThat(profile.middleName, equalTo("M."))
		assertThat(profile.lastName, equalTo("Last"))
		assertThat(profile.birthDay, equalTo(18))
		assertThat(profile.birthMonth, equalTo(12))
		assertThat(profile.birthYear, equalTo(1976))
		val fields = profile.fields
		assertThat(fields, hasSize<Any>(2))
		assertThat(fields[0].name, equalTo("Field1"))
		assertThat(fields[0].value, equalTo("Value1"))
		assertThat(fields[1].name, equalTo("Field2"))
		assertThat(fields[1].value, equalTo("Value2"))
	}

	private fun setupFilledProfile() {
		setupString("Sone/1/Profile/FirstName", "First")
		setupString("Sone/1/Profile/MiddleName", "M.")
		setupString("Sone/1/Profile/LastName", "Last")
		setupInteger("Sone/1/Profile/BirthDay", 18)
		setupInteger("Sone/1/Profile/BirthMonth", 12)
		setupInteger("Sone/1/Profile/BirthYear", 1976)
		setupString("Sone/1/Profile/Fields/0/Name", "Field1")
		setupString("Sone/1/Profile/Fields/0/Value", "Value1")
		setupString("Sone/1/Profile/Fields/1/Name", "Field2")
		setupString("Sone/1/Profile/Fields/1/Value", "Value2")
		setupString("Sone/1/Profile/Fields/2/Name")
	}

	private fun setupString(nodeName: String, value: String? = null) {
		whenever(configuration.getStringValue(eq(nodeName))).thenReturn(TestValue.from(value))
	}

	private fun setupInteger(nodeName: String, value: Int?) {
		whenever(configuration.getIntValue(eq(nodeName))).thenReturn(TestValue.from(value))
	}

	@Test
	fun postsAreParsedCorrectly() {
		setupCompletePosts()
		val postBuilderFactory = createPostBuilderFactory()
		val posts = configurationSoneParser.parsePosts(postBuilderFactory)
		assertThat(posts, containsInAnyOrder(
				isPost("P0", 1000L, "T0", null),
				isPost("P1", 1001L, "T1", "1234567890123456789012345678901234567890123")
		))
	}

	private fun createPostBuilderFactory(): PostBuilderFactory {
		val postBuilderFactory = mock<PostBuilderFactory>()
		whenever(postBuilderFactory.newPostBuilder()).thenAnswer { TestPostBuilder() }
		return postBuilderFactory
	}

	private fun setupCompletePosts() {
		setupPost("0", "P0", 1000L, "T0")
		setupPost("1", "P1", 1001L, "T1", "1234567890123456789012345678901234567890123")
		setupPost("2")
	}

	private fun setupPost(postNumber: String, postId: String? = null, time: Long = 0, text: String? = null, recipientId: String? = null) {
		setupString("Sone/1/Posts/$postNumber/ID", postId)
		setupLong("Sone/1/Posts/$postNumber/Time", time)
		setupString("Sone/1/Posts/$postNumber/Text", text)
		setupString("Sone/1/Posts/$postNumber/Recipient", recipientId)
	}

	private fun setupLong(nodeName: String, value: Long?) {
		whenever(configuration.getLongValue(eq(nodeName))).thenReturn(TestValue.from(value))
	}

	@Test
	fun postWithoutTimeIsRecognized() {
		setupPostWithoutTime()
		expectedException.expect<InvalidPostFound>()
		configurationSoneParser.parsePosts(createPostBuilderFactory())
	}

	private fun setupPostWithoutTime() {
		setupPost("0", "P0", 0L, "T0")
	}

	@Test
	fun postWithoutTextIsRecognized() {
		setupPostWithoutText()
		expectedException.expect<InvalidPostFound>()
		configurationSoneParser.parsePosts(createPostBuilderFactory())
	}

	private fun setupPostWithoutText() {
		setupPost("0", "P0", 1000L)
	}

	@Test
	fun postWithInvalidRecipientIdIsRecognized() {
		setupPostWithInvalidRecipientId()
		val posts = configurationSoneParser.parsePosts(createPostBuilderFactory())
		assertThat(posts, contains(isPost("P0", 1000L, "T0", null)))
	}

	private fun setupPostWithInvalidRecipientId() {
		setupPost("0", "P0", 1000L, "T0", "123")
		setupPost("1")
	}

	@Test
	fun postRepliesAreParsedCorrectly() {
		setupPostReplies()
		val postReplyBuilderFactory = object : PostReplyBuilderFactory {
			override fun newPostReplyBuilder(): PostReplyBuilder {
				return TestPostReplyBuilder()
			}
		}
		val postReplies = configurationSoneParser.parsePostReplies(postReplyBuilderFactory)
		assertThat(postReplies, hasSize(2))
		assertThat(postReplies, containsInAnyOrder(
				isPostReply("R0", "P0", 1000L, "T0"),
				isPostReply("R1", "P1", 1001L, "T1")
		))
	}

	private fun setupPostReplies() {
		setupPostReply("0", "R0", "P0", 1000L, "T0")
		setupPostReply("1", "R1", "P1", 1001L, "T1")
		setupPostReply("2")
	}

	private fun setupPostReply(postReplyNumber: String, postReplyId: String? = null, postId: String? = null, time: Long = 0, text: String? = null) {
		setupString("Sone/1/Replies/$postReplyNumber/ID", postReplyId)
		setupString("Sone/1/Replies/$postReplyNumber/Post/ID", postId)
		setupLong("Sone/1/Replies/$postReplyNumber/Time", time)
		setupString("Sone/1/Replies/$postReplyNumber/Text", text)
	}

	@Test
	fun missingPostIdIsRecognized() {
		setupPostReplyWithMissingPostId()
		expectedException.expect<InvalidPostReplyFound>()
		configurationSoneParser.parsePostReplies(null)
	}

	private fun setupPostReplyWithMissingPostId() {
		setupPostReply("0", "R0", null, 1000L, "T0")
	}

	@Test
	fun missingPostReplyTimeIsRecognized() {
		setupPostReplyWithMissingPostReplyTime()
		expectedException.expect<InvalidPostReplyFound>()
		configurationSoneParser.parsePostReplies(null)
	}

	private fun setupPostReplyWithMissingPostReplyTime() {
		setupPostReply("0", "R0", "P0", 0L, "T0")
	}

	@Test
	fun missingPostReplyTextIsRecognized() {
		setupPostReplyWithMissingPostReplyText()
		expectedException.expect<InvalidPostReplyFound>()
		configurationSoneParser.parsePostReplies(null)
	}

	private fun setupPostReplyWithMissingPostReplyText() {
		setupPostReply("0", "R0", "P0", 1000L)
	}

	@Test
	fun likedPostIdsParsedCorrectly() {
		setupLikedPostIds()
		val likedPostIds = configurationSoneParser.parseLikedPostIds()
		assertThat(likedPostIds, containsInAnyOrder("P1", "P2", "P3"))
	}

	private fun setupLikedPostIds() {
		setupString("Sone/1/Likes/Post/0/ID", "P1")
		setupString("Sone/1/Likes/Post/1/ID", "P2")
		setupString("Sone/1/Likes/Post/2/ID", "P3")
		setupString("Sone/1/Likes/Post/3/ID")
	}

	@Test
	fun likedPostReplyIdsAreParsedCorrectly() {
		setupLikedPostReplyIds()
		val likedPostReplyIds = configurationSoneParser.parseLikedPostReplyIds()
		assertThat(likedPostReplyIds, containsInAnyOrder("R1", "R2", "R3"))
	}

	private fun setupLikedPostReplyIds() {
		setupString("Sone/1/Likes/Reply/0/ID", "R1")
		setupString("Sone/1/Likes/Reply/1/ID", "R2")
		setupString("Sone/1/Likes/Reply/2/ID", "R3")
		setupString("Sone/1/Likes/Reply/3/ID")
	}

	@Test
	fun friendsAreParsedCorrectly() {
		setupFriends()
		val friends = configurationSoneParser.parseFriends()
		assertThat(friends, containsInAnyOrder("F1", "F2", "F3"))
	}

	private fun setupFriends() {
		setupString("Sone/1/Friends/0/ID", "F1")
		setupString("Sone/1/Friends/1/ID", "F2")
		setupString("Sone/1/Friends/2/ID", "F3")
		setupString("Sone/1/Friends/3/ID")
	}

	@Test
	fun topLevelAlbumsAreParsedCorrectly() {
		setupTopLevelAlbums()
		val albumBuilderFactory = createAlbumBuilderFactory()
		val topLevelAlbums = configurationSoneParser.parseTopLevelAlbums(albumBuilderFactory)
		assertThat(topLevelAlbums, hasSize<Any>(2))
		val firstAlbum = topLevelAlbums[0]
		assertThat(firstAlbum, isAlbum("A1", null, "T1", "D1"))
		assertThat(firstAlbum.albums, emptyIterable<Any>())
		assertThat<List<Image>>(firstAlbum.images, emptyIterable<Any>())
		val secondAlbum = topLevelAlbums[1]
		assertThat(secondAlbum, isAlbum("A2", null, "T2", "D2"))
		assertThat(secondAlbum.albums, hasSize<Any>(1))
		assertThat<List<Image>>(secondAlbum.images, emptyIterable<Any>())
		val thirdAlbum = secondAlbum.albums[0]
		assertThat(thirdAlbum, isAlbum("A3", "A2", "T3", "D3"))
		assertThat(thirdAlbum.albums, emptyIterable<Any>())
		assertThat<List<Image>>(thirdAlbum.images, emptyIterable<Any>())
	}

	private fun setupTopLevelAlbums() {
		setupAlbum(0, "A1", null, "T1", "D1", "I1")
		setupAlbum(1, "A2", null, "T2", "D2")
		setupAlbum(2, "A3", "A2", "T3", "D3", "I3")
		setupAlbum(3)
	}

	private fun setupAlbum(albumNumber: Int, albumId: String? = null, parentAlbumId: String? = null, title: String? = null, description: String? = null, imageId: String? =null) {
		val albumPrefix = "Sone/1/Albums/$albumNumber"
		setupString("$albumPrefix/ID", albumId)
		setupString("$albumPrefix/Title", title)
		setupString("$albumPrefix/Description", description)
		setupString("$albumPrefix/Parent", parentAlbumId)
		setupString("$albumPrefix/AlbumImage", imageId)
	}

	private fun createAlbumBuilderFactory(): AlbumBuilderFactory {
		val albumBuilderFactory = mock<AlbumBuilderFactory>()
		whenever(albumBuilderFactory.newAlbumBuilder()).thenAnswer { TestAlbumBuilder() }
		return albumBuilderFactory
	}

	@Test
	fun albumWithInvalidTitleIsRecognized() {
		setupAlbum(0, "A1", null, null, "D1", "I1")
		expectedException.expect<InvalidAlbumFound>()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
	}

	@Test
	fun albumWithInvalidDescriptionIsRecognized() {
		setupAlbum(0, "A1", null, "T1", null, "I1")
		expectedException.expect<InvalidAlbumFound>()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
	}

	@Test
	fun albumWithInvalidParentIsRecognized() {
		setupAlbum(0, "A1", "A0", "T1", "D1", "I1")
		expectedException.expect<InvalidParentAlbumFound>()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
	}

	@Test
	fun imagesAreParsedCorrectly() {
		setupTopLevelAlbums()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
		setupImages()
		configurationSoneParser.parseImages(createImageBuilderFactory())
		val albums = configurationSoneParser.albums
		assertThat<List<Image>>(albums["A1"]!!.images, contains<Image>(isImage("I1", 1000L, "K1", "T1", "D1", 16, 9)))
		assertThat<List<Image>>(albums["A2"]!!.images, contains<Image>(isImage("I2", 2000L, "K2", "T2", "D2", 16 * 2, 9 * 2)))
		assertThat<List<Image>>(albums["A3"]!!.images, contains<Image>(isImage("I3", 3000L, "K3", "T3", "D3", 16 * 3, 9 * 3)))
	}

	private fun setupImages() {
		setupImage(0, "I1", "A1", 1000L, "K1", "T1", "D1", 16, 9)
		setupImage(1, "I2", "A2", 2000L, "K2", "T2", "D2", 16 * 2, 9 * 2)
		setupImage(2, "I3", "A3", 3000L, "K3", "T3", "D3", 16 * 3, 9 * 3)
		setupImage(3, null, null, 0L, null, null, null, 0, 0)
	}

	private fun setupImage(imageNumber: Int, id: String?, parentAlbumId: String?, creationTime: Long?, key: String?, title: String?, description: String?, width: Int?, height: Int?) {
		val imagePrefix = "Sone/1/Images/$imageNumber"
		setupString("$imagePrefix/ID", id)
		setupString("$imagePrefix/Album", parentAlbumId)
		setupLong("$imagePrefix/CreationTime", creationTime)
		setupString("$imagePrefix/Key", key)
		setupString("$imagePrefix/Title", title)
		setupString("$imagePrefix/Description", description)
		setupInteger("$imagePrefix/Width", width)
		setupInteger("$imagePrefix/Height", height)
	}

	private fun createImageBuilderFactory(): ImageBuilderFactory {
		val imageBuilderFactory = mock<ImageBuilderFactory>()
		whenever(imageBuilderFactory.newImageBuilder()).thenAnswer { TestImageBuilder() }
		return imageBuilderFactory
	}

	@Test
	fun missingAlbumIdIsRecognized() {
		setupTopLevelAlbums()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
		setupImage(0, "I1", null, 1000L, "K1", "T1", "D1", 16, 9)
		expectedException.expect<InvalidImageFound>()
		configurationSoneParser.parseImages(createImageBuilderFactory())
	}

	@Test
	fun invalidAlbumIdIsRecognized() {
		setupTopLevelAlbums()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
		setupImage(0, "I1", "A4", 1000L, "K1", "T1", "D1", 16, 9)
		expectedException.expect<InvalidParentAlbumFound>()
		configurationSoneParser.parseImages(createImageBuilderFactory())
	}

	@Test
	fun missingCreationTimeIsRecognized() {
		setupTopLevelAlbums()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
		setupImage(0, "I1", "A1", null, "K1", "T1", "D1", 16, 9)
		expectedException.expect<InvalidImageFound>()
		configurationSoneParser.parseImages(createImageBuilderFactory())
	}

	@Test
	fun missingKeyIsRecognized() {
		setupTopLevelAlbums()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
		setupImage(0, "I1", "A1", 1000L, null, "T1", "D1", 16, 9)
		expectedException.expect<InvalidImageFound>()
		configurationSoneParser.parseImages(createImageBuilderFactory())
	}

	@Test
	fun missingTitleIsRecognized() {
		setupTopLevelAlbums()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
		setupImage(0, "I1", "A1", 1000L, "K1", null, "D1", 16, 9)
		expectedException.expect<InvalidImageFound>()
		configurationSoneParser.parseImages(createImageBuilderFactory())
	}

	@Test
	fun missingDescriptionIsRecognized() {
		setupTopLevelAlbums()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
		setupImage(0, "I1", "A1", 1000L, "K1", "T1", null, 16, 9)
		expectedException.expect<InvalidImageFound>()
		configurationSoneParser.parseImages(createImageBuilderFactory())
	}

	@Test
	fun missingWidthIsRecognized() {
		setupTopLevelAlbums()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
		setupImage(0, "I1", "A1", 1000L, "K1", "T1", "D1", null, 9)
		expectedException.expect<InvalidImageFound>()
		configurationSoneParser.parseImages(createImageBuilderFactory())
	}

	@Test
	fun missingHeightIsRecognized() {
		setupTopLevelAlbums()
		configurationSoneParser.parseTopLevelAlbums(createAlbumBuilderFactory())
		setupImage(0, "I1", "A1", 1000L, "K1", "T1", "D1", 16, null)
		expectedException.expect<InvalidImageFound>()
		configurationSoneParser.parseImages(createImageBuilderFactory())
	}

}
