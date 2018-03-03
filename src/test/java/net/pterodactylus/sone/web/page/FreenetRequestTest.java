package net.pterodactylus.sone.web.page;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.net.URISyntaxException;

import net.pterodactylus.util.web.Method;

import freenet.clients.http.ToadletContext;
import freenet.support.api.HTTPRequest;

import org.junit.Test;

/**
 * Unit test for {@link FreenetRequest}.
 */
public class FreenetRequestTest {

	private final URI uri = new URI(".");
	private final Method method = Method.GET;
	private final HTTPRequest httpRequest = mock(HTTPRequest.class);
	private final ToadletContext toadletContext = mock(ToadletContext.class);
	private final FreenetRequest request = new FreenetRequest(uri, method, httpRequest, toadletContext);

	@SuppressWarnings("unused")
	public FreenetRequestTest() throws URISyntaxException {
	}

	@Test
	public void uriIsRetainedCorrectly() {
		assertThat(request.getUri(), is(uri));
	}

	@Test
	public void methodIsRetainedCorrectly() {
		assertThat(request.getMethod(), is(method));
	}

	@Test
	public void httpRequestIsRetainedCorrectly() {
		assertThat(request.getHttpRequest(), is(httpRequest));
	}

	@Test
	public void toadletContextIsRetainedCorrectly() {
		assertThat(request.getToadletContext(), is(toadletContext));
	}

}
