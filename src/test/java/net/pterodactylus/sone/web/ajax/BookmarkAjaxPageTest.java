/*
 * © 2013 xplosion interactive
 */

package net.pterodactylus.sone.web.ajax;

import static com.google.common.base.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;

import freenet.clients.http.HTTPRequestImpl;
import freenet.support.api.HTTPRequest;
import org.junit.Test;

/**
 * Tests for {@link BookmarkAjaxPage}.
 *
 * @author <a href="mailto:d.roden@xplosion.de">David Roden</a>
 */
public class BookmarkAjaxPageTest {

	@Test
	public void testBookmarkingExistingPost() throws URISyntaxException {
		/* create mocks. */
		Core core = mock(Core.class);
		Post post = mock(Post.class);
		when(core.getPost("abc")).thenReturn(of(post));
		WebInterface webInterface = mock(WebInterface.class);
		when(webInterface.getCore()).thenReturn(core);
		HTTPRequest httpRequest = new HTTPRequestImpl(new URI("/ajax/bookmark.ajax?post=abc"), "GET");
		FreenetRequest request = mock(FreenetRequest.class);
		when(request.getHttpRequest()).thenReturn(httpRequest);

		/* create JSON page. */
		BookmarkAjaxPage bookmarkAjaxPage = new BookmarkAjaxPage(webInterface);
		JsonReturnObject jsonReturnObject = bookmarkAjaxPage.createJsonObject(request);

		/* verify response. */
		assertThat(jsonReturnObject, notNullValue());
		assertThat(jsonReturnObject.isSuccess(), is(true));

		/* verify behaviour. */
		verify(core).bookmarkPost(post);
	}

	@Test
	public void testBookmarkingMissingPost() throws URISyntaxException {
		/* create mocks. */
		Core core = mock(Core.class);
		WebInterface webInterface = mock(WebInterface.class);
		when(webInterface.getCore()).thenReturn(core);
		HTTPRequest httpRequest = new HTTPRequestImpl(new URI("/ajax/bookmark.ajax"), "GET");
		FreenetRequest request = mock(FreenetRequest.class);
		when(request.getHttpRequest()).thenReturn(httpRequest);

		/* create JSON page. */
		BookmarkAjaxPage bookmarkAjaxPage = new BookmarkAjaxPage(webInterface);
		JsonReturnObject jsonReturnObject = bookmarkAjaxPage.createJsonObject(request);

		/* verify response. */
		assertThat(jsonReturnObject, notNullValue());
		assertThat(jsonReturnObject.isSuccess(), is(false));
		assertThat(((JsonErrorReturnObject) jsonReturnObject).getError(), is("invalid-post-id"));

		/* verify behaviour. */
		verify(core, never()).bookmarkPost(any(Post.class));
	}

}
