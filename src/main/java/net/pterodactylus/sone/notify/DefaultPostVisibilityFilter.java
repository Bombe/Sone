package net.pterodactylus.sone.notify;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.freenet.wot.Trust;
import net.pterodactylus.util.notify.Notification;

/**
 * Filters {@link Notification}s involving {@link Post}s.
 */
@Singleton
public class DefaultPostVisibilityFilter implements PostVisibilityFilter {

	/**
	 * Checks whether a post is visible to the given Sone. A post is not
	 * considered visible if one of the following statements is true:
	 * <ul>
	 * <li>The post does not have a Sone.</li>
	 * <li>The post’s {@link Post#getTime() time} is in the future.</li>
	 * </ul>
	 * <p>
	 * If {@code post} is not {@code null} more checks are performed, and the
	 * post will be invisible if:
	 * </p>
	 * <ul>
	 * <li>The Sone of the post is not the given Sone, the given Sone does not
	 * follow the post’s Sone, and the given Sone is not the recipient of the
	 * post.</li>
	 * <li>The trust relationship between the two Sones can not be retrieved.</li>
	 * <li>The given Sone has explicitely assigned negative trust to the post’s
	 * Sone.</li>
	 * <li>The given Sone has not explicitely assigned negative trust to the
	 * post’s Sone but the implicit trust is negative.</li>
	 * </ul>
	 * If none of these statements is true the post is considered visible.
	 *
	 * @param sone
	 * 		The Sone that checks for a post’s visibility (may be
	 * 		{@code null} to skip Sone-specific checks, such as trust)
	 * @param post
	 * 		The post to check for visibility
	 * @return {@code true} if the post is considered visible, {@code false}
	 * otherwise
	 */
	@Override
	public boolean isPostVisible(@Nullable Sone sone, @Nonnull Post post) {
		checkNotNull(post, "post must not be null");
		if (!post.isLoaded()) {
			return false;
		}
		Sone postSone = post.getSone();
		if (sone != null) {
			Trust trust = postSone.getIdentity().getTrust((OwnIdentity) sone.getIdentity());
			if (trust != null) {
				if ((trust.getExplicit() != null) && (trust.getExplicit() < 0)) {
					return false;
				}
				if ((trust.getExplicit() == null) && (trust.getImplicit() != null) && (trust.getImplicit() < 0)) {
					return false;
				}
			} else {
				/*
				 * a null trust means that the trust updater has not yet
				 * received a trust value for this relation. if we return false,
				 * the post feed will stay empty until the trust updater has
				 * received trust values. to prevent this we simply assume that
				 * posts are visible if there is no trust.
				 */
			}
			if ((!postSone.equals(sone)) && !sone.hasFriend(postSone.getId()) && !sone.getId().equals(post.getRecipientId().orNull())) {
				return false;
			}
		}
		return post.getTime() <= System.currentTimeMillis();
	}

	@Override
	@Nonnull
	public Predicate<Post> isVisible(@Nullable final Sone currentSone) {
		return post -> (post != null) && isPostVisible(currentSone, post);
	}

}
