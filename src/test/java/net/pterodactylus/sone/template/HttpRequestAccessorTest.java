package net.pterodactylus.sone.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.pterodactylus.util.template.TemplateContext;

import freenet.support.api.HTTPRequest;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link HttpRequestAccessor}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class HttpRequestAccessorTest {

	private static final String REQUEST_PATH = "/the/real/path";
	private static final String USER_AGENT = "Test/1.0";
	private static final String HEADER_PATH = "/some/path";
	private final HttpRequestAccessor accessor = new HttpRequestAccessor();
	private final TemplateContext context = mock(TemplateContext.class);
	private final HTTPRequest httpRequest = mock(HTTPRequest.class);

	@Before
	public void setupHttpRequest() {
		when(httpRequest.getPath()).thenReturn(REQUEST_PATH);
		when(httpRequest.getHeader("User-Agent")).thenReturn(USER_AGENT);
		when(httpRequest.getHeader("Path")).thenReturn(HEADER_PATH);
	}

	@Test
	public void preferCallingMethodsInsteadOfReturningHeaders() {
		assertThat(accessor.get(context, httpRequest, "path"),
				Matchers.<Object>is(REQUEST_PATH));
		verify(httpRequest, never()).getHeader("Path");
	}

	@Test
	public void headerIsReturnedCorrectly() {
		assertThat(accessor.get(context, httpRequest, "User-Agent"),
				Matchers.<Object>is(USER_AGENT));
	}

}
