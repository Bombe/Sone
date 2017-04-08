package net.pterodactylus.sone.web.pages;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link NewPage}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class NewPageTest extends WebPageTest {

	private final NewPage newPage = new NewPage(template, webInterface);

	@Before
	public void setupNumberOfPostsPerPage() {
		webInterface.getCore().getPreferences().setPostsPerPage(5);
	}

	@Test
	public void postsAreNotDuplicatedWhenTheyComeFromBothNewPostsAndNewRepliesNotifications() throws Exception {
		// given
		Post extraPost = mock(Post.class);
		List<Post> posts = asList(mock(Post.class), mock(Post.class));
		List<PostReply> postReplies = asList(mock(PostReply.class), mock(PostReply.class));
		when(postReplies.get(0).getPost()).thenReturn(Optional.of(posts.get(0)));
		when(postReplies.get(1).getPost()).thenReturn(Optional.of(extraPost));
		when(webInterface.getNewPosts(currentSone)).thenReturn(posts);
		when(webInterface.getNewReplies(currentSone)).thenReturn(postReplies);

		// when
		newPage.processTemplate(freenetRequest, templateContext);

		// then
		List<Post> renderedPosts = templateContext.get("posts", List.class);
		assertThat(renderedPosts, containsInAnyOrder(posts.get(0), posts.get(1), extraPost));
	}

}
