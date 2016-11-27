package net.pterodactylus.sone.web;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.core.Preferences;
import net.pterodactylus.sone.core.UpdateChecker;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.SoneOptions.DefaultSoneOptions;
import net.pterodactylus.sone.data.TemporaryImage;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;
import net.pterodactylus.util.web.Response;

import freenet.clients.http.ToadletContext;
import freenet.support.api.HTTPRequest;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.io.ByteStreams;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
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
	protected final Map<String, String> requestParameters = new HashMap<>();
	protected final Map<String, String> requestHeaders = new HashMap<>();
	protected final FreenetRequest freenetRequest = mock(FreenetRequest.class);
	private final PipedOutputStream responseOutputStream = new PipedOutputStream();
	private final PipedInputStream responseInputStream;
	protected final Response response = new Response(responseOutputStream);
	protected final ToadletContext toadletContext = mock(ToadletContext.class);

	private final Set<OwnIdentity> ownIdentities = new HashSet<>();
	private final Map<String, Sone> sones = new HashMap<>();
	private final List<Sone> localSones = new ArrayList<>();

	protected WebPageTest() {
		try {
			responseInputStream = new PipedInputStream(responseOutputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public final void setupFreenetRequest() {
		when(freenetRequest.getToadletContext()).thenReturn(toadletContext);
		when(freenetRequest.getHttpRequest()).thenReturn(httpRequest);
		when(httpRequest.getPartAsStringFailsafe(anyString(), anyInt())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String parameter = invocation.getArgument(0);
				int maxLength = invocation.getArgument(1);
				return requestParameters.containsKey(parameter) ? requestParameters.get(parameter).substring(0, Math.min(maxLength, requestParameters.get(parameter).length())) : "";
			}
		});
		when(httpRequest.getParam(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String parameter = invocation.getArgument(0);
				return requestParameters.containsKey(parameter) ? requestParameters.get(parameter) : "";
			}
		});
		when(httpRequest.getParam(anyString(), ArgumentMatchers.<String>any())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String parameter = invocation.getArgument(0);
				return requestParameters.containsKey(parameter) ? requestParameters.get(parameter) : invocation.<String>getArgument(1);
			}
		});
		when(httpRequest.isPartSet(anyString())).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				return requestParameters.containsKey(invocation.<String>getArgument(0));
			}
		});
		when(httpRequest.getParts()).thenAnswer(new Answer<String[]>() {
			@Override
			public String[] answer(InvocationOnMock invocation) throws Throwable {
				return requestParameters.keySet().toArray(new String[requestParameters.size()]);
			}
		});
		when(httpRequest.getHeader(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return requestHeaders.get(invocation.<String>getArgument(0).toLowerCase());
			}
		});
	}

	@Before
	public final void setupCore() {
		UpdateChecker updateChecker = mock(UpdateChecker.class);
		when(core.getUpdateChecker()).thenReturn(updateChecker);
		when(core.getPreferences()).thenReturn(new Preferences(eventBus));
		when(core.getLocalSone(anyString())).thenReturn(null);
		when(core.getLocalSones()).thenReturn(localSones);
		when(core.getSone(anyString())).thenReturn(Optional.<Sone>absent());
		when(core.getSones()).thenAnswer(new Answer<Collection<Sone>>() {
			@Override
			public Collection<Sone> answer(InvocationOnMock invocation) throws Throwable {
				return sones.values();
			}
		});
		when(core.getSone(anyString())).thenAnswer(new Answer<Optional<Sone>>() {
			@Override
			public Optional<Sone> answer(InvocationOnMock invocation) throws Throwable {
				return Optional.fromNullable(sones.get(invocation.getArgument(0)));
			}
		});
		when(core.getPost(anyString())).thenReturn(Optional.<Post>absent());
		when(core.getAlbum(anyString())).thenReturn(null);
		when(core.getImage(anyString())).thenReturn(null);
		when(core.getImage(anyString(), anyBoolean())).thenReturn(null);
		when(core.getTemporaryImage(anyString())).thenReturn(null);
	}

	@Before
	public final void setupIdentityManager() {
		when(core.getIdentityManager().getAllOwnIdentities()).thenReturn(ownIdentities);
	}

	@Before
	public final void setupWebInterface() {
		when(webInterface.getCurrentSone(toadletContext)).thenReturn(currentSone);
		when(webInterface.getCurrentSone(eq(toadletContext), anyBoolean())).thenReturn(currentSone);
		when(webInterface.getNotification(anyString())).thenReturn(Optional.<Notification>absent());
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

	protected void addHttpRequestHeader(@Nonnull String name, String value) {
		requestHeaders.put(name.toLowerCase(), value);
	}

	protected void addHttpRequestParameter(String name, final String value) {
		requestParameters.put(name, value);
	}

	protected void addPost(String postId, Post post) {
		when(core.getPost(postId)).thenReturn(Optional.fromNullable(post));
	}

	protected void addSone(String soneId, Sone sone) {
		sones.put(soneId, sone);
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

	protected void addImage(String imageId, Image image) {
		when(core.getImage(eq(imageId))).thenReturn(image);
		when(core.getImage(eq(imageId), anyBoolean())).thenReturn(image);
	}

	protected void addTemporaryImage(String imageId, TemporaryImage temporaryImage) {
		when(core.getTemporaryImage(eq(imageId))).thenReturn(temporaryImage);
	}

	protected byte[] getResponseBytes() throws IOException {
		response.getContent().close();
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			ByteStreams.copy(responseInputStream, outputStream);
			return outputStream.toByteArray();
		}
	}

	protected void addNotification(String notificationId, Notification notification) {
		when(webInterface.getNotification(eq(notificationId))).thenReturn(Optional.of(notification));
	}

}
