package net.pterodactylus.sone.test;

import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.PostReplyBuilder;

/**
 * {@link PostReplyBuilder} that returns a mocked {@link PostReply}.
 */
public class TestPostReplyBuilder implements PostReplyBuilder {

	private final PostReply postReply = mock(PostReply.class);

	@Override
	public PostReplyBuilder to(String postId) {
		when(postReply.getPostId()).thenReturn(postId);
		return this;
	}

	@Override
	public PostReply build() throws IllegalStateException {
		return postReply;
	}

	@Override
	public PostReplyBuilder randomId() {
		when(postReply.getId()).thenReturn(randomUUID().toString());
		return this;
	}

	@Override
	public PostReplyBuilder withId(String id) {
		when(postReply.getId()).thenReturn(id);
		return this;
	}

	@Override
	public PostReplyBuilder from(String senderId) {
		Sone sone = mock(Sone.class);
		when(sone.getId()).thenReturn(senderId);
		when(postReply.getSone()).thenReturn(sone);
		return this;
	}

	@Override
	public PostReplyBuilder currentTime() {
		when(postReply.getTime()).thenReturn(currentTimeMillis());
		return this;
	}

	@Override
	public PostReplyBuilder withTime(long time) {
		when(postReply.getTime()).thenReturn(time);
		return this;
	}

	@Override
	public PostReplyBuilder withText(String text) {
		when(postReply.getText()).thenReturn(text);
		return this;
	}

}
