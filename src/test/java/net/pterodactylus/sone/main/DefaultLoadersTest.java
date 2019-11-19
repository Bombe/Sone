package net.pterodactylus.sone.main;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateProvider;
import net.pterodactylus.util.web.Method;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.Response;

import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContext;
import freenet.support.api.HTTPRequest;

import org.junit.Test;

/**
 * Unit test for {@link DefaultLoaders}.
 */
public class DefaultLoadersTest {

	private final Loaders loaders = new DefaultLoaders();
	private final StringWriter stringWriter = new StringWriter();
	private final TemplateContext templateContext = new TemplateContext();

	@Test
	public void templateCanBeLoadedFromTheClasspath() {
		Template template = loaders.loadTemplate("/net/pterodactylus/sone/main/template.txt");
		template.render(templateContext, stringWriter);
		assertThat(stringWriter.toString(), is("Template. bar\n"));
	}

	@Test
	public void staticPageIsServedFromClasspath() throws IOException, URISyntaxException {
		Page<FreenetRequest> staticPage = loaders.loadStaticPage("text/", "/net/pterodactylus/sone/main/", "text/plain");
		URI uri = new URI("http://some.host/text/template.txt");
		Method method = Method.GET;
		HTTPRequest httpRequest = mock(HTTPRequest.class);
		ToadletContext toadletContext = mock(ToadletContext.class);
		SessionManager sessionManager = mock(SessionManager.class);
		FreenetRequest request = new FreenetRequest(uri, method, httpRequest, toadletContext, sessionManager);
		OutputStream outputStream = new ByteArrayOutputStream();
		Response response = new Response(outputStream);
		staticPage.handleRequest(request, response);
		assertThat(response.getContentType(), startsWith("text/plain"));
		assertThat(response.getStatusCode(), is(200));
	}

	@Test
	public void templateIsLocatedInClasspath() {
		TemplateProvider templateProvider = loaders.getTemplateProvider();
		Template template = templateProvider.getTemplate(templateContext, "about.html");
		assertThat(template, notNullValue());
	}

}
