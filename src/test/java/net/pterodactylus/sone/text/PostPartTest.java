package net.pterodactylus.sone.text;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.Post;

import org.junit.Test;

/**
 * Unit test for {@link PostPart}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostPartTest {

	private final Post post = mock(Post.class);
	private final PostPart part = new PostPart(post);

	@Test
	public void postIsRetainedCorrectly() {
		assertThat(part.getPost(), is(post));
	}

	@Test
	public void textIsTakenFromPost() {
		when(post.getText()).thenReturn("text");
		assertThat(part.getText(), is("text"));
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNotAllowedForPost() {
	    new PostPart(null);
	}

}
