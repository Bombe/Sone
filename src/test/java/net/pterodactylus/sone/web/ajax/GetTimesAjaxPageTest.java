package net.pterodactylus.sone.web.ajax;

import static java.lang.System.currentTimeMillis;
import static net.pterodactylus.sone.web.ajax.GetTimesAjaxPage.getTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.ajax.GetTimesAjaxPage.Time;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link GetTimesAjaxPage}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetTimesAjaxPageTest {

	private final WebInterface webInterface = Mockito.mock(WebInterface.class, RETURNS_DEEP_STUBS);

	@Test
	public void timestampInTheFutureIsTranslatedCorrectly() {
		when(webInterface.getL10n().getString("View.Time.InTheFuture")).thenReturn("in the future");
		Time time = getTime(webInterface, currentTimeMillis() + 100);
		assertThat(time.getText(), is("in the future"));
	}

	@Test
	public void timestampAFewSecondsAgoIsTranslatedCorrectly() {
		when(webInterface.getL10n().getString("View.Time.AFewSecondsAgo")).thenReturn("a few seconds ago");
		Time time = getTime(webInterface, currentTimeMillis() - 1000);
		assertThat(time.getText(), is("a few seconds ago"));
	}

}
