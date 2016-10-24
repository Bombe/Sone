package net.pterodactylus.sone.web;

import static net.pterodactylus.sone.web.WebTestUtils.redirectsTo;
import static net.pterodactylus.util.web.Method.GET;
import static net.pterodactylus.util.web.Method.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;

import org.junit.Test;

/**
 * Unit test for {@link CreateReplyPageTest}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreateReplyPageTest extends WebPageTest {

	private final CreateReplyPage page = new CreateReplyPage(template, webInterface);

	@Test
	public void pageReturnsCorrectPath() {
		assertThat(page.getPath(), is("createReply.html"));
	}

	@Test
	public void replyIsCreatedCorrectly() throws Exception {
		request("", POST);
		addHttpRequestParameter("post", "post-id");
		addHttpRequestParameter("text", "some text");
		addHttpRequestParameter("returnPage", "returnPage.html");
		Post post = mock(Post.class);
		addPost("post-id", post);
		expectedException.expect(redirectsTo("returnPage.html"));
		try {
			page.processTemplate(freenetRequest, templateContext);
		} finally {
			verify(core).createReply(currentSone, post, "some text");
		}
	}

	@Test
	public void replyIsCreatedWithCorrectSender() throws Exception {
		request("", POST);
		addHttpRequestParameter("post", "post-id");
		addHttpRequestParameter("text", "some text");
		addHttpRequestParameter("returnPage", "returnPage.html");
		addHttpRequestParameter("sender", "sender-id");
		Sone sender = mock(Sone.class);
		addLocalSone("sender-id", sender);
		Post post = mock(Post.class);
		addPost("post-id", post);
		expectedException.expect(redirectsTo("returnPage.html"));
		try {
			page.processTemplate(freenetRequest, templateContext);
		} finally {
			verify(core).createReply(sender, post, "some text");
		}
	}

	@Test
	public void emptyTextSetsVariableInTemplateContext() throws Exception {
		request("", POST);
		addPost("post-id", mock(Post.class));
		addHttpRequestParameter("post", "post-id");
		addHttpRequestParameter("text", "   ");
		addHttpRequestParameter("returnPage", "returnPage.html");
		page.processTemplate(freenetRequest, templateContext);
		assertThat(templateContext.<Boolean>get("errorTextEmpty", Boolean.class), is(true));
		verifyParametersAreCopied("");
		verify(core, never()).createReply(any(Sone.class), any(Post.class), anyString());
	}

	private void verifyParametersAreCopied(String text) {
		assertThat(templateContext.<String>get("postId", String.class), is("post-id"));
		assertThat(templateContext.<String>get("text", String.class), is(text));
		assertThat(templateContext.<String>get("returnPage", String.class), is("returnPage.html"));
	}

	@Test
	public void userIsRedirectIfPostDoesNotExist() throws Exception {
		request("", POST);
		addHttpRequestParameter("post", "post-id");
		addHttpRequestParameter("text", "some text");
		addHttpRequestParameter("returnPage", "returnPage.html");
		expectedException.expect(redirectsTo("noPermission.html"));
		page.processTemplate(freenetRequest, templateContext);
	}

	@Test
	public void getRequestServesTemplateAndStoresParameters() throws Exception {
		request("", GET);
		addHttpRequestParameter("post", "post-id");
		addHttpRequestParameter("text", "some text");
		addHttpRequestParameter("returnPage", "returnPage.html");
		page.processTemplate(freenetRequest, templateContext);
		verifyParametersAreCopied("some text");
	}

}
