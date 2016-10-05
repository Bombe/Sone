package net.pterodactylus.sone.text;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * Unit test for {@link FreenetLinkPart}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreenetLinkPartTest {

	private final FreenetLinkPart part = new FreenetLinkPart("link", "text", "title", true);

	@Test
	public void linkIsRetainedCorrectly() {
		assertThat(part.getLink(), is("link"));
	}

	@Test
	public void textIsRetainedCorrectly() {
		assertThat(part.getText(), is("text"));
	}

	@Test
	public void titleIsRetainedCorrectly() {
		assertThat(part.getTitle(), is("title"));
	}

	@Test
	public void trustedIsRetainedCorrectly() {
		assertThat(part.isTrusted(), is(true));
	}

	@Test
	public void textIsUsedAsTitleIfNoTextIsGiven() {
		assertThat(new FreenetLinkPart("link", "text", true).getTitle(), is("text"));
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForLink() {
		new FreenetLinkPart(null, "text", "title", true);
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForText() {
		new FreenetLinkPart("link", null, "title", true);
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForLinkInSecondaryConstructor() {
		new FreenetLinkPart(null, "text", true);
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForTextInSecondaryConstructor() {
		new FreenetLinkPart("link", null, true);
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForTitle() {
		new FreenetLinkPart("link", "text", null, true);
	}

}
