package net.pterodactylus.sone.core;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.FluentIterable.from;

import java.util.Collection;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

/**
 * Compares the contents of two {@link Sone}s and fires events for new and
 * removed elements.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneChangeDetector {

	private final Sone oldSone;
	private Optional<PostProcessor> newPostProcessor = absent();
	private Optional<PostProcessor> removedPostProcessor = absent();
	private Optional<PostReplyProcessor> newPostReplyProcessor = absent();
	private Optional<PostReplyProcessor> removedPostReplyProcessor = absent();

	public SoneChangeDetector(Sone oldSone) {
		this.oldSone = oldSone;
	}

	public void onNewPosts(PostProcessor newPostProcessor) {
		this.newPostProcessor = fromNullable(newPostProcessor);
	}

	public void onRemovedPosts(PostProcessor removedPostProcessor) {
		this.removedPostProcessor = fromNullable(removedPostProcessor);
	}

	public void onNewPostReplies(PostReplyProcessor newPostReplyProcessor) {
		this.newPostReplyProcessor = fromNullable(newPostReplyProcessor);
	}

	public void onRemovedPostReplies(
			PostReplyProcessor removedPostReplyProcessor) {
		this.removedPostReplyProcessor = fromNullable(removedPostReplyProcessor);
	}

	public void detectChanges(Sone newSone) {
		processPosts(from(newSone.getPosts()).filter(
				notContainedIn(oldSone.getPosts())), newPostProcessor);
		processPosts(from(oldSone.getPosts()).filter(
				notContainedIn(newSone.getPosts())), removedPostProcessor);
		processPostReplies(from(newSone.getReplies()).filter(
				notContainedIn(oldSone.getReplies())), newPostReplyProcessor);
		processPostReplies(from(oldSone.getReplies()).filter(
				notContainedIn(newSone.getReplies())), removedPostReplyProcessor);
	}

	private void processPostReplies(FluentIterable<PostReply> postReplies,
			Optional<PostReplyProcessor> postReplyProcessor) {
		for (PostReply postReply : postReplies) {
			notifyPostReplyProcessor(postReplyProcessor, postReply);
		}
	}

	private void notifyPostReplyProcessor(
			Optional<PostReplyProcessor> postReplyProcessor,
			PostReply postReply) {
		if (postReplyProcessor.isPresent()) {
			postReplyProcessor.get()
					.processPostReply(postReply);
		}
	}

	private void processPosts(FluentIterable<Post> posts,
			Optional<PostProcessor> newPostProcessor) {
		for (Post post : posts) {
			notifyPostProcessor(newPostProcessor, post);
		}
	}

	private void notifyPostProcessor(Optional<PostProcessor> postProcessor,
			Post newPost) {
		if (postProcessor.isPresent()) {
			postProcessor.get().processPost(newPost);
		}
	}

	private <T> Predicate<T> notContainedIn(final Collection<T> posts) {
		return new Predicate<T>() {
			@Override
			public boolean apply(T element) {
				return !posts.contains(element);
			}
		};
	}

	public interface PostProcessor {

		void processPost(Post post);

	}

	public interface PostReplyProcessor {

		void processPostReply(PostReply postReply);

	}

}
