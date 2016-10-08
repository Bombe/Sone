package net.pterodactylus.sone.web;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

import freenet.clients.http.ToadletContext;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link NewPage}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class NewPageTest {

	private final Template template = mock(Template.class);
	private final WebInterface webInterface = mock(WebInterface.class, RETURNS_DEEP_STUBS);
	private final NewPage newPage = new NewPage(template, webInterface);
	private final Sone currentSone = mock(Sone.class);
	private final TemplateContext templateContext = new TemplateContext();
	private final FreenetRequest freenetRequest = mock(FreenetRequest.class, RETURNS_DEEP_STUBS);

	@Before
	public void setupFreenetRequest() {
		when(freenetRequest.getToadletContext()).thenReturn(mock(ToadletContext.class));
	}

	@Before
	public void setupWebInterface() {
		when(webInterface.getCore().getPreferences().getPostsPerPage()).thenReturn(5);
		when(webInterface.getCurrentSone(any(ToadletContext.class), anyBoolean())).thenReturn(currentSone);
		when(webInterface.getNotifications(any(Sone.class))).thenReturn(Collections.<Notification>emptyList());
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
