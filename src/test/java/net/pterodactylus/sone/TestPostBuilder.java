package net.pterodactylus.sone;

import static com.google.common.base.Optional.fromNullable;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.PostBuilder;

/**
 * {@link PostBuilder} implementation that returns a mocked {@link Post}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TestPostBuilder implements PostBuilder {

	private final Post post = mock(Post.class);
	private String recipientId = null;

	@Override
	public PostBuilder copyPost(Post post) throws NullPointerException {
		return this;
	}

	@Override
	public PostBuilder from(String senderId) {
		final Sone sone = mock(Sone.class);
		when(sone.getId()).thenReturn(senderId);
		when(post.getSone()).thenReturn(sone);
		return this;
	}

	@Override
	public PostBuilder randomId() {
		when(post.getId()).thenReturn(randomUUID().toString());
		return this;
	}

	@Override
	public PostBuilder withId(String id) {
		when(post.getId()).thenReturn(id);
		return this;
	}

	@Override
	public PostBuilder currentTime() {
		when(post.getTime()).thenReturn(currentTimeMillis());
		return this;
	}

	@Override
	public PostBuilder withTime(long time) {
		when(post.getTime()).thenReturn(time);
		return this;
	}

	@Override
	public PostBuilder withText(String text) {
		when(post.getText()).thenReturn(text);
		return this;
	}

	@Override
	public PostBuilder to(String recipientId) {
		this.recipientId = recipientId;
		return this;
	}

	@Override
	public Post build() throws IllegalStateException {
		when(post.getRecipientId()).thenReturn(fromNullable(recipientId));
		return post;
	}

}
