/*
 * Sone - MemoryDatabaseTest.kt - Copyright © 2013–2019 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.database.memory

import com.google.common.base.*
import com.google.common.base.Optional.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.test.Matchers.*
import net.pterodactylus.util.config.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.invocation.*
import java.util.Arrays.*
import java.util.UUID.*
import kotlin.test.*

/**
 * Tests for [MemoryDatabase].
 */
class MemoryDatabaseTest {

	private val configuration = deepMock<Configuration>()
	private val memoryDatabase = MemoryDatabase(configuration)
	private val sone = mock<Sone>()

	@BeforeTest
	fun setupSone() {
		whenever(sone.id).thenReturn(SONE_ID)
	}

	@Test
	fun `stored sone is made available`() {
		storeSone()
		assertThat(memoryDatabase.getPost("post1"), isPost("post1", 1000L, "post1", absent()))
		assertThat(memoryDatabase.getPost("post2"), isPost("post2", 2000L, "post2", of(RECIPIENT_ID)))
		assertThat(memoryDatabase.getPost("post3"), nullValue())
		assertThat(memoryDatabase.getPostReply("reply1"), isPostReply("reply1", "post1", 3000L, "reply1"))
		assertThat(memoryDatabase.getPostReply("reply2"), isPostReply("reply2", "post2", 4000L, "reply2"))
		assertThat(memoryDatabase.getPostReply("reply3"), isPostReply("reply3", "post1", 5000L, "reply3"))
		assertThat(memoryDatabase.getPostReply("reply4"), nullValue())
		assertThat(memoryDatabase.getAlbum("album1"), isAlbum("album1", null, "album1", "album-description1"))
		assertThat(memoryDatabase.getAlbum("album2"), isAlbum("album2", null, "album2", "album-description2"))
		assertThat(memoryDatabase.getAlbum("album3"), isAlbum("album3", "album1", "album3", "album-description3"))
		assertThat(memoryDatabase.getAlbum("album4"), nullValue())
		assertThat(memoryDatabase.getImage("image1"), isImage("image1", 1000L, "KSK@image1", "image1", "image-description1", 16, 9))
		assertThat(memoryDatabase.getImage("image2"), isImage("image2", 2000L, "KSK@image2", "image2", "image-description2", 32, 18))
		assertThat(memoryDatabase.getImage("image3"), isImage("image3", 3000L, "KSK@image3", "image3", "image-description3", 48, 27))
		assertThat(memoryDatabase.getImage("image4"), nullValue())
	}

	private fun storeSone() {
		val firstPost = TestPostBuilder().withId("post1")
				.from(SONE_ID)
				.withTime(1000L)
				.withText("post1")
				.build()
		val secondPost = TestPostBuilder().withId("post2")
				.from(SONE_ID)
				.withTime(2000L)
				.withText("post2")
				.to(RECIPIENT_ID)
				.build()
		val posts = asList(firstPost, secondPost)
		whenever(sone.posts).thenReturn(posts)
		val firstPostFirstReply = TestPostReplyBuilder().withId("reply1")
				.from(SONE_ID)
				.to(firstPost.id)
				.withTime(3000L)
				.withText("reply1")
				.build()
		val firstPostSecondReply = TestPostReplyBuilder().withId("reply3")
				.from(RECIPIENT_ID)
				.to(firstPost.id)
				.withTime(5000L)
				.withText("reply3")
				.build()
		val secondPostReply = TestPostReplyBuilder().withId("reply2")
				.from(SONE_ID)
				.to(secondPost.id)
				.withTime(4000L)
				.withText("reply2")
				.build()
		val postReplies = setOf(firstPostFirstReply, firstPostSecondReply, secondPostReply)
		whenever(sone.replies).thenReturn(postReplies)
		val firstAlbum = TestAlbumBuilder().withId("album1")
				.by(sone)
				.build()
				.modify()
				.setTitle("album1")
				.setDescription("album-description1")
				.update()
		val secondAlbum = TestAlbumBuilder().withId("album2")
				.by(sone)
				.build()
				.modify()
				.setTitle("album2")
				.setDescription("album-description2")
				.update()
		val thirdAlbum = TestAlbumBuilder().withId("album3")
				.by(sone)
				.build()
				.modify()
				.setTitle("album3")
				.setDescription("album-description3")
				.update()
		firstAlbum.addAlbum(thirdAlbum)
		val rootAlbum = mock<Album>()
		whenever(rootAlbum.id).thenReturn("root")
		whenever(rootAlbum.albums).thenReturn(listOf(firstAlbum, secondAlbum))
		whenever(sone.rootAlbum).thenReturn(rootAlbum)
		val firstImage = TestImageBuilder().withId("image1")
				.build()
				.modify()
				.setSone(sone)
				.setCreationTime(1000L)
				.setKey("KSK@image1")
				.setTitle("image1")
				.setDescription("image-description1")
				.setWidth(16)
				.setHeight(9)
				.update()
		val secondImage = TestImageBuilder().withId("image2")
				.build()
				.modify()
				.setSone(sone)
				.setCreationTime(2000L)
				.setKey("KSK@image2")
				.setTitle("image2")
				.setDescription("image-description2")
				.setWidth(32)
				.setHeight(18)
				.update()
		val thirdImage = TestImageBuilder().withId("image3")
				.build()
				.modify()
				.setSone(sone)
				.setCreationTime(3000L)
				.setKey("KSK@image3")
				.setTitle("image3")
				.setDescription("image-description3")
				.setWidth(48)
				.setHeight(27)
				.update()
		firstAlbum.addImage(firstImage)
		firstAlbum.addImage(thirdImage)
		secondAlbum.addImage(secondImage)
		memoryDatabase.storeSone(sone)
	}

