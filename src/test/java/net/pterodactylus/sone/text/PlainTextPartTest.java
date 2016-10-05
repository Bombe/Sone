package net.pterodactylus.sone.text;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * Unit test for {@link PlainTextPart}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PlainTextPartTest {

	private final PlainTextPart part = new PlainTextPart("text");

	@Test
	public void textIsRetainedCorrectly() {
		assertThat(part.getText(), is("text"));
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForText() {
	    new PlainTextPart(null);
	}

}
