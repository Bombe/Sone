package net.pterodactylus.sone.web;

import static net.pterodactylus.sone.web.WebTestUtils.redirectsTo;
import static net.pterodactylus.util.web.Method.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Album.Modifier;
import net.pterodactylus.sone.data.Album.Modifier.AlbumTitleMustNotBeEmpty;
import net.pterodactylus.sone.test.Dirty;

import org.junit.Test;

/**
 * Unit test for {@link CreateAlbumPage}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreateAlbumPageTest extends WebPageTest {

	private final CreateAlbumPage page = new CreateAlbumPage(template, webInterface);

	@Test
	public void pageReturnsCorrectPath() {
		assertThat(page.getPath(), is("createAlbum.html"));
	}

	@Test
	public void getRequestShowsTemplate() throws Exception {
		page.processTemplate(freenetRequest, templateContext);
	}

	@Test
	public void missingNameResultsInAttributeSetInTemplateContext() throws Exception {
		request("", POST);
		page.processTemplate(freenetRequest, templateContext);
		assertThat(templateContext.get("nameMissing"), is((Object) true));
	}

	@Test
	public void titleAndDescriptionAreSetCorrectlyOnTheAlbum() throws Exception {
		request("", POST);
		Album parentAlbum = createAlbum("parent-id");
		when(core.getAlbum("parent-id")).thenReturn(parentAlbum);
		Album newAlbum = createAlbum("album-id");
		when(core.createAlbum(currentSone, parentAlbum)).thenReturn(newAlbum);
		addHttpRequestParameter("name", "new name");
		addHttpRequestParameter("description", "new description");
		addHttpRequestParameter("parent", "parent-id");
		expectedException.expect(redirectsTo("imageBrowser.html?album=album-id"));
		try {
			page.processTemplate(freenetRequest, templateContext);
		} finally {
			verify(newAlbum).modify();
			verify(newAlbum.modify()).setTitle("new name");
			verify(newAlbum.modify()).setDescription("new description");
			verify(newAlbum.modify()).update();
		}
	}

	private Album createAlbum(String albumId) {
		Album newAlbum = mock(Album.class, RETURNS_DEEP_STUBS);
		when(newAlbum.getId()).thenReturn(albumId);
		Modifier albumModifier = mock(Modifier.class, RETURNS_SELF);
		when(newAlbum.modify()).thenReturn(albumModifier);
		when(albumModifier.update()).thenReturn(newAlbum);
		return newAlbum;
	}

	@Test
	public void rootAlbumIsUsedIfNoParentIsSpecified() throws Exception {
		request("", POST);
		Album parentAlbum = createAlbum("root-id");
		when(currentSone.getRootAlbum()).thenReturn(parentAlbum);
		Album newAlbum = createAlbum("album-id");
		when(core.createAlbum(currentSone, parentAlbum)).thenReturn(newAlbum);
		addHttpRequestParameter("name", "new name");
		addHttpRequestParameter("description", "new description");
		expectedException.expect(redirectsTo("imageBrowser.html?album=album-id"));
		page.processTemplate(freenetRequest, templateContext);
	}

	@Test
	@Dirty("that exception can never happen")
	public void emptyAlbumTitleRedirectsToErrorPage() throws Exception {
		request("", POST);
		Album parentAlbum = createAlbum("root-id");
		when(currentSone.getRootAlbum()).thenReturn(parentAlbum);
		Album newAlbum = createAlbum("album-id");
		when(core.createAlbum(currentSone, parentAlbum)).thenReturn(newAlbum);
		when(newAlbum.modify().update()).thenThrow(AlbumTitleMustNotBeEmpty.class);
		addHttpRequestParameter("name", "new name");
		addHttpRequestParameter("description", "new description");
		expectedException.expect(redirectsTo("emptyAlbumTitle.html"));
		page.processTemplate(freenetRequest, templateContext);
	}

}
