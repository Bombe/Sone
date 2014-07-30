package net.pterodactylus.sone.core;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import net.pterodactylus.sone.core.Core.MarkPostKnown;
import net.pterodactylus.sone.data.Post;

import org.junit.Test;

/**
 * Unit test for {@link Core} and its subclasses.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CoreTest {

	@Test
	public void markPostKnownMarksPostAsKnown() {
		Core core = mock(Core.class);
		Post post = mock(Post.class);
		MarkPostKnown markPostKnown = core.new MarkPostKnown(post);
		markPostKnown.run();
		verify(core).markPostKnown(eq(post));
	}

}
