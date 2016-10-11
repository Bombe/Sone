package net.pterodactylus.sone.web;

import static net.pterodactylus.sone.web.WebTestUtils.redirectsTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.util.web.Method;

import org.junit.Test;

/**
 * Unit test for {@link BookmarkPage}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class BookmarkPageTest extends WebPageTest {

	private final BookmarkPage page = new BookmarkPage(template, webInterface);

	@Test
	public void pathIsSetCorrectly() {
		assertThat(page.getPath(), is("bookmark.html"));
	}

	@Test
	public void getRequestDoesNotBookmarkAnythingAndDoesNotRedirect() throws Exception {
		page.processTemplate(freenetRequest, templateContext);
		verify(core, never()).bookmarkPost(any(Post.class));
	}

	@Test
	public void postIsBookmarkedCorrectly() throws Exception {
		setupRequest();
		Post post = mock(Post.class);
		addPost("post-id", post);
		expectedException.expect(redirectsTo("return-page.html"));
		try {
			page.processTemplate(freenetRequest, templateContext);
		} finally {
			verify(core).bookmarkPost(post);
		}
	}

	private void setupRequest() {
		request("", Method.POST);
		addHttpRequestParameter("post", "post-id");
		addHttpRequestParameter("returnPage", "return-page.html");
	}

	@Test
	public void nonExistentPostIsNotBookmarked() throws Exception {
		setupRequest();
		addPost("post-id", null);
		expectedException.expect(redirectsTo("return-page.html"));
		try {
			page.processTemplate(freenetRequest, templateContext);
		} finally {
			verify(core, never()).bookmarkPost(any(Post.class));
		}
	}

}
