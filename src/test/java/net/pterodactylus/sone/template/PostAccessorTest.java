package net.pterodactylus.sone.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.template.TemplateContext;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link PostAccessor}.
 */
public class PostAccessorTest {

	private final Core core = mock(Core.class);
	private final PostAccessor accessor = new PostAccessor(core);
	private final Post post = mock(Post.class);

	private final long now = System.currentTimeMillis();

	@Before
	public void setupPost() {
		when(post.getId()).thenReturn("post-id");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void accessorReturnsTheCorrectReplies() {
		List<PostReply> replies = new ArrayList<>();
		replies.add(createPostReply(2000));
		replies.add(createPostReply(-1000));
		replies.add(createPostReply(-2000));
		replies.add(createPostReply(-3000));
		replies.add(createPostReply(-4000));
		when(core.getReplies("post-id")).thenReturn(replies);
		Collection<PostReply> repliesForPost = (Collection<PostReply>) accessor.get(null, post, "replies");
		assertThat(repliesForPost, contains(
				replies.get(1),
				replies.get(2),
				replies.get(3),
				replies.get(4)
		));
	}

	private PostReply createPostReply(long timeOffset) {
		PostReply postReply = mock(PostReply.class);
		when(postReply.getTime()).thenReturn(now + timeOffset);
		return postReply;
	}

	@Test
	@SuppressWarnings("unchecked")
	public void accessorReturnsTheLikingSones() {
		Set<Sone> sones = mock(Set.class);
		when(core.getLikes(post)).thenReturn(sones);
		Set<Sone> likingSones = (Set<Sone>) accessor.get(null, post, "likes");
		assertThat(likingSones, is(sones));
	}

	@Test
	public void accessorReturnsWhetherTheCurrentSoneLikedAPost() {
		Sone sone = mock(Sone.class);
		when(sone.isLikedPostId("post-id")).thenReturn(true);
		TemplateContext templateContext = new TemplateContext();
		templateContext.set("currentSone", sone);
		assertThat(accessor.get(templateContext, post, "liked"), is((Object) true));
	}

	@Test
	public void accessorReturnsFalseIfPostIsNotLiked() {
		Sone sone = mock(Sone.class);
		TemplateContext templateContext = new TemplateContext();
		templateContext.set("currentSone", sone);
		assertThat(accessor.get(templateContext, post, "liked"), is((Object) false));
	}

	@Test
	public void accessorReturnsFalseIfThereIsNoCurrentSone() {
		TemplateContext templateContext = new TemplateContext();
		assertThat(accessor.get(templateContext, post, "liked"), is((Object) false));
	}

	@Test
	public void accessorReturnsThatNotKnownPostIsNew() {
		assertThat(accessor.get(null, post, "new"), is((Object) true));
	}

	@Test
	public void accessorReturnsThatKnownPostIsNotNew() {
		when(post.isKnown()).thenReturn(true);
		assertThat(accessor.get(null, post, "new"), is((Object) false));
	}

	@Test
	public void accessorReturnsIfPostIsBookmarked() {
		when(core.isBookmarked(post)).thenReturn(true);
		assertThat(accessor.get(null, post, "bookmarked"), is((Object) true));
	}

	@Test
	public void accessorReturnsOtherProperties() {
		assertThat(accessor.get(null, post, "hashCode"), is((Object) post.hashCode()));
	}

}
