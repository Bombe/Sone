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
import net.pterodactylus.util.notify.Notification;

/**
 * Filter for {@link ListNotification}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ListNotificationFilters {

	/**
	 * Filters new-post and new-reply notifications in the given list of
	 * notifications. If {@code currentSone} is <code>null</code>, nothing is
	 * filtered and the given list is returned.
	 * If {@code currentSone} is not {@code null}, only posts that are posted by
	 * a friend Sone or the Sone itself, and replies that are replies to posts
	 * of friend Sones or the Sone itself will be retained
	 * in the notifications.
	 *
	 * @param notifications
	 *            The notifications to filter
	 * @param currentSone
	 *            The current Sone, or {@code null} if not logged in
	 * @return The filtered notifications
	 */
	public static List<Notification> filterNotifications(List<Notification> notifications, Sone currentSone) {
		if (currentSone == null) {
			return notifications;
		}
		ListNotification<Post> newPostNotification = getNotification(notifications, "new-post-notification", Post.class);
		System.out.println("Found new-post-notification with " + ((newPostNotification != null) ? newPostNotification.getElements().size() : -1) + " posts.");
		if (newPostNotification != null) {
			List<Post> newPosts = new ArrayList<Post>();
			for (Post post : newPostNotification.getElements()) {
				System.out.println("Checking Post: " + post);
				if (currentSone.hasFriend(post.getSone().getId()) || currentSone.equals(post.getSone())) {
					System.out.println("  CS.hF: " + currentSone.hasFriend(post.getSone().getId()));
					System.out.println("  CS.e:" + currentSone.equals(post.getSone()));
					newPosts.add(post);
				}
			}
			int notificationIndex = notifications.indexOf(newPostNotification);
			if (newPosts.isEmpty()) {
				System.out.println("Removing notification.");
				notifications.remove(notificationIndex);
			} else {
				System.out.println("Replacing Notification.");
				newPostNotification = new ListNotification<Post>(newPostNotification);
				newPostNotification.setElements(newPosts);
				notifications.set(notificationIndex, newPostNotification);
			}
		}
		ListNotification<Reply> newReplyNotification = getNotification(notifications, "new-replies-notification", Reply.class);
		System.out.println("Found new-reply-notification with " + ((newReplyNotification != null) ? newReplyNotification.getElements().size() : -1) + " replies.");
		if (newReplyNotification != null) {
			List<Reply> newReplies = new ArrayList<Reply>();
			for (Reply reply : newReplyNotification.getElements()) {
				System.out.println("Checking Reply: " + reply);
				if (currentSone.hasFriend(reply.getPost().getSone().getId()) || currentSone.equals(reply.getPost().getSone())) {
					System.out.println("  CS.hF: " + currentSone.hasFriend(reply.getPost().getSone().getId()));
					System.out.println("  CS.e: " + currentSone.equals(reply.getPost().getSone()));
					newReplies.add(reply);
				}
			}
			int notificationIndex = notifications.indexOf(newReplyNotification);
			if (newReplies.isEmpty()) {
				System.out.println("Removing Notification.");
				notifications.remove(notificationIndex);
			} else {
				System.out.println("Replacing Notification.");
				newReplyNotification = new ListNotification<Reply>(newReplyNotification);
				newReplyNotification.setElements(newReplies);
				notifications.set(notificationIndex, newReplyNotification);
			}
		}
		return notifications;
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

}
