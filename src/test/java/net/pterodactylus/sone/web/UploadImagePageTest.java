package net.pterodactylus.sone.web;

import static net.pterodactylus.sone.web.WebTestUtils.redirectsTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.core.UpdateChecker;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

import freenet.clients.http.ToadletContext;
import freenet.support.api.HTTPRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit test for {@link UploadImagePageTest}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UploadImagePageTest {

	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	private final Template template = new Template();
	private final WebInterface webInterface = mock(WebInterface.class);
	private final UploadImagePage uploadImagePage = new UploadImagePage(template, webInterface);

	private final TemplateContext templateContext = new TemplateContext();
	private final HTTPRequest httpRequest = mock(HTTPRequest.class);
	private final ToadletContext toadletContext = mock(ToadletContext.class);
	private final Core core = mock(Core.class);
	private final Sone currentSone = mock(Sone.class);
	private final Album parentAlbum = mock(Album.class);

	@Before
	public void setupWebInterface() {
		UpdateChecker updateChecker = mock(UpdateChecker.class);
		when(core.getUpdateChecker()).thenReturn(updateChecker);
		when(webInterface.getCore()).thenReturn(core);
		when(webInterface.getCurrentSone(any(ToadletContext.class))).thenReturn(currentSone);
	}

	@Before
	public void setupParentAlbum() {
		when(core.getAlbum("parent-id")).thenReturn(parentAlbum);
		when(parentAlbum.getSone()).thenReturn(currentSone);
	}

	@Test
	public void uploadingAnImageWithoutTitleRedirectsToEmptyImageTitlePage() throws Exception {
		FreenetRequest request = new FreenetRequest(new URI(""), Method.POST, httpRequest, toadletContext);
		when(httpRequest.getPartAsStringFailsafe(eq("parent"), anyInt())).thenReturn("parent-id");
		when(httpRequest.getPartAsStringFailsafe(eq("title"), anyInt())).thenReturn("  ");
		expectedException.expect(redirectsTo("emptyImageTitle.html"));
		uploadImagePage.processTemplate(request, templateContext);
	}

}
