package net.pterodactylus.sone.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.web.page.FreenetTemplatePage.RedirectException;
import net.pterodactylus.util.collection.Pagination;

import org.junit.Test;

/**
 * Unit test for {@link BookmarksPage}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class BookmarksPageTest extends WebPageTest {

	private final BookmarksPage page = new BookmarksPage(template, webInterface);

	@Test
	public void pageReturnsCorrectPath() {
		assertThat(page.getPath(), is("bookmarks.html"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void pageSetsCorrectPostsInTemplateContext() throws RedirectException {
		Post post1 = createPost(true, 3000L);
		Post post2 = createPost(true, 1000L);
		Post post3 = createPost(true, 2000L);
		Set<Post> bookmarkedPosts = createBookmarkedPosts(post1, post2, post3);
		when(core.getBookmarkedPosts()).thenReturn(bookmarkedPosts);
		when(core.getPreferences().getPostsPerPage()).thenReturn(5);
		page.processTemplate(freenetRequest, templateContext);
		assertThat((Collection<Post>) templateContext.get("posts"), contains(post1, post3, post2));
		assertThat(((Pagination<Post>) templateContext.get("pagination")).getItems(), contains(post1, post3, post2));
		assertThat(((Boolean) templateContext.get("postsNotLoaded")), is(false));
	}

	private Set<Post> createBookmarkedPosts(Post post1, Post post2, Post post3) {
		Set<Post> bookmarkedPosts = new HashSet<>();
		bookmarkedPosts.add(post1);
		bookmarkedPosts.add(post2);
		bookmarkedPosts.add(post3);
		return bookmarkedPosts;
	}

	@Test
	@SuppressWarnings("unchecked")
	public void notLoadedPostsAreNotIncludedButAFlagIsSet() throws RedirectException {
		Post post1 = createPost(true, 1000L);
		Post post2 = createPost(true, 3000L);
		Post post3 = createPost(false, 2000L);
		Set<Post> bookmarkedPosts = createBookmarkedPosts(post1, post2, post3);
		when(core.getBookmarkedPosts()).thenReturn(bookmarkedPosts);
		when(core.getPreferences().getPostsPerPage()).thenReturn(5);
		page.processTemplate(freenetRequest, templateContext);
		assertThat((Collection<Post>) templateContext.get("posts"), contains(post2, post1));
		assertThat(((Pagination<Post>) templateContext.get("pagination")).getItems(), contains(post2, post1));
		assertThat(((Boolean) templateContext.get("postsNotLoaded")), is(true));
	}

	private Post createPost(boolean postLoaded, long time) {
		Post post = mock(Post.class);
		when(post.isLoaded()).thenReturn(postLoaded);
		when(post.getTime()).thenReturn(time);
		return post;
	}

}
