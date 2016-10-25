package net.pterodactylus.sone.web;

import static net.pterodactylus.sone.web.WebTestUtils.redirectsTo;
import static net.pterodactylus.util.web.Method.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for {@link CreateSonePage}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreateSonePageTest extends WebPageTest {

	private final CreateSonePage page = new CreateSonePage(template, webInterface);
	private final Sone[] localSones = { createSone("local-sone1"), createSone("local-sone2"), createSone("local-sone3") };
	private final OwnIdentity[] ownIdentities = {
			createOwnIdentity("own-id-1", "Sone"),
			createOwnIdentity("own-id-2", "Test", "Foo"),
			createOwnIdentity("own-id-3"),
			createOwnIdentity("own-id-4", "Sone")
	};

	@Test
	public void pageReturnsCorrectPath() {
		assertThat(page.getPath(), is("createSone.html"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getRequestStoresListOfIdentitiesInTemplateContext() throws Exception {
		addDefaultLocalSones();
		addDefaultOwnIdentities();
		page.processTemplate(freenetRequest, templateContext);
		assertThat((Collection<Sone>) templateContext.get("sones"), contains(localSones[0], localSones[1], localSones[2]));
		assertThat((Collection<OwnIdentity>) templateContext.get("identitiesWithoutSone"), contains(ownIdentities[1], ownIdentities[2]));
	}

	private void addDefaultLocalSones() {
		addLocalSone("local-sone3", localSones[2]);
		addLocalSone("local-sone1", localSones[0]);
		addLocalSone("local-sone2", localSones[1]);
	}

	private void addDefaultOwnIdentities() {
		addOwnIdentity(ownIdentities[2]);
		addOwnIdentity(ownIdentities[0]);
		addOwnIdentity(ownIdentities[3]);
		addOwnIdentity(ownIdentities[1]);
	}

	private Sone createSone(String id) {
		Sone sone = mock(Sone.class);
		when(sone.getId()).thenReturn(id);
		when(sone.getProfile()).thenReturn(new Profile(sone));
		return sone;
	}

	private OwnIdentity createOwnIdentity(String id, final String... contexts) {
		OwnIdentity ownIdentity = mock(OwnIdentity.class);
		when(ownIdentity.getId()).thenReturn(id);
		when(ownIdentity.getNickname()).thenReturn(id);
		when(ownIdentity.getContexts()).thenReturn(new HashSet<>(Arrays.asList(contexts)));
		when(ownIdentity.hasContext(anyString())).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				return Arrays.asList(contexts).contains(invocation.<String>getArgument(0));
			}
		});
		return ownIdentity;
	}

	@Test
	public void soneIsCreatedAndLoggedIn() throws Exception {
		addDefaultLocalSones();
		addDefaultOwnIdentities();
		addHttpRequestParameter("identity", "own-id-3");
		request("", POST);
		Sone newSone = mock(Sone.class);
		when(core.createSone(ownIdentities[2])).thenReturn(newSone);
		expectedException.expect(redirectsTo("index.html"));
		try {
			page.processTemplate(freenetRequest, templateContext);
		} finally {
			verify(core).createSone(ownIdentities[2]);
			verify(webInterface).setCurrentSone(toadletContext, newSone);
		}
	}

	@Test
	public void onInvalidIdentityIdFlagIsStoredInTemplateContext() throws Exception {
		addDefaultLocalSones();
		addDefaultOwnIdentities();
		addHttpRequestParameter("identity", "own-id-invalid");
		request("", POST);
		page.processTemplate(freenetRequest, templateContext);
		assertThat(((Boolean) templateContext.get("errorNoIdentity")), is(true));
	}

	@Test
	public void ifSoneIsNotCreatedUserIsStillRedirectedToIndex() throws Exception {
		addDefaultLocalSones();
		addDefaultOwnIdentities();
		addHttpRequestParameter("identity", "own-id-3");
		request("", POST);
		when(core.createSone(ownIdentities[2])).thenReturn(null);
		expectedException.expect(redirectsTo("index.html"));
		try {
			page.processTemplate(freenetRequest, templateContext);
		} finally {
			verify(core).createSone(ownIdentities[2]);
			verify(webInterface).setCurrentSone(toadletContext, null);
		}
	}

	@Test
	public void doNotShowCreateSoneInMenuIfFullAccessRequiredButClientHasNoFullAccess() {
		when(core.getPreferences().isRequireFullAccess()).thenReturn(true);
		when(toadletContext.isAllowedFullAccess()).thenReturn(false);
		assertThat(page.isEnabled(toadletContext), is(false));
	}

	@Test
	public void showCreateSoneInMenuIfNotLoggedInAndClientHasFullAccess() {
		when(core.getPreferences().isRequireFullAccess()).thenReturn(true);
		when(toadletContext.isAllowedFullAccess()).thenReturn(true);
		unsetCurrentSone();
		assertThat(page.isEnabled(toadletContext), is(true));
	}

	@Test
	public void showCreateSoneInMenuIfNotLoggedIn() {
		unsetCurrentSone();
		assertThat(page.isEnabled(toadletContext), is(true));
	}

	@Test
	public void showCreateSoneInMenuIfLoggedInAndASingleSoneExists() {
		addLocalSone("local-sone", mock(Sone.class));
		assertThat(page.isEnabled(toadletContext), is(true));
	}

	@Test
	public void doNotShowCreateSoneInMenuIfLoggedInAndMoreLocalSonesExists() {
		addLocalSone("local-sone1", mock(Sone.class));
		addLocalSone("local-sone2", mock(Sone.class));
		assertThat(page.isEnabled(toadletContext), is(false));
	}

}
