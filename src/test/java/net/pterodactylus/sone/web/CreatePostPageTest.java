package net.pterodactylus.sone.web;

import static net.pterodactylus.util.web.Method.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import net.pterodactylus.sone.data.Sone;

import com.google.common.base.Optional;
import org.junit.Test;

/**
 * Unit test for {@link CreatePostPage}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreatePostPageTest extends WebPageTest {

	private final CreatePostPage page = new CreatePostPage(template, webInterface);

	@Test
	public void pageReturnsCorrectPath() {
		assertThat(page.getPath(), is("createPost.html"));
	}

	@Test
	public void returnPageIsSetInTemplateContext() throws Exception {
		addHttpRequestParameter("returnPage", "returnPage.html");
		page.processTemplate(freenetRequest, templateContext);
		assertThat(templateContext.get("returnPage"), is((Object) "returnPage.html"));
	}

	@Test
	public void postIsCreatedCorrectly() throws Exception {
		addHttpRequestParameter("returnPage", "returnPage.html");
		addHttpRequestParameter("text", "post text");
		request("", POST);
		expectedException.expect(WebTestUtils.redirectsTo("returnPage.html"));
		try {
			page.processTemplate(freenetRequest, templateContext);
		} finally {
			verify(core).createPost(currentSone, Optional.<Sone>absent(), "post text");
		}
	}

	@Test
	public void creatingAnEmptyPostIsDenied() throws Exception {
		addHttpRequestParameter("returnPage", "returnPage.html");
		addHttpRequestParameter("text", "   ");
		request("", POST);
		page.processTemplate(freenetRequest, templateContext);
		assertThat(templateContext.get("errorTextEmpty"), is((Object) true));
	}

	@Test
	public void aSenderCanBeSelected() throws Exception {
		addHttpRequestParameter("returnPage", "returnPage.html");
		addHttpRequestParameter("text", "post text");
		addHttpRequestParameter("sender", "sender-id");
		Sone sender = mock(Sone.class);
		addLocalSone("sender-id", sender);
		request("", POST);
		expectedException.expect(WebTestUtils.redirectsTo("returnPage.html"));
		try {
			page.processTemplate(freenetRequest, templateContext);
		} finally {
			verify(core).createPost(sender, Optional.<Sone>absent(), "post text");
		}
	}

	@Test
	public void aRecipientCanBeSelected() throws Exception {
		addHttpRequestParameter("returnPage", "returnPage.html");
		addHttpRequestParameter("text", "post text");
		addHttpRequestParameter("recipient", "recipient-id");
		Sone recipient = mock(Sone.class);
		addSone("recipient-id", recipient);
		request("", POST);
		expectedException.expect(WebTestUtils.redirectsTo("returnPage.html"));
		try {
			page.processTemplate(freenetRequest, templateContext);
		} finally {
			verify(core).createPost(currentSone, Optional.of(recipient), "post text");
		}
	}

}