	@Test
	fun `stored and removed sone is not available`() {
		storeSone()
		memoryDatabase.removeSone(sone)
		assertThat(memoryDatabase.sones, empty())
	}

	@Test
	fun `post recipients are detected correctly`() {
		val postWithRecipient = createPost(of(RECIPIENT_ID))
		memoryDatabase.storePost(postWithRecipient)
		val postWithoutRecipient = createPost(absent())
		memoryDatabase.storePost(postWithoutRecipient)
		assertThat(memoryDatabase.getDirectedPosts(RECIPIENT_ID), contains(postWithRecipient))
	}

	private fun createPost(recipient: Optional<String>): Post {
		val postWithRecipient = mock<Post>()
		whenever(postWithRecipient.id).thenReturn(randomUUID().toString())
		whenever(postWithRecipient.sone).thenReturn(sone)
		whenever(postWithRecipient.recipientId).thenReturn(recipient)
		return postWithRecipient
	}

	@Test
	fun `post replies are managed correctly`() {
		val firstPost = createPost(absent())
		val firstPostFirstReply = createPostReply(firstPost, 1000L)
		val secondPost = createPost(absent())
		val secondPostFirstReply = createPostReply(secondPost, 1000L)
		val secondPostSecondReply = createPostReply(secondPost, 2000L)
		memoryDatabase.storePost(firstPost)
		memoryDatabase.storePost(secondPost)
		memoryDatabase.storePostReply(firstPostFirstReply)
		memoryDatabase.storePostReply(secondPostFirstReply)
		memoryDatabase.storePostReply(secondPostSecondReply)
		assertThat(memoryDatabase.getReplies(firstPost.id), contains(firstPostFirstReply))
		assertThat(memoryDatabase.getReplies(secondPost.id), contains(secondPostFirstReply, secondPostSecondReply))
	}

	private fun createPostReply(post: Post, time: Long): PostReply {
		val postReply = mock<PostReply>()
		whenever(postReply.id).thenReturn(randomUUID().toString())
		whenever(postReply.time).thenReturn(time)
		whenever(postReply.post).thenReturn(of(post))
		val postId = post.id
		whenever(postReply.postId).thenReturn(postId)
		return postReply
	}

	@Test
	fun `test basic album functionality`() {
		val newAlbum = AlbumImpl(mock())
		assertThat(memoryDatabase.getAlbum(newAlbum.id), nullValue())
		memoryDatabase.storeAlbum(newAlbum)
		assertThat(memoryDatabase.getAlbum(newAlbum.id), equalTo<Album>(newAlbum))
		memoryDatabase.removeAlbum(newAlbum)
		assertThat(memoryDatabase.getAlbum(newAlbum.id), nullValue())
	}

	private fun initializeFriends() {
		whenever(configuration.getStringValue("Sone/$SONE_ID/Friends/0/ID")).thenReturn(TestValue.from("Friend1"))
		whenever(configuration.getStringValue("Sone/$SONE_ID/Friends/1/ID")).thenReturn(TestValue.from("Friend2"))
		whenever(configuration.getStringValue("Sone/$SONE_ID/Friends/2/ID")).thenReturn(TestValue.from(null))
	}

	@Test
	fun `friends are returned correctly`() {
		initializeFriends()
		whenever(sone.isLocal).thenReturn(true)
		val friends = memoryDatabase.getFriends(sone)
		assertThat(friends, containsInAnyOrder("Friend1", "Friend2"))
	}

