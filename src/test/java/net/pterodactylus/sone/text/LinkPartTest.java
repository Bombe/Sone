package net.pterodactylus.sone.text;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * Unit test for {@link LinkPart}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LinkPartTest {

	private final LinkPart part = new LinkPart("link", "text", "title");

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
	public void linkIsUsedAsTitleIfNoTitleIsGiven() {
		assertThat(new LinkPart("link", "text").getTitle(), is("link"));
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForLink() {
		new LinkPart(null, "text", "title");
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForText() {
		new LinkPart("link", null, "title");
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForLinkInSecondaryConstructor() {
		new LinkPart(null, "text");
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForTextInSecondaryConstructor() {
		new LinkPart("link", null);
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForTitle() {
		new LinkPart("link", "text", null);
	}

}
