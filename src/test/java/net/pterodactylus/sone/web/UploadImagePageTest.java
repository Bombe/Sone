package net.pterodactylus.sone.web;

import static net.pterodactylus.sone.web.WebTestUtils.redirectsTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.util.web.Method;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link UploadImagePageTest}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UploadImagePageTest extends WebPageTest {

	private final UploadImagePage uploadImagePage = new UploadImagePage(template, webInterface);

	private final Album parentAlbum = mock(Album.class);

	@Before
	public void setupParentAlbum() {
		when(core.getAlbum("parent-id")).thenReturn(parentAlbum);
		when(parentAlbum.getSone()).thenReturn(currentSone);
	}

	@Test
	public void uploadingAnImageWithoutTitleRedirectsToEmptyImageTitlePage() throws Exception {
		request("", Method.POST);
		when(httpRequest.getPartAsStringFailsafe(eq("parent"), anyInt())).thenReturn("parent-id");
		when(httpRequest.getPartAsStringFailsafe(eq("title"), anyInt())).thenReturn("  ");
		expectedException.expect(redirectsTo("emptyImageTitle.html"));
		uploadImagePage.processTemplate(freenetRequest, templateContext);
	}

}
