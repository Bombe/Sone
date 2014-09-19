package net.pterodactylus.sone.core;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import net.pterodactylus.sone.core.SoneChangeDetector.PostProcessor;
import net.pterodactylus.sone.core.SoneChangeDetector.PostReplyProcessor;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link SoneChangeDetector}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneChangeDetectorTest {

	private final Sone oldSone = mock(Sone.class);
	private final Sone newSone = mock(Sone.class);
	private final SoneChangeDetector soneChangeDetector =
			new SoneChangeDetector(oldSone);
	private final Post oldPost = mock(Post.class);
	private final Post removedPost = mock(Post.class);
	private final Post newPost = mock(Post.class);
	private final PostProcessor newPostProcessor = mock(PostProcessor.class);
	private final PostProcessor removedPostProcessor =
			mock(PostProcessor.class);
	private final PostReply oldPostReply = mock(PostReply.class);
	private final PostReply removedPostReply = mock(PostReply.class);
	private final PostReply newPostReply = mock(PostReply.class);
	private final PostReplyProcessor newPostReplyProcessor =
			mock(PostReplyProcessor.class);
	private final PostReplyProcessor removedPostReplyProcessor =
			mock(PostReplyProcessor.class);

	@Before
	public void setupPosts() {
		when(oldSone.getPosts()).thenReturn(asList(oldPost, removedPost));
		when(newSone.getPosts()).thenReturn(asList(oldPost, newPost));
	}

	@Before
	public void setupPostProcessors() {
		soneChangeDetector.onNewPosts(newPostProcessor);
		soneChangeDetector.onRemovedPosts(removedPostProcessor);
	}

	@Before
	public void setupPostReplies() {
		when(oldSone.getReplies()).thenReturn(
				new HashSet<PostReply>(
						asList(oldPostReply, removedPostReply)));
		when(newSone.getReplies()).thenReturn(
				new HashSet<PostReply>(asList(oldPostReply, newPostReply)));
	}

	@Before
	public void setupPostReplyProcessors() {
		soneChangeDetector.onNewPostReplies(newPostReplyProcessor);
		soneChangeDetector.onRemovedPostReplies(removedPostReplyProcessor);
	}

	@Test
	public void changeDetectorDetectsChanges() {
		soneChangeDetector.detectChanges(newSone);

		verify(newPostProcessor).processPost(newPost);
		verify(newPostProcessor, never()).processPost(oldPost);
		verify(newPostProcessor, never()).processPost(removedPost);
		verify(removedPostProcessor).processPost(removedPost);
		verify(removedPostProcessor, never()).processPost(oldPost);
		verify(removedPostProcessor, never()).processPost(newPost);

		verify(newPostReplyProcessor).processPostReply(newPostReply);
		verify(newPostReplyProcessor, never()).processPostReply(oldPostReply);
		verify(newPostReplyProcessor, never()).processPostReply(
				removedPostReply);
		verify(removedPostReplyProcessor).processPostReply(removedPostReply);
		verify(removedPostReplyProcessor, never()).processPostReply(
				oldPostReply);
		verify(removedPostReplyProcessor, never()).processPostReply(
				newPostReply);
	}

	@Test
	public void changeDetectorDoesNotNotifyAnyProcessorIfProcessorsUnset() {
	    soneChangeDetector.onNewPosts(null);
	    soneChangeDetector.onRemovedPosts(null);
	    soneChangeDetector.onNewPostReplies(null);
	    soneChangeDetector.onRemovedPostReplies(null);
		soneChangeDetector.detectChanges(newSone);
		verify(newPostProcessor, never()).processPost(any(Post.class));
		verify(removedPostProcessor, never()).processPost(any(Post.class));
		verify(newPostReplyProcessor, never()).processPostReply(any(PostReply.class));
		verify(removedPostReplyProcessor, never()).processPostReply(any(PostReply.class));
	}

}
