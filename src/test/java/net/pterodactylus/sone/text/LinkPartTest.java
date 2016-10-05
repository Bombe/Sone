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
	public void textIsUsedAsTitleIfNoTitleIsGiven() {
		assertThat(new LinkPart("link", "text").getTitle(), is("text"));
	}

}
