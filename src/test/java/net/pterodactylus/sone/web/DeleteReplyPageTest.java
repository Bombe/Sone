package net.pterodactylus.sone.web;

import static net.pterodactylus.sone.web.WebTestUtils.redirectsTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

import freenet.support.api.HTTPRequest;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit test for {@link DeleteReplyPage}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DeleteReplyPageTest {

	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	private final Template template = new Template();
	private final WebInterface webInterface = mock(WebInterface.class, RETURNS_DEEP_STUBS);
	private final DeleteReplyPage page = new DeleteReplyPage(template, webInterface);
	private final TemplateContext templateContext = new TemplateContext();
	private final FreenetRequest freenetRequest = mock(FreenetRequest.class);
	private final HTTPRequest httpRequest = mock(HTTPRequest.class);

	@Before
	public void setupWebInterface() {
		when(webInterface.getNotifications(any(Sone.class))).thenReturn(Collections.<Notification>emptyList());
	}

	@Before
	public void setupHttpRequest() {
		when(freenetRequest.getHttpRequest()).thenReturn(httpRequest);
	}

	@Test
	public void tryingToDeleteAReplyWithAnInvalidIdResultsInNoPermissionPage() throws Exception {
		when(freenetRequest.getMethod()).thenReturn(Method.POST);
		when(httpRequest.getPartAsStringFailsafe(eq("reply"), anyInt())).thenReturn("id");
		when(webInterface.getCore().getPostReply("id")).thenReturn(Optional.<PostReply>absent());
		expectedException.expect(redirectsTo("noPermission.html"));
		page.processTemplate(freenetRequest, templateContext);
	}

}
