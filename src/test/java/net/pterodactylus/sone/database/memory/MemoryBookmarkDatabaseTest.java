package net.pterodactylus.sone.database.memory;

import static com.google.common.base.Optional.fromNullable;
import static net.pterodactylus.sone.Matchers.isPostWithId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.pterodactylus.sone.data.Post;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for {@link MemoryBookmarkDatabase}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MemoryBookmarkDatabaseTest {

	private final MemoryDatabase memoryDatabase = mock(MemoryDatabase.class);
	private final ConfigurationLoader configurationLoader =
			mock(ConfigurationLoader.class);
	private final MemoryBookmarkDatabase bookmarkDatabase =
			new MemoryBookmarkDatabase(memoryDatabase, configurationLoader);
	private final Map<String, Post> posts = new HashMap<String, Post>();

	@Before
	public void setupMemoryDatabase() {
		when(memoryDatabase.getPost(anyString())).thenAnswer(
				new Answer<Optional<Post>>() {
					@Override
					public Optional<Post> answer(
							InvocationOnMock invocation) {
						return fromNullable(
								posts.get(invocation.getArguments()[0]));
					}
				});
	}

	@Before
	public void setupPosts() {
		createAndRegisterPost("PostId1");
		createAndRegisterPost("PostId2");
	}

	private Post createAndRegisterPost(String postId) {
		Post post = createPost(postId);
		posts.put(postId, post);
		return post;
	}

	private Post createPost(String postId) {
		Post post = mock(Post.class);
		when(post.getId()).thenReturn(postId);
		return post;
	}

	@Test
	public void bookmarkDatabaseRetainsBookmarkedPosts() {
		Set<Post> allPosts = new HashSet<Post>(posts.values());
		for (Post post : allPosts) {
			bookmarkDatabase.bookmarkPost(post);
		}
		assertThat(bookmarkDatabase.getBookmarkedPosts(), is(allPosts));
		for (Post post : allPosts) {
			assertThat(bookmarkDatabase.isPostBookmarked(post), is(true));
		}
	}

	@Test
	public void bookmarkingAPostSavesTheDatabase() {
		for (Post post : posts.values()) {
			bookmarkDatabase.bookmarkPost(post);
		}
		verify(configurationLoader, times(posts.size()))
				.saveBookmarkedPosts(any(Set.class));
	}

	@Test
	public void unbookmarkingAPostSavesTheDatabase() {
		for (Post post : posts.values()) {
			bookmarkDatabase.bookmarkPost(post);
			bookmarkDatabase.unbookmarkPost(post);
		}
		verify(configurationLoader, times(posts.size() * 2))
				.saveBookmarkedPosts(any(Set.class));
	}

	@Test
	public void removingABookmarkRemovesTheCorrectBookmark() {
		Set<Post> allPosts = new HashSet<Post>(posts.values());
		for (Post post : allPosts) {
			bookmarkDatabase.bookmarkPost(post);
		}
		Post randomPost = posts.values().iterator().next();
		bookmarkDatabase.unbookmarkPost(randomPost);
		allPosts.remove(randomPost);
		assertThat(bookmarkDatabase.getBookmarkedPosts(), is(allPosts));
		for (Post post : posts.values()) {
			assertThat(bookmarkDatabase.isPostBookmarked(post),
					is(!post.equals(randomPost)));
		}
	}

	@Test
	public void startingTheDatabaseLoadsBookmarkedPosts() {
		bookmarkDatabase.start();
		verify(configurationLoader).loadBookmarkedPosts();
	}

	@Test
	public void stoppingTheDatabaseSavesTheBookmarkedPosts() {
		bookmarkDatabase.stop();
		verify(configurationLoader).saveBookmarkedPosts(any(Set.class));
	}

	@Test
	public void bookmarkedPostsIncludeNotYetLoadedPosts() {
		bookmarkDatabase.bookmarkPost(posts.get("PostId1"));
		bookmarkDatabase.bookmarkPost(createPost("PostId3"));
		final Set<Post> bookmarkedPosts =
				bookmarkDatabase.getBookmarkedPosts();
		assertThat(bookmarkedPosts,
				contains(isPostWithId("PostId1"), isPostWithId("PostId3")));
	}

}
