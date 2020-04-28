package net.pterodactylus.sone.text;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * JUnit test for {@link TextFilter}.
 */
public class TextFilterTest {

	@Test
	public void textFilterCanBeCreated() {
		new TextFilter();
	}

	@Test
	public void textFilterRemovesHttpLinkToSameHost() {
		String textWithHttpLink = "Some text with an http://foo.bar/link.html in it.";
		assertThat(TextFilter.filter("foo.bar", textWithHttpLink), is("Some text with an link.html in it."));
	}

	@Test
	public void textFilterRemovesHttpsLinkToSameHost() {
		String textWithHttpLink = "Some text with an https://foo.bar/link.html in it.";
		assertThat(TextFilter.filter("foo.bar", textWithHttpLink), is("Some text with an link.html in it."));
	}

	@Test
	public void textWithoutALinkIsReturnedUnmodified() {
		String textWithHttpLink = "Some text without a link in it.";
		assertThat(TextFilter.filter("foo.bar", textWithHttpLink), is("Some text without a link in it."));
	}

	@Test
	public void nothingIsRemovedWhenThereIsNoHostHeader() {
		String textWithHttpLink = "Some text with an https://foo.bar/link.html in it.";
		assertThat(TextFilter.filter(null, textWithHttpLink), is("Some text with an https://foo.bar/link.html in it."));
	}

	@Test
	public void textFilterDoesNotRemoveLinksToDifferentHost() {
		String textWithHttpLink = "Some text with an https://foo.bar/link.html in it.";
		assertThat(TextFilter.filter("bar.baz", textWithHttpLink), is("Some text with an https://foo.bar/link.html in it."));
	}

}
