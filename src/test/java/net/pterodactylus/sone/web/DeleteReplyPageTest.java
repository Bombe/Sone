package net.pterodactylus.sone.web;

import static net.pterodactylus.sone.web.WebTestUtils.redirectsTo;
import static net.pterodactylus.util.web.Method.POST;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.PostReply;

import com.google.common.base.Optional;
import org.junit.Test;

/**
 * Unit test for {@link DeleteReplyPage}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DeleteReplyPageTest extends WebPageTest {

	private final DeleteReplyPage page = new DeleteReplyPage(template, webInterface);

	@Test
	public void tryingToDeleteAReplyWithAnInvalidIdResultsInNoPermissionPage() throws Exception {
		request("", POST);
		when(httpRequest.getPartAsStringFailsafe(eq("reply"), anyInt())).thenReturn("id");
		when(webInterface.getCore().getPostReply("id")).thenReturn(Optional.<PostReply>absent());
		expectedException.expect(redirectsTo("noPermission.html"));
		page.processTemplate(freenetRequest, templateContext);
	}

}
