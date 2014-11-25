/*
 * Sone - MemoryDatabaseTest.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sone.database.memory;

import static com.google.common.base.Optional.of;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static net.pterodactylus.sone.Matchers.isAlbum;
import static net.pterodactylus.sone.Matchers.isImage;
import static net.pterodactylus.sone.Matchers.isPost;
import static net.pterodactylus.sone.Matchers.isPostReply;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.pterodactylus.sone.TestAlbumBuilder;
import net.pterodactylus.sone.TestImageBuilder;
import net.pterodactylus.sone.TestPostBuilder;
import net.pterodactylus.sone.TestPostReplyBuilder;
import net.pterodactylus.sone.TestValue;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.impl.AlbumImpl;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.Value;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link MemoryDatabase}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MemoryDatabaseTest {

	private static final String SONE_ID = "sone";
	private static final String RECIPIENT_ID = "recipient";
	private final Configuration configuration = mock(Configuration.class);
	private final MemoryDatabase memoryDatabase = new MemoryDatabase(null, configuration);
	private final Sone sone = mock(Sone.class);

	@Before
	public void setupSone() {
		when(sone.getId()).thenReturn(SONE_ID);
	}

	@Test
	public void storedSoneIsMadeAvailable() {
		Post firstPost = new TestPostBuilder().withId("post1")
				.from(SONE_ID)
				.withTime(1000L)
				.withText("post1")
				.build();
		Post secondPost = new TestPostBuilder().withId("post2")
				.from(SONE_ID)
				.withTime(2000L)
				.withText("post2")
				.to(RECIPIENT_ID)
				.build();
		List<Post> posts = asList(firstPost, secondPost);
		when(sone.getPosts()).thenReturn(posts);
		PostReply firstPostFirstReply =
				new TestPostReplyBuilder().withId("reply1")
						.from(SONE_ID)
						.to(firstPost.getId())
						.withTime(3000L)
						.withText("reply1")
						.build();
		PostReply firstPostSecondReply =
				new TestPostReplyBuilder().withId("reply3")
						.from(RECIPIENT_ID)
						.to(firstPost.getId())
						.withTime(5000L)
						.withText("reply3")
						.build();
		PostReply secondPostReply =
				new TestPostReplyBuilder().withId("reply2")
						.from(SONE_ID)
						.to(secondPost.getId())
						.withTime(4000L)
						.withText("reply2")
						.build();
		Set<PostReply> postReplies = new HashSet<PostReply>(
				asList(firstPostFirstReply, firstPostSecondReply,
						secondPostReply));
		when(sone.getReplies()).thenReturn(postReplies);
		Album firstAlbum = new TestAlbumBuilder().withId("album1")
				.by(sone)
				.build()
				.modify()
				.setTitle("album1")
				.setDescription("album-description1")
				.update();
		Album secondAlbum = new TestAlbumBuilder().withId("album2").by(
				sone).build().modify().setTitle("album2").setDescription(
				"album-description2").setAlbumImage("image1").update();
		Album thirdAlbum = new TestAlbumBuilder().withId("album3").by(
				sone).build().modify().setTitle("album3").setDescription(
				"album-description3").update();
		firstAlbum.addAlbum(thirdAlbum);
		Album rootAlbum = mock(Album.class);
		when(rootAlbum.getAlbums()).thenReturn(
				asList(firstAlbum, secondAlbum));
		when(sone.getRootAlbum()).thenReturn(rootAlbum);
		Image firstImage = new TestImageBuilder().withId("image1")
				.build()
				.modify()
				.setSone(sone)
				.setCreationTime(1000L)
				.setKey("KSK@image1")
				.setTitle("image1")
				.setDescription("image-description1")
				.setWidth(16)
				.setHeight(9)
				.update();
		Image secondImage = new TestImageBuilder().withId("image2")
				.build()
				.modify()
				.setSone(sone)
				.setCreationTime(2000L)
				.setKey("KSK@image2")
				.setTitle("image2")
				.setDescription("image-description2")
				.setWidth(32)
				.setHeight(18)
				.update();
		Image thirdImage = new TestImageBuilder().withId("image3")
				.build()
				.modify()
				.setSone(sone)
				.setCreationTime(3000L)
				.setKey("KSK@image3")
				.setTitle("image3")
				.setDescription("image-description3")
				.setWidth(48)
				.setHeight(27)
				.update();
		firstAlbum.addImage(firstImage);
		firstAlbum.addImage(thirdImage);
		secondAlbum.addImage(secondImage);
		memoryDatabase.storeSone(sone);
		assertThat(memoryDatabase.getPost("post1").get(),
				isPost(firstPost.getId(), 1000L, "post1",
						Optional.<String>absent()));
		assertThat(memoryDatabase.getPost("post2").get(),
				isPost(secondPost.getId(), 2000L, "post2", of(RECIPIENT_ID)));
		assertThat(memoryDatabase.getPost("post3").isPresent(), is(false));
		assertThat(memoryDatabase.getPostReply("reply1").get(),
				isPostReply("reply1", "post1", 3000L, "reply1"));
		assertThat(memoryDatabase.getPostReply("reply2").get(),
				isPostReply("reply2", "post2", 4000L, "reply2"));
		assertThat(memoryDatabase.getPostReply("reply3").get(),
				isPostReply("reply3", "post1", 5000L, "reply3"));
		assertThat(memoryDatabase.getPostReply("reply4").isPresent(),
				is(false));
		assertThat(memoryDatabase.getAlbum("album1").get(),
				isAlbum("album1", null, "album1", "album-description1",
						null));
		assertThat(memoryDatabase.getAlbum("album2").get(),
				isAlbum("album2", null, "album2", "album-description2",
						"image1"));
		assertThat(memoryDatabase.getAlbum("album3").get(),
				isAlbum("album3", "album1", "album3", "album-description3",
						null));
		assertThat(memoryDatabase.getAlbum("album4").isPresent(), is(false));
		assertThat(memoryDatabase.getImage("image1").get(),
				isImage("image1", 1000L, "KSK@image1", "image1",
						"image-description1", 16, 9));
		assertThat(memoryDatabase.getImage("image2").get(),
				isImage("image2", 2000L, "KSK@image2", "image2",
						"image-description2", 32, 18));
		assertThat(memoryDatabase.getImage("image3").get(),
				isImage("image3", 3000L, "KSK@image3", "image3",
						"image-description3", 48, 27));
		assertThat(memoryDatabase.getImage("image4").isPresent(), is(false));
	}

	@Test
	public void storedAndRemovedSoneIsNotAvailable() {
	    storedSoneIsMadeAvailable();
		memoryDatabase.removeSone(sone);
		assertThat(memoryDatabase.getSones(), empty());
	}

	@Test
	public void postRecipientsAreDetectedCorrectly() {
		Post postWithRecipient = createPost(of(RECIPIENT_ID));
		memoryDatabase.storePost(postWithRecipient);
		Post postWithoutRecipient = createPost(Optional.<String>absent());
		memoryDatabase.storePost(postWithoutRecipient);
		assertThat(memoryDatabase.getDirectedPosts(RECIPIENT_ID),
				contains(postWithRecipient));
	}

	private Post createPost(Optional<String> recipient) {
		Post postWithRecipient = mock(Post.class);
		when(postWithRecipient.getId()).thenReturn(randomUUID().toString());
		when(postWithRecipient.getSone()).thenReturn(sone);
		when(postWithRecipient.getRecipientId()).thenReturn(recipient);
		return postWithRecipient;
	}

	@Test
	public void postRepliesAreManagedCorrectly() {
		Post firstPost = createPost(Optional.<String>absent());
		PostReply firstPostFirstReply = createPostReply(firstPost, 1000L);
		Post secondPost = createPost(Optional.<String>absent());
		PostReply secondPostFirstReply = createPostReply(secondPost, 1000L);
		PostReply secondPostSecondReply = createPostReply(secondPost, 2000L);
		memoryDatabase.storePost(firstPost);
		memoryDatabase.storePost(secondPost);
		memoryDatabase.storePostReply(firstPostFirstReply);
		memoryDatabase.storePostReply(secondPostFirstReply);
		memoryDatabase.storePostReply(secondPostSecondReply);
		assertThat(memoryDatabase.getReplies(firstPost.getId()),
				contains(firstPostFirstReply));
		assertThat(memoryDatabase.getReplies(secondPost.getId()),
				contains(secondPostFirstReply, secondPostSecondReply));
	}

	private PostReply createPostReply(Post post, long time) {
		PostReply postReply = mock(PostReply.class);
		when(postReply.getId()).thenReturn(randomUUID().toString());
		when(postReply.getTime()).thenReturn(time);
		when(postReply.getPost()).thenReturn(of(post));
		final String postId = post.getId();
		when(postReply.getPostId()).thenReturn(postId);
		return postReply;
	}

	@Test
	public void testBasicAlbumFunctionality() {
		Album newAlbum = new AlbumImpl(mock(Sone.class));
		assertThat(memoryDatabase.getAlbum(newAlbum.getId()), is(Optional.<Album>absent()));
		memoryDatabase.storeAlbum(newAlbum);
		assertThat(memoryDatabase.getAlbum(newAlbum.getId()), is(of(newAlbum)));
		memoryDatabase.removeAlbum(newAlbum);
		assertThat(memoryDatabase.getAlbum(newAlbum.getId()), is(Optional.<Album>absent()));
	}

	private void initializeFriends() {
		when(configuration.getStringValue("Sone/" + SONE_ID + "/Friends/0/ID")).thenReturn(
				TestValue.from("Friend1"));
		when(configuration.getStringValue("Sone/" + SONE_ID + "/Friends/1/ID")).thenReturn(
				TestValue.from("Friend2"));
		when(configuration.getStringValue("Sone/" + SONE_ID + "/Friends/2/ID")).thenReturn(
				TestValue.<String>from(null));
	}

	@Test
	public void friendsAreReturnedCorrectly() {
		initializeFriends();
		when(sone.isLocal()).thenReturn(true);
		Collection<String> friends = memoryDatabase.getFriends(sone);
		assertThat(friends, containsInAnyOrder("Friend1", "Friend2"));
	}

	@Test
	public void friendsAreOnlyLoadedOnceFromConfiguration() {
		friendsAreReturnedCorrectly();
		memoryDatabase.getFriends(sone);
		verify(configuration).getStringValue("Sone/" + SONE_ID + "/Friends/0/ID");
	}

	@Test
	public void friendsAreOnlyReturnedForLocalSones() {
		Collection<String> friends = memoryDatabase.getFriends(sone);
		assertThat(friends, emptyIterable());
		verify(configuration, never()).getStringValue("Sone/" + SONE_ID + "/Friends/0/ID");
	}

	@Test
	public void checkingForAFriendReturnsTrue() {
		initializeFriends();
		when(sone.isLocal()).thenReturn(true);
		assertThat(memoryDatabase.isFriend(sone, "Friend1"), is(true));
	}

	@Test
	public void checkingForAFriendThatIsNotAFriendReturnsFalse() {
		initializeFriends();
		when(sone.isLocal()).thenReturn(true);
		assertThat(memoryDatabase.isFriend(sone, "FriendX"), is(false));
	}

	@Test
	public void checkingForAFriendOfRemoteSoneReturnsFalse() {
		initializeFriends();
		assertThat(memoryDatabase.isFriend(sone, "Friend1"), is(false));
	}

	private Map<String, Value<String>> prepareConfigurationValues() {
		final Map<String, Value<String>> configurationValues = new HashMap<String, Value<String>>();
		when(configuration.getStringValue(anyString())).thenAnswer(new Answer<Value<String>>() {
			@Override
			public Value<String> answer(InvocationOnMock invocation) throws Throwable {
				Value<String> stringValue = TestValue.from(null);
				configurationValues.put((String) invocation.getArguments()[0], stringValue);
				return stringValue;
			}
		});
		return configurationValues;
	}

	@Test
	public void friendIsAddedCorrectlyToLocalSone() {
		Map<String, Value<String>> configurationValues = prepareConfigurationValues();
		when(sone.isLocal()).thenReturn(true);
		memoryDatabase.addFriend(sone, "Friend1");
		assertThat(configurationValues.get("Sone/" + SONE_ID + "/Friends/0/ID"),
				is(TestValue.from("Friend1")));
		assertThat(configurationValues.get("Sone/" + SONE_ID + "/Friends/1/ID"),
				is(TestValue.<String>from(null)));
	}

	@Test
	public void friendIsNotAddedToRemoteSone() {
		memoryDatabase.addFriend(sone, "Friend1");
		verify(configuration, never()).getStringValue(anyString());
	}

	@Test
	public void configurationIsWrittenOnceIfFriendIsAddedTwice() {
		prepareConfigurationValues();
		when(sone.isLocal()).thenReturn(true);
		memoryDatabase.addFriend(sone, "Friend1");
		memoryDatabase.addFriend(sone, "Friend1");
		verify(configuration, times(2)).getStringValue(anyString());
	}

	@Test
	public void friendIsRemovedCorrectlyFromLocalSone() {
		Map<String, Value<String>> configurationValues = prepareConfigurationValues();
		when(sone.isLocal()).thenReturn(true);
		memoryDatabase.addFriend(sone, "Friend1");
		memoryDatabase.removeFriend(sone, "Friend1");
		assertThat(configurationValues.get("Sone/" + SONE_ID + "/Friends/0/ID"),
				is(TestValue.<String>from(null)));
		assertThat(configurationValues.get("Sone/" + SONE_ID + "/Friends/1/ID"),
				is(TestValue.<String>from(null)));
	}

	@Test
	public void configurationIsNotWrittenWhenANonFriendIsRemoved() {
		when(sone.isLocal()).thenReturn(true);
		memoryDatabase.removeFriend(sone, "Friend1");
		verify(configuration, never()).getStringValue(anyString());
	}

}
