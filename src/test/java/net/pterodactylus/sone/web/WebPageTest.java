package net.pterodactylus.sone.web;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.core.UpdateChecker;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

import freenet.clients.http.ToadletContext;
import freenet.support.api.HTTPRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

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
	protected final Core core = webInterface.getCore();

	protected final Sone currentSone = mock(Sone.class);

	protected final TemplateContext templateContext = new TemplateContext();
	protected final HTTPRequest httpRequest = mock(HTTPRequest.class);
	protected final FreenetRequest freenetRequest = mock(FreenetRequest.class);
	protected final ToadletContext toadletContext = mock(ToadletContext.class);


	@Before
	public final void setupFreenetRequest() {
		when(freenetRequest.getToadletContext()).thenReturn(toadletContext);
		when(freenetRequest.getHttpRequest()).thenReturn(httpRequest);
	}

	@Before
	public final void setupWebInterface() {
		UpdateChecker updateChecker = mock(UpdateChecker.class);
		when(webInterface.getCore().getUpdateChecker()).thenReturn(updateChecker);
		when(webInterface.getCurrentSone(toadletContext)).thenReturn(currentSone);
		when(webInterface.getCurrentSone(eq(toadletContext), anyBoolean())).thenReturn(currentSone);
		when(webInterface.getNotifications(currentSone)).thenReturn(new ArrayList<Notification>());
	}

	protected void request(String uri, Method method) {
		try {
			when(freenetRequest.getUri()).thenReturn(new URI(uri));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		when(freenetRequest.getMethod()).thenReturn(method);
	}

}
