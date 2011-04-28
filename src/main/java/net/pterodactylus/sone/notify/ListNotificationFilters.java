/*
 * Sone - ListNotificationFilters.java - Copyright © 2010 David Roden
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.freenet.wot.Trust;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.validation.Validation;

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
	public static List<Notification> filterNotifications(List<Notification> notifications, Sone currentSone) {
		ListNotification<Post> newPostNotification = getNotification(notifications, "new-post-notification", Post.class);
		if (newPostNotification != null) {
			ListNotification<Post> filteredNotification = filterNewPostNotification(newPostNotification, currentSone);
			int notificationIndex = notifications.indexOf(newPostNotification);
			if (filteredNotification == null) {
				notifications.remove(notificationIndex);
			} else {
				notifications.set(notificationIndex, filteredNotification);
			}
		}
		ListNotification<Reply> newReplyNotification = getNotification(notifications, "new-replies-notification", Reply.class);
		if (newReplyNotification != null) {
			ListNotification<Reply> filteredNotification = filterNewReplyNotification(newReplyNotification, currentSone);
			int notificationIndex = notifications.indexOf(newReplyNotification);
			if (filteredNotification == null) {
				notifications.remove(notificationIndex);
			} else {
				notifications.set(notificationIndex, filteredNotification);
			}
		}
		return notifications;
	}

	/**
	 * Filters the new posts of the given notification. If {@code currentSone}
	 * is {@code null}, {@code null} is returned and the notification is
	 * subsequently removed. Otherwise only posts that are posted by friend
	 * Sones of the given Sone are retained; all other posts are removed.
	 *
	 * @param newPostNotification
	 *            The new-post notification
	 * @param currentSone
	 *            The current Sone, or {@code null} if not logged in
	 * @return The filtered new-post notification, or {@code null} if the
	 *         notification should be removed
	 */
	public static ListNotification<Post> filterNewPostNotification(ListNotification<Post> newPostNotification, Sone currentSone) {
		if (currentSone == null) {
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
	public static ListNotification<Reply> filterNewReplyNotification(ListNotification<Reply> newReplyNotification, Sone currentSone) {
		if (currentSone == null) {
			return null;
		}
		List<Reply> newReplies = new ArrayList<Reply>();
		for (Reply reply : newReplyNotification.getElements()) {
			if (isPostVisible(currentSone, reply.getPost())) {
				newReplies.add(reply);
			}
		}
		if (newReplies.isEmpty()) {
			return null;
		}
		if (newReplies.size() == newReplyNotification.getElements().size()) {
			return newReplyNotification;
		}
		ListNotification<Reply> filteredNotification = new ListNotification<Reply>(newReplyNotification);
		filteredNotification.setElements(newReplies);
		return filteredNotification;
	}

	/**
	 * Finds the notification with the given ID in the list of notifications and
	 * returns it.
	 *
	 * @param <T>
	 *            The type of the item in the notification
	 * @param notifications
	 *            The notification to search
	 * @param notificationId
	 *            The ID of the requested notification
	 * @param notificationElementClass
	 *            The class of the notification item
	 * @return The requested notification, or {@code null} if no notification
	 *         with the given ID could be found
	 */
	@SuppressWarnings("unchecked")
	private static <T> ListNotification<T> getNotification(Collection<? extends Notification> notifications, String notificationId, Class<T> notificationElementClass) {
		for (Notification notification : notifications) {
			if (!notificationId.equals(notification.getId())) {
				continue;
			}
			return (ListNotification<T>) notification;
		}
		return null;
	}

	/**
	 * Checks whether a post is visible to the given Sone. A post is not
	 * considered visible if one of the following statements is true:
	 * <ul>
	 * <li>The post does not have a Sone.</li>
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
	 *            The Sone that checks for a post’s visibility
	 * @param post
	 *            The post to check for visibility
	 * @return {@code true} if the post is considered visible, {@code false}
	 *         otherwise
	 */
	public static boolean isPostVisible(Sone sone, Post post) {
		Validation.begin().isNotNull("Sone", sone).isNotNull("Post", post).check().isNotNull("Sone’s Identity", sone.getIdentity()).check().isInstanceOf("Sone’s Identity", sone.getIdentity(), OwnIdentity.class).check();
		Sone postSone = post.getSone();
		if (postSone == null) {
			return false;
		}
		Trust trust = postSone.getIdentity().getTrust((OwnIdentity) sone.getIdentity());
		if (trust != null) {
			if ((trust.getExplicit() != null) && (trust.getExplicit() < 0)) {
				return false;
			}
			if ((trust.getExplicit() == null) && (trust.getImplicit() != null) && (trust.getImplicit() < 0)) {
				return false;
			}
		} else {
			return false;
		}
		if ((!postSone.equals(sone)) && !sone.hasFriend(postSone.getId()) && !sone.equals(post.getRecipient())) {
			return false;
		}
		return true;
	}

}
