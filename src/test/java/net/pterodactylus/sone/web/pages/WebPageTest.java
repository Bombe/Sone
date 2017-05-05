package net.pterodactylus.sone.web.pages;

import static net.pterodactylus.sone.test.GuiceKt.supply;
import static net.pterodactylus.sone.web.WebTestUtils.redirectsTo;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.naming.SizeLimitExceededException;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.core.Preferences;
import net.pterodactylus.sone.core.UpdateChecker;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.SoneOptions.DefaultSoneOptions;
import net.pterodactylus.sone.data.TemporaryImage;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.sone.web.page.FreenetTemplatePage.RedirectException;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;
import net.pterodactylus.util.web.Response;

import freenet.clients.http.ToadletContext;
import freenet.l10n.BaseL10n;
import freenet.support.SimpleReadOnlyArrayBucket;
import freenet.support.api.Bucket;
import freenet.support.api.HTTPRequest;
import freenet.support.api.HTTPUploadedFile;
import freenet.support.io.NullBucket;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
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
	protected final BaseL10n l10n = webInterface.getL10n();

	protected final Sone currentSone = mock(Sone.class);

	protected final TemplateContext templateContext = new TemplateContext();
	protected final HTTPRequest httpRequest = mock(HTTPRequest.class);
	protected final Multimap<String, String> requestParameters = ArrayListMultimap.create();
	protected final Map<String, String> requestParts = new HashMap<>();
	protected final Map<String, String> requestHeaders = new HashMap<>();
	private final Map<String, String> uploadedFilesNames = new HashMap<>();
	private final Map<String, String> uploadedFilesContentTypes = new HashMap<>();
	private final Map<String, String> uploadedFilesSources = new HashMap<>();
	protected final FreenetRequest freenetRequest = mock(FreenetRequest.class);
	private final PipedOutputStream responseOutputStream = new PipedOutputStream();
	private final PipedInputStream responseInputStream;
	protected final Response response = new Response(responseOutputStream);
	protected final ToadletContext toadletContext = mock(ToadletContext.class);

	private final Set<OwnIdentity> ownIdentities = new HashSet<>();
	private final Map<String, Sone> sones = new HashMap<>();
	protected final List<Sone> localSones = new ArrayList<>();
	private final ListMultimap<String, PostReply> postReplies = ArrayListMultimap.create();

	protected final Injector injector = Guice.createInjector(
			supply(WebInterface.class).byInstance(webInterface),
			supply(Template.class).byInstance(template)
	);

	protected WebPageTest() {
		try {
			responseInputStream = new PipedInputStream(responseOutputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public final void setupFreenetRequest() throws SizeLimitExceededException {
		when(freenetRequest.getToadletContext()).thenReturn(toadletContext);
		when(freenetRequest.getHttpRequest()).thenReturn(httpRequest);
		when(httpRequest.getMultipleParam(anyString())).thenAnswer(new Answer<String[]>() {
			@Override
			public String[] answer(InvocationOnMock invocation) throws Throwable {
				return requestParameters.get(invocation.<String>getArgument(0)).toArray(new String[0]);
			}
		});
		when(httpRequest.isPartSet(anyString())).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				return requestParts.get(invocation.<String>getArgument(0)) != null;
			}
		});
		when(httpRequest.getParts()).thenAnswer(new Answer<String[]>() {
			@Override
			public String[] answer(InvocationOnMock invocation) throws Throwable {
				return requestParts.keySet().toArray(new String[requestParts.size()]);
			}
		});
		when(httpRequest.getPartAsStringFailsafe(anyString(), anyInt())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String parameter = invocation.getArgument(0);
				int maxLength = invocation.getArgument(1);
				String value = requestParts.get(parameter);
				return requestParts.containsKey(parameter) ? value.substring(0, Math.min(maxLength, value.length())) : "";
			}
		});
		when(httpRequest.getPartAsStringThrowing(anyString(), anyInt())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String partName = invocation.getArgument(0);
				if (!requestParts.containsKey(partName)) throw new NoSuchElementException();
				String partValue = requestParts.get(partName);
				if (partValue.length() > invocation.<Integer>getArgument(1)) throw new SizeLimitExceededException();
				return partValue;
			}
		});
		when(httpRequest.hasParameters()).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				return !requestParameters.isEmpty();
			}
		});
		when(httpRequest.getParameterNames()).thenAnswer(new Answer<Collection<String>>() {
			@Override
			public Collection<String> answer(InvocationOnMock invocation) throws Throwable {
				return requestParameters.keySet();
			}
		});
		when(httpRequest.getParam(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String parameter = invocation.getArgument(0);
				return requestParameters.containsKey(parameter) ? requestParameters.get(parameter).iterator().next() : "";
			}
		});
		when(httpRequest.getParam(anyString(), ArgumentMatchers.<String>any())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String parameter = invocation.getArgument(0);
				return requestParameters.containsKey(parameter) ? requestParameters.get(parameter).iterator().next() : invocation.<String>getArgument(1);
			}
		});
		when(httpRequest.isParameterSet(anyString())).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				return requestParameters.containsKey(invocation.<String>getArgument(0)) &&
						requestParameters.get(invocation.<String>getArgument(0)).iterator().next() != null;
			}
		});
		when(httpRequest.getHeader(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return requestHeaders.get(invocation.<String>getArgument(0).toLowerCase());
			}
		});
		when(httpRequest.getUploadedFile(anyString())).thenAnswer(new Answer<HTTPUploadedFile>() {
			@Override
			public HTTPUploadedFile answer(InvocationOnMock invocation) throws Throwable {
				final String name = invocation.getArgument(0);
				if (!uploadedFilesSources.containsKey(name)) {
					return null;
				}
				return new HTTPUploadedFile() {
					@Override
					public String getContentType() {
						return uploadedFilesContentTypes.get(name);
					}

					@Override
					public Bucket getData() {
						try (InputStream inputStream = getClass().getResourceAsStream(uploadedFilesSources.get(name))) {
							byte[] bytes = ByteStreams.toByteArray(inputStream);
							return new SimpleReadOnlyArrayBucket(bytes, 0, bytes.length);
						} catch (IOException ioe1) {
							return new NullBucket();
						}
					}

					@Override
					public String getFilename() {
						return uploadedFilesNames.get(name);
					}
				};
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
		when(core.getPostReply(anyString())).thenReturn(Optional.<PostReply>absent());
		when(core.getReplies(anyString())).thenAnswer(new Answer<List<PostReply>>() {
			@Override
			public List<PostReply> answer(InvocationOnMock invocation) throws Throwable {
				return postReplies.get(invocation.<String>getArgument(0));
			}
		});
		when(core.getAlbum(anyString())).thenReturn(null);
		when(core.getImage(anyString())).thenReturn(null);
		when(core.getImage(anyString(), anyBoolean())).thenReturn(null);
		when(core.getTemporaryImage(anyString())).thenReturn(null);
	}

	@Before
	public void setupL10n() {
		when(l10n.getString(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArgument(0);
			}
		});
	}

	@Before
	public final void setupIdentityManager() {
		when(core.getIdentityManager().getAllOwnIdentities()).thenReturn(ownIdentities);
	}

	@Before
	public final void setupWebInterface() {
		when(webInterface.getCurrentSone(eq(toadletContext), anyBoolean())).thenReturn(currentSone);
		when(webInterface.getCurrentSoneCreatingSession(toadletContext)).thenReturn(currentSone);
		when(webInterface.getCurrentSoneWithoutCreatingSession(toadletContext)).thenReturn(currentSone);
		when(webInterface.getNotification(anyString())).thenReturn(Optional.<Notification>absent());
		when(webInterface.getNotifications(Mockito.<Sone>any())).thenReturn(new ArrayList<Notification>());
	}

	@Before
	public void setupSone() {
		when(currentSone.getOptions()).thenReturn(new DefaultSoneOptions());
	}

	protected SoneTemplatePage getPage() {
		return null;
	}

	protected void unsetCurrentSone() {
		when(webInterface.getCurrentSone(eq(toadletContext), anyBoolean())).thenReturn(null);
		when(webInterface.getCurrentSoneCreatingSession(toadletContext)).thenReturn(null);
		when(webInterface.getCurrentSoneWithoutCreatingSession(toadletContext)).thenReturn(null);
	}

	protected void request(String uri, Method method) {
		try {
			when(httpRequest.getPath()).thenReturn(uri);
			when(freenetRequest.getUri()).thenReturn(new URI(uri));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		when(freenetRequest.getMethod()).thenReturn(method);
		when(httpRequest.getMethod()).thenReturn(method.name());
	}

	protected void addHttpRequestHeader(@Nonnull String name, String value) {
		requestHeaders.put(name.toLowerCase(), value);
	}

	protected void addHttpRequestParameter(String name, final String value) {
		requestParameters.put(name, value);
	}

	protected void addHttpRequestPart(String name, String value) {
		requestParts.put(name, value);
	}

	protected void addPost(String postId, Post post) {
		when(core.getPost(postId)).thenReturn(Optional.fromNullable(post));
	}

	protected void addPostReply(String postReplyId, PostReply postReply) {
		if (postReply.getPostId() != null) {
			postReplies.put(postReply.getPostId(), postReply);
		}
		when(core.getPostReply(postReplyId)).thenReturn(Optional.fromNullable(postReply));
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

	protected void addUploadedFile(@Nonnull String name, @Nonnull String filename, @Nonnull String contentType, @Nonnull String resource) {
		uploadedFilesNames.put(name, filename);
		uploadedFilesContentTypes.put(name, contentType);
		uploadedFilesSources.put(name, resource);
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

	protected void verifyRedirect(String target) throws RedirectException {
		expectedException.expect(redirectsTo(target));
		getPage().handleRequest(freenetRequest, templateContext);
	}

	protected void verifyRedirect(String target, Runnable verification) throws RedirectException {
		expectedException.expect(redirectsTo(target));
		try {
			getPage().handleRequest(freenetRequest, templateContext);
			fail();
		} finally {
			verification.run();
		}
	}

}
