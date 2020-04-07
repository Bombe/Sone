package net.pterodactylus.sone.main;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

import freenet.clients.http.ToadletContext;
import freenet.support.api.HTTPRequest;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test for {@link DebugLoaders}.
 */
public class DebugLoadersTest {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private final StringWriter stringWriter = new StringWriter();
	private final TemplateContext templateContext = new TemplateContext();
	private Loaders loaders;

	@Before
	public void setupLoader() throws IOException {
		String templatePath = temporaryFolder.newFolder("temps").getPath();
		loaders = new DebugLoaders(templatePath);
		File templateFile = new File(templatePath, "template.txt");
		Files.write("<%if foo>foo<%else>bar<%/if>", templateFile, Charsets.UTF_8);
		new File(templatePath, "templates").mkdir();
		File secondTemplateFile = new File(templatePath, "templates/template.txt");
		Files.write("<%if foo>foo<%else>bar<%/if>", secondTemplateFile, Charsets.UTF_8);
	}

	@Test
	public void debugLoaderCanLoadTemplatesFromFilesystem() throws IOException {
		Template template = loaders.loadTemplate("/template.txt");
		template.render(templateContext, stringWriter);
		assertThat(stringWriter.toString(), is("bar"));
	}

	@Test
	public void staticPageIsServedFromFilesystem() throws URISyntaxException, IOException {
		Page<FreenetRequest> page = loaders.loadStaticPage("text/", "", "text/plain");
		URI uri = new URI("http://some.host/text/template.txt");
		Method method = Method.GET;
		HTTPRequest httpRequest = mock(HTTPRequest.class);
		ToadletContext toadletContext = mock(ToadletContext.class);
		FreenetRequest request = new FreenetRequest(uri, method, httpRequest, toadletContext);
		OutputStream outputStream = new ByteArrayOutputStream();
		Response response = new Response(outputStream);
		page.handleRequest(request, response);
		assertThat(response.getContentType(), startsWith("text/plain"));
		assertThat(response.getStatusCode(), is(200));
	}

	@Test
	public void templateProviderLocatesTemplatesInFileSystem() {
		TemplateProvider templateProvider = loaders.getTemplateProvider();
		Template template = templateProvider.getTemplate(templateContext, "template.txt");
		template.render(templateContext, stringWriter);
		assertThat(stringWriter.toString(), is("bar"));
	}

}
