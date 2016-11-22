package net.pterodactylus.sone.web;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.core.Preferences;
import net.pterodactylus.sone.core.UpdateChecker;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.SoneOptions.DefaultSoneOptions;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

import freenet.clients.http.ToadletContext;
import freenet.support.api.HTTPRequest;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Base class for web page tests.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class WebPageTest {

	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	protected final Template template = new Template();
	protected final WebInterface webInterface = mock(WebInterface.class, RETURNS_DEEP_STUBS);
	protected final EventBus eventBus = mock(EventBus.class);
	protected final Core core = webInterface.getCore();

	protected final Sone currentSone = mock(Sone.class);

	protected final TemplateContext templateContext = new TemplateContext();
	protected final HTTPRequest httpRequest = mock(HTTPRequest.class);
	protected final FreenetRequest freenetRequest = mock(FreenetRequest.class);
	protected final ToadletContext toadletContext = mock(ToadletContext.class);

	private final Set<OwnIdentity> ownIdentities = new HashSet<>();
	private final List<Sone> localSones = new ArrayList<>();

	@Before
	public final void setupFreenetRequest() {
		when(freenetRequest.getToadletContext()).thenReturn(toadletContext);
		when(freenetRequest.getHttpRequest()).thenReturn(httpRequest);
		when(httpRequest.getPartAsStringFailsafe(anyString(), anyInt())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return "";
			}
		});
		when(httpRequest.getParam(anyString())).thenReturn("");
		when(httpRequest.getParam(anyString(), anyString())).thenReturn("");
	}

	@Before
	public final void setupCore() {
		UpdateChecker updateChecker = mock(UpdateChecker.class);
		when(core.getUpdateChecker()).thenReturn(updateChecker);
		when(core.getPreferences()).thenReturn(new Preferences(eventBus));
		when(core.getLocalSone(anyString())).thenReturn(null);
		when(core.getLocalSones()).thenReturn(localSones);
		when(core.getSone(anyString())).thenReturn(Optional.<Sone>absent());
		when(core.getPost(anyString())).thenReturn(Optional.<Post>absent());
		when(core.getAlbum(anyString())).thenReturn(null);
	}

	@Before
	public final void setupIdentityManager() {
		when(core.getIdentityManager().getAllOwnIdentities()).thenReturn(ownIdentities);
	}

	@Before
	public final void setupWebInterface() {
		when(webInterface.getCurrentSone(toadletContext)).thenReturn(currentSone);
		when(webInterface.getCurrentSone(eq(toadletContext), anyBoolean())).thenReturn(currentSone);
		when(webInterface.getNotifications(currentSone)).thenReturn(new ArrayList<Notification>());
	}

	@Before
	public void setupSone() {
		when(currentSone.getOptions()).thenReturn(new DefaultSoneOptions());
	}

	protected void unsetCurrentSone() {
		when(webInterface.getCurrentSone(toadletContext)).thenReturn(null);
		when(webInterface.getCurrentSone(eq(toadletContext), anyBoolean())).thenReturn(null);
	}

	protected void request(String uri, Method method) {
		try {
			when(freenetRequest.getUri()).thenReturn(new URI(uri));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		when(freenetRequest.getMethod()).thenReturn(method);
	}

	protected void addHttpRequestParameter(String name, final String value) {
		when(httpRequest.getPartAsStringFailsafe(eq(name), anyInt())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				int maxLength = invocation.getArgument(1);
				return value.substring(0, Math.min(maxLength, value.length()));
			}
		});
		when(httpRequest.getParam(eq(name))).thenReturn(value);
		when(httpRequest.getParam(eq(name), anyString())).thenReturn(value);
		when(httpRequest.isPartSet(eq(name))).thenReturn(value != null && !value.isEmpty());
	}

	protected void addPost(String postId, Post post) {
		when(core.getPost(postId)).thenReturn(Optional.fromNullable(post));
	}

	protected void addSone(String soneId, Sone sone) {
		when(core.getSone(eq(soneId))).thenReturn(Optional.fromNullable(sone));
	}

	protected void addLocalSone(String soneId, Sone sone) {
		when(core.getLocalSone(eq(soneId))).thenReturn(sone);
		localSones.add(sone);
	}

	protected void addOwnIdentity(OwnIdentity ownIdentity) {
		ownIdentities.add(ownIdentity);
	}

	protected void addAlbum(String albumId, Album album) {
		when(core.getAlbum(eq(albumId))).thenReturn(album);
	}

}
