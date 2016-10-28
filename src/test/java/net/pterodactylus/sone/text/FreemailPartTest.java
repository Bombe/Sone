package net.pterodactylus.sone.text;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * Unit test for {@link FreemailPart}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreemailPartTest {

	private final FreemailPart part = new FreemailPart("local", "freemail-id", "identity-id");

	@Test
	public void freemailPartRetainsEmailLocalPart() {
		assertThat(part.getEmailLocalPart(), is("local"));
	}

	@Test
	public void freemailPartRetainsFreemailId() {
		assertThat(part.getFreemailId(), is("freemail-id"));
	}

	@Test
	public void freemailPartRetainsIdentityId() {
		assertThat(part.getIdentityId(), is("identity-id"));
	}

	@Test
	public void freemailPartReturnsCorrectText() {
		assertThat(part.getText(), is("local@freemail-id.freemail"));
	}

}