	@Test
	fun `friends are only loaded once from configuration`() {
		initializeFriends()
		whenever(sone.isLocal).thenReturn(true)
		memoryDatabase.getFriends(sone)
		memoryDatabase.getFriends(sone)
		verify(configuration, times(1)).getStringValue("Sone/$SONE_ID/Friends/0/ID")
	}

	@Test
	fun `friends are only returned for local sones`() {
		val friends = memoryDatabase.getFriends(sone)
		assertThat(friends, emptyIterable<Any>())
		verify(configuration, never()).getStringValue("Sone/$SONE_ID/Friends/0/ID")
	}

	@Test
	fun `checking for a friend returns true`() {
		initializeFriends()
		whenever(sone.isLocal).thenReturn(true)
		assertThat(memoryDatabase.isFriend(sone, "Friend1"), equalTo(true))
	}

	@Test
	fun `checking for a friend that is not a friend returns false`() {
		initializeFriends()
		whenever(sone.isLocal).thenReturn(true)
		assertThat(memoryDatabase.isFriend(sone, "FriendX"), equalTo(false))
	}

	@Test
	fun `checking for a friend of remote sone returns false`() {
		initializeFriends()
		assertThat(memoryDatabase.isFriend(sone, "Friend1"), equalTo(false))
	}

	private fun prepareConfigurationValues(): Map<String, Value<*>> =
			mutableMapOf<String, Value<*>>().also { configurationValues ->
				whenever(configuration.getStringValue(anyString())).thenAnswer(createAndCacheValue(configurationValues))
				whenever(configuration.getLongValue(anyString())).thenAnswer(createAndCacheValue(configurationValues))
			}

	private fun createAndCacheValue(configurationValues: MutableMap<String, Value<*>>) =
			{ invocation: InvocationOnMock ->
				configurationValues[invocation[0]]
						?: TestValue.from(null).also {
							configurationValues[invocation[0]] = it
						}
			}

	@Test
	fun `friend is added correctly to local sone`() {
		val configurationValues = prepareConfigurationValues()
		whenever(sone.isLocal).thenReturn(true)
		memoryDatabase.addFriend(sone, "Friend1")
		assertThat(configurationValues["Sone/$SONE_ID/Friends/0/ID"], equalTo<Value<*>>(TestValue.from("Friend1")))
		assertThat(configurationValues["Sone/$SONE_ID/Friends/1/ID"], equalTo<Value<*>>(TestValue.from(null)))
	}

	@Test
	fun `friend is not added to remote sone`() {
		memoryDatabase.addFriend(sone, "Friend1")
		verify(configuration, never()).getStringValue(anyString())
	}

	@Test
	fun `friend is removed correctly from local sone`() {
		val configurationValues = prepareConfigurationValues()
		whenever(sone.isLocal).thenReturn(true)
		memoryDatabase.addFriend(sone, "Friend1")
		memoryDatabase.removeFriend(sone, "Friend1")
		assertThat(configurationValues["Sone/$SONE_ID/Friends/0/ID"], equalTo<Value<*>>(TestValue.from(null)))
		assertThat(configurationValues["Sone/$SONE_ID/Friends/1/ID"], equalTo<Value<*>>(TestValue.from(null)))
	}

	@Test
	fun `configuration is not written when a non-friend is removed`() {
		prepareConfigurationValues()
		whenever(sone.isLocal).thenReturn(true)
		memoryDatabase.removeFriend(sone, "Friend1")
		verify(configuration).getStringValue(anyString())
	}

	@Test
	fun `sone following time is returned correctly`() {
		prepareConfigurationValues()
		configuration.getStringValue("SoneFollowingTimes/0/Sone").value = "sone"
		configuration.getLongValue("SoneFollowingTimes/0/Time").value = 1000L
		assertThat(memoryDatabase.getFollowingTime("sone"), equalTo(1000L))
	}

	@Test
	fun `null is returned when sone is not followed`() {
		prepareConfigurationValues()
		configuration.getStringValue("SoneFollowingTimes/0/Sone").value = "otherSone"
		configuration.getLongValue("SoneFollowingTimes/0/Time").value = 1000L
		assertThat(memoryDatabase.getFollowingTime("sone"), nullValue())
	}

	@Test
	fun `time is stored in configuration when a sone is followed`() {
		prepareConfigurationValues()
		whenever(sone.isLocal).thenReturn(true)
		memoryDatabase.addFriend(sone, "Friend")
		assertThat(configuration.getStringValue("SoneFollowingTimes/0/Sone").value, equalTo("Friend"))
		assertThat(System.currentTimeMillis() - configuration.getLongValue("SoneFollowingTimes/0/Time").value, lessThan(1000L))
		assertThat(configuration.getStringValue("SoneFollowingTimes/1/Sone").value, nullValue())
	}

