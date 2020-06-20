/*
 * Sone - ListNotificationFilter.java - Copyright © 2010–2020 David Roden
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

import static com.google.common.collect.FluentIterable.from;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.notify.Notification;

import com.google.common.base.Optional;

/**
 * Filter for {@link ListNotification}s.
 */
@Singleton
public class ListNotificationFilter {

	private final PostVisibilityFilter postVisibilityFilter;
	private final ReplyVisibilityFilter replyVisibilityFilter;

	@Inject
	public ListNotificationFilter(@Nonnull PostVisibilityFilter postVisibilityFilter, @Nonnull ReplyVisibilityFilter replyVisibilityFilter) {
		this.postVisibilityFilter = postVisibilityFilter;
		this.replyVisibilityFilter = replyVisibilityFilter;
	}

	/**
	 * Filters new-post and new-reply notifications in the given list of
	 * notifications. If {@code currentSone} is <code>null</code>, new-post and
	 * new-reply notifications are removed completely. If {@code currentSone} is
	 * not {@code null}, only posts that are posted by a friend Sone or the Sone
	 * itself, and replies that are replies to posts of friend Sones or the Sone
	 * itself will be retained in the notifications.
	 *
	 * @param notifications
	 * 		The notifications to filter
	 * @param currentSone
	 * 		The current Sone, or {@code null} if not logged in
	 * @return The filtered notifications
	 */
	@SuppressWarnings("unchecked")
	public List<Notification> filterNotifications(Collection<? extends Notification> notifications, Sone currentSone) {
		List<Notification> filteredNotifications = new ArrayList<>();
		for (Notification notification : notifications) {
			if (notification.getId().equals("new-sone-notification")) {
				if ((currentSone != null) && !currentSone.getOptions().isShowNewSoneNotifications()) {
					continue;
				}
				filteredNotifications.add(notification);
			} else if (notification.getId().equals("new-post-notification")) {
				if (currentSone == null) {
					continue;
				}
				if (!currentSone.getOptions().isShowNewPostNotifications()) {
					continue;
				}
				Optional<ListNotification<Post>> filteredNotification = filterPostNotification((ListNotification<Post>) notification, currentSone);
				if (filteredNotification.isPresent()) {
					filteredNotifications.add(filteredNotification.get());
				}
			} else if (notification.getId().equals("new-reply-notification")) {
				if (currentSone == null) {
					continue;
				}
				if (!currentSone.getOptions().isShowNewReplyNotifications()) {
					continue;
				}
				Optional<ListNotification<PostReply>> filteredNotification =
						filterNewReplyNotification((ListNotification<PostReply>) notification, currentSone);
				if (filteredNotification.isPresent()) {
					filteredNotifications.add(filteredNotification.get());
				}
			} else if (notification.getId().equals("mention-notification")) {
				Optional<ListNotification<Post>> filteredNotification = filterPostNotification((ListNotification<Post>) notification, null);
				if (filteredNotification.isPresent()) {
					filteredNotifications.add(filteredNotification.get());
				}
			} else {
				filteredNotifications.add(notification);
			}
		}
		return filteredNotifications;
	}

	/**
	 * Filters the posts of the given notification.
	 *
	 * @param postNotification
	 * 		The post notification
	 * @param currentSone
	 * 		The current Sone, or {@code null} if not logged in
	 * @return The filtered post notification, or {@link Optional#absent()} if the notification should be removed
	 */
	@Nonnull
	private Optional<ListNotification<Post>> filterPostNotification(@Nonnull ListNotification<Post> postNotification,
			@Nullable Sone currentSone) {
		List<Post> newPosts = postNotification.getElements().stream().filter(postVisibilityFilter.isVisible(currentSone)).collect(toList());
		if (newPosts.isEmpty()) {
			return Optional.absent();
		}
		if (newPosts.size() == postNotification.getElements().size()) {
			return Optional.of(postNotification);
		}
		ListNotification<Post> filteredNotification = new ListNotification<>(postNotification);
		filteredNotification.setElements(newPosts);
		filteredNotification.setLastUpdateTime(postNotification.getLastUpdatedTime());
		return Optional.of(filteredNotification);
	}

	/**
	 * Filters the new replies of the given notification. If {@code currentSone}
	 * is {@code null}, {@code null} is returned and the notification is
	 * subsequently removed. Otherwise only replies that are replies to posts
	 * that are posted by friend Sones of the given Sone are retained; all other
	 * replies are removed.
	 *
	 * @param newReplyNotification
	 * 		The new-reply notification
	 * @param currentSone
	 * 		The current Sone, or {@code null} if not logged in
	 * @return The filtered new-reply notification, or {@code null} if the
	 * notification should be removed
	 */
	private Optional<ListNotification<PostReply>> filterNewReplyNotification(ListNotification<PostReply> newReplyNotification,
			@Nonnull Sone currentSone) {
		List<PostReply> newReplies = from(newReplyNotification.getElements()).filter(replyVisibilityFilter.isVisible(currentSone)).toList();
		if (newReplies.isEmpty()) {
			return Optional.absent();
		}
		if (newReplies.size() == newReplyNotification.getElements().size()) {
			return Optional.of(newReplyNotification);
		}
		ListNotification<PostReply> filteredNotification = new ListNotification<>(newReplyNotification);
		filteredNotification.setElements(newReplies);
		filteredNotification.setLastUpdateTime(newReplyNotification.getLastUpdatedTime());
		return Optional.of(filteredNotification);
	}

}
