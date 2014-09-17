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
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.AlbumImpl;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link MemoryDatabase}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MemoryDatabaseTest {

	private static final String SONE_ID = "sone";
	private static final String RECIPIENT_ID = "recipient";
	private final MemoryDatabase memoryDatabase = new MemoryDatabase(null, null);
	private final Sone sone = mock(Sone.class);

	@Before
	public void setupSone() {
		when(sone.getId()).thenReturn(SONE_ID);
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

}
