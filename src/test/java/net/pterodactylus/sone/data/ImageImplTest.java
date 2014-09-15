package net.pterodactylus.sone.data;

import net.pterodactylus.sone.data.ImageImpl.ImageTitleMustNotBeEmpty;

import org.junit.Test;

/**
 * Unit test for {@link ImageImpl}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ImageImplTest {

	private final Image image = new ImageImpl();

	@Test(expected = ImageTitleMustNotBeEmpty.class)
	public void modifierDoesNotAllowTitleDoBeEmpty() {
		image.modify().setTitle("").update();
	}

}
