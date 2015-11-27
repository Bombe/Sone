/*
 * Sone - ListNotificationFilters.java - Copyright © 2010–2015 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.notify;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.freenet.wot.Trust;
import net.pterodactylus.util.notify.Notification;

import com.google.common.base.Optional;

/**
 * Filter for {@link ListNotification}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ListNotificationFilters {

	/**
	 * Filters new-post and new-reply notifications in the given list of
	 * notifications. If {@code currentSone} is <code>null</code>, new-post and
	 * new-reply notifications are removed completely. If {@code currentSone} is
	 * not {@code null}, only posts that are posted by a friend Sone or the Sone
	 * itself, and replies that are replies to posts of friend Sones or the Sone
	 * itself will be retained in the notifications.
	 *
	 * @param notifications
	 *            The notifications to filter
	 * @param currentSone
	 *            The current Sone, or {@code null} if not logged in
	 * @return The filtered notifications
	 */
	@SuppressWarnings("unchecked")
	public static List<Notification> filterNotifications(Collection<? extends Notification> notifications, Sone currentSone) {
		List<Notification> filteredNotifications = new ArrayList<Notification>();
		for (Notification notification : notifications) {
			if (notification.getId().equals("new-sone-notification")) {
				if ((currentSone != null) && !currentSone.getOptions().isShowNewSoneNotifications()) {
					continue;
				}
				filteredNotifications.add(notification);
			} else if (notification.getId().equals("new-post-notification")) {
				if ((currentSone != null) && !currentSone.getOptions().isShowNewPostNotifications()) {
					continue;
				}
				ListNotification<Post> filteredNotification = filterNewPostNotification((ListNotification<Post>) notification, currentSone, true);
				if (filteredNotification != null) {
					filteredNotifications.add(filteredNotification);
				}
			} else if (notification.getId().equals("new-reply-notification")) {
				if ((currentSone != null) && !currentSone.getOptions().isShowNewReplyNotifications()) {
					continue;
				}
				ListNotification<PostReply> filteredNotification = filterNewReplyNotification((ListNotification<PostReply>) notification, currentSone);
				if (filteredNotification != null) {
					filteredNotifications.add(filteredNotification);
				}
			} else if (notification.getId().equals("mention-notification")) {
				ListNotification<Post> filteredNotification = filterNewPostNotification((ListNotification<Post>) notification, null, false);
				if (filteredNotification != null) {
					filteredNotifications.add(filteredNotification);
				}
			} else {
				filteredNotifications.add(notification);
			}
		}
		return filteredNotifications;
	}

	/**
	 * Filters the new posts of the given notification. If {@code currentSone}
	 * is {@code null} and {@code soneRequired} is {@code true}, {@code null} is
	 * returned and the notification is subsequently removed. Otherwise only
	 * posts that are posted by friend Sones of the given Sone are retained; all
	 * other posts are removed.
	 *
	 * @param newPostNotification
	 *            The new-post notification
	 * @param currentSone
	 *            The current Sone, or {@code null} if not logged in
	 * @param soneRequired
	 *            Whether a non-{@code null} Sone in {@code currentSone} is
	 *            required
	 * @return The filtered new-post notification, or {@code null} if the
	 *         notification should be removed
	 */
	public static ListNotification<Post> filterNewPostNotification(ListNotification<Post> newPostNotification, Sone currentSone, boolean soneRequired) {
		if (soneRequired && (currentSone == null)) {
			return null;
		}
		List<Post> newPosts = new ArrayList<Post>();
		for (Post post : newPostNotification.getElements()) {
			if (isPostVisible(currentSone, post)) {
				newPosts.add(post);
			}
		}
		if (newPosts.isEmpty()) {
			return null;
		}
		if (newPosts.size() == newPostNotification.getElements().size()) {
			return newPostNotification;
		}
		ListNotification<Post> filteredNotification = new ListNotification<Post>(newPostNotification);
		filteredNotification.setElements(newPosts);
		filteredNotification.setLastUpdateTime(newPostNotification.getLastUpdatedTime());
		return filteredNotification;
	}

	/**
	 * Filters the new replies of the given notification. If {@code currentSone}
	 * is {@code null}, {@code null} is returned and the notification is
	 * subsequently removed. Otherwise only replies that are replies to posts
	 * that are posted by friend Sones of the given Sone are retained; all other
	 * replies are removed.
	 *
	 * @param newReplyNotification
	 *            The new-reply notification
	 * @param currentSone
	 *            The current Sone, or {@code null} if not logged in
	 * @return The filtered new-reply notification, or {@code null} if the
	 *         notification should be removed
	 */
	public static ListNotification<PostReply> filterNewReplyNotification(ListNotification<PostReply> newReplyNotification, Sone currentSone) {
		if (currentSone == null) {
			return null;
		}
		List<PostReply> newReplies = new ArrayList<PostReply>();
		for (PostReply reply : newReplyNotification.getElements()) {
			if (isReplyVisible(currentSone, reply)) {
				newReplies.add(reply);
			}
		}
		if (newReplies.isEmpty()) {
			return null;
		}
		if (newReplies.size() == newReplyNotification.getElements().size()) {
			return newReplyNotification;
		}
		ListNotification<PostReply> filteredNotification = new ListNotification<PostReply>(newReplyNotification);
		filteredNotification.setElements(newReplies);
		filteredNotification.setLastUpdateTime(newReplyNotification.getLastUpdatedTime());
		return filteredNotification;
	}

	/**
	 * Filters the given posts, using {@link #isPostVisible(Sone, Post)} to
	 * decide whether a post should be contained in the returned list. If
	 * {@code currentSone} is not {@code null} it is used to filter out posts
	 * that are from Sones that are not followed or not trusted by the given
	 * Sone.
	 *
	 * @param posts
	 *            The posts to filter
	 * @param currentSone
	 *            The current Sone (may be {@code null})
	 * @return The filtered posts
	 */
	public static List<Post> filterPosts(Collection<Post> posts, Sone currentSone) {
		List<Post> filteredPosts = new ArrayList<Post>();
		for (Post post : posts) {
			if (isPostVisible(currentSone, post)) {
				filteredPosts.add(post);
			}
		}
		return filteredPosts;
	}

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
	 *            The Sone that checks for a post’s visibility (may be
	 *            {@code null} to skip Sone-specific checks, such as trust)
	 * @param post
	 *            The post to check for visibility
	 * @return {@code true} if the post is considered visible, {@code false}
	 *         otherwise
	 */
	public static boolean isPostVisible(Sone sone, Post post) {
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
		if (post.getTime() > System.currentTimeMillis()) {
			return false;
		}
		return true;
	}

	/**
	 * Checks whether a reply is visible to the given Sone. A reply is not
	 * considered visible if one of the following statements is true:
	 * <ul>
	 * <li>The reply does not have a post.</li>
	 * <li>The reply’s post does not have a Sone.</li>
	 * <li>The Sone of the reply’s post is not the given Sone, the given Sone
	 * does not follow the reply’s post’s Sone, and the given Sone is not the
	 * recipient of the reply’s post.</li>
	 * <li>The trust relationship between the two Sones can not be retrieved.</li>
	 * <li>The given Sone has explicitely assigned negative trust to the post’s
	 * Sone.</li>
	 * <li>The given Sone has not explicitely assigned negative trust to the
	 * reply’s post’s Sone but the implicit trust is negative.</li>
	 * <li>The reply’s post’s {@link Post#getTime() time} is in the future.</li>
	 * <li>The reply’s {@link Reply#getTime() time} is in the future.</li>
	 * </ul>
	 * If none of these statements is true the reply is considered visible.
	 *
	 * @param sone
	 *            The Sone that checks for a post’s visibility (may be
	 *            {@code null} to skip Sone-specific checks, such as trust)
	 * @param reply
	 *            The reply to check for visibility
	 * @return {@code true} if the reply is considered visible, {@code false}
	 *         otherwise
	 */
	public static boolean isReplyVisible(Sone sone, PostReply reply) {
		checkNotNull(reply, "reply must not be null");
		Optional<Post> post = reply.getPost();
		if (!post.isPresent()) {
			return false;
		}
		if (!isPostVisible(sone, post.get())) {
			return false;
		}
		if (reply.getTime() > System.currentTimeMillis()) {
			return false;
		}
		return true;
	}

}
