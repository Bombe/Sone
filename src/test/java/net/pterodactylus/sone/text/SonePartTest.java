package net.pterodactylus.sone.text;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link SonePart}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SonePartTest {

	private final Sone sone = mock(Sone.class);
	private final SonePart part = new SonePart(sone);

	@Test
	public void soneIsRetainedCorrectly() {
	    assertThat(part.getSone(), is(sone));
	}

	@Test
	public void textIsConstructedFromSonesNiceName() {
	    when(sone.getProfile()).thenReturn(mock(Profile.class));
		when(sone.getName()).thenReturn("sone");
		assertThat(part.getText(), is("sone"));
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForSone() {
	    new SonePart(null);
	}

}