	@Test
	fun `existing time is not overwritten when a sone is followed`() {
		prepareConfigurationValues()
		configuration.getStringValue("SoneFollowingTimes/0/Sone").value = "Friend"
		configuration.getLongValue("SoneFollowingTimes/0/Time").value = 1000L
		whenever(sone.isLocal).thenReturn(true)
		memoryDatabase.addFriend(sone, "Friend")
		assertThat(configuration.getStringValue("SoneFollowingTimes/0/Sone").value, equalTo("Friend"))
		assertThat(configuration.getLongValue("SoneFollowingTimes/0/Time").value, equalTo(1000L))
		assertThat(configuration.getStringValue("SoneFollowingTimes/1/Sone").value, nullValue())
	}

	@Test
	fun `unfollowing a sone removes the following time`() {
		prepareConfigurationValues()
		configuration.getStringValue("Sone/sone/Friends/0/ID").value = "Friend"
		configuration.getStringValue("SoneFollowingTimes/0/Sone").value = "Friend"
		configuration.getLongValue("SoneFollowingTimes/0/Time").value = 1000L
		whenever(sone.isLocal).thenReturn(true)
		memoryDatabase.removeFriend(sone, "Friend")
		assertThat(configuration.getStringValue("SoneFollowingTimes/0/Sone").value, nullValue())
	}

	@Test
	fun `unfollowing a sone does not remove the following time if another local sone follows it`() {
		prepareConfigurationValues()
		configuration.getStringValue("Sone/sone/Friends/0/ID").value = "Friend"
		configuration.getStringValue("Sone/other-sone/Friends/0/ID").value = "Friend"
		configuration.getStringValue("SoneFollowingTimes/0/Sone").value = "Friend"
		configuration.getLongValue("SoneFollowingTimes/0/Time").value = 1000L
		val otherSone = mock<Sone>()
		whenever(otherSone.isLocal).thenReturn(true)
		whenever(otherSone.id).thenReturn("other-sone")
		memoryDatabase.getFriends(otherSone)
		whenever(sone.isLocal).thenReturn(true)
		memoryDatabase.removeFriend(sone, "Friend")
		assertThat(configuration.getStringValue("SoneFollowingTimes/0/Sone").value, equalTo("Friend"))
		assertThat(configuration.getLongValue("SoneFollowingTimes/0/Time").value, equalTo(1000L))
	}

	@Test
	fun `marking a post as known saves configuration`() {
		prepareConfigurationValues()
		val post = mock<Post>()
		whenever(post.id).thenReturn("post-id")
		memoryDatabase.setPostKnown(post, true)
		assertThat(configuration.getStringValue("KnownPosts/0/ID").value, equalTo("post-id"))
		assertThat(configuration.getStringValue("KnownPosts/1/ID").value, equalTo<Any>(null))
	}

	@Test
	fun `marking a post reply as known saves configuration`() {
		prepareConfigurationValues()
		val postReply = mock<PostReply>()
		whenever(postReply.id).thenReturn("post-reply-id")
		memoryDatabase.setPostReplyKnown(postReply, true)
		assertThat(configuration.getStringValue("KnownReplies/0/ID").value, equalTo("post-reply-id"))
		assertThat(configuration.getStringValue("KnownReplies/1/ID").value, equalTo<Any>(null))
	}

	@Test
	@Dirty("the rate limiter should be mocked")
	fun `saving the database twice in a row only saves it once`() {
		memoryDatabase.save()
		memoryDatabase.save()
		verify(configuration.getStringValue("KnownPosts/0/ID"), times(1)).value = null
	}

	@Test
	@Dirty("the rate limiter should be mocked")
	fun `setting posts as knows twice in a row only saves the database once`() {
		prepareConfigurationValues()
		val post = mock<Post>()
		whenever(post.id).thenReturn("post-id")
		memoryDatabase.setPostKnown(post, true)
		memoryDatabase.setPostKnown(post, true)
		verify(configuration, times(1)).getStringValue("KnownPosts/1/ID")
	}

	@Test
	@Dirty("the rate limiter should be mocked")
	fun `setting post replies as knows twice in a row only saves the database once`() {
		prepareConfigurationValues()
		val postReply = mock<PostReply>()
		whenever(postReply.id).thenReturn("post-reply-id")
		memoryDatabase.setPostReplyKnown(postReply, true)
		memoryDatabase.setPostReplyKnown(postReply, true)
		verify(configuration, times(1)).getStringValue("KnownReplies/1/ID")
	}

}

private const val SONE_ID = "sone"
private const val RECIPIENT_ID = "recipient"
