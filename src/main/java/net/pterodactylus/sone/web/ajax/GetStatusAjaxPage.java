/*
 * Sone - GetStatusAjaxPage.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.web.ajax;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.notify.ListNotificationFilters;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.filter.Filter;
import net.pterodactylus.util.filter.Filters;
import net.pterodactylus.util.json.JsonArray;
import net.pterodactylus.util.json.JsonObject;
import net.pterodactylus.util.notify.Notification;

/**
 * The “get status” AJAX handler returns all information that is necessary to
 * update the web interface in real-time.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetStatusAjaxPage extends JsonPage {

	/** Date formatter. */
	private static final DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, HH:mm:ss");

	/**
	 * Creates a new “get status” AJAX handler.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public GetStatusAjaxPage(WebInterface webInterface) {
		super("getStatus.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(Request request) {
		final Sone currentSone = getCurrentSone(request.getToadletContext(), false);
		/* load Sones. */
		boolean loadAllSones = Boolean.parseBoolean(request.getHttpRequest().getParam("loadAllSones", "true"));
		Set<Sone> sones = new HashSet<Sone>(Collections.singleton(getCurrentSone(request.getToadletContext(), false)));
		if (loadAllSones) {
			sones.addAll(webInterface.getCore().getSones());
		}
		JsonArray jsonSones = new JsonArray();
		for (Sone sone : sones) {
			if (sone == null) {
				continue;
			}
			JsonObject jsonSone = createJsonSone(sone);
			jsonSones.add(jsonSone);
		}
		/* load notifications. */
		List<Notification> notifications = ListNotificationFilters.filterNotifications(new ArrayList<Notification>(webInterface.getNotifications().getNotifications()), currentSone);
		Collections.sort(notifications, Notification.LAST_UPDATED_TIME_SORTER);
		JsonArray jsonNotificationInformations = new JsonArray();
		for (Notification notification : notifications) {
			jsonNotificationInformations.add(createJsonNotificationInformation(notification));
		}
		/* load new posts. */
		Set<Post> newPosts = webInterface.getNewPosts();
		if (currentSone != null) {
			newPosts = Filters.filteredSet(newPosts, new Filter<Post>() {

				@Override
				public boolean filterObject(Post post) {
					return currentSone.hasFriend(post.getSone().getId()) || currentSone.equals(post.getSone()) || currentSone.equals(post.getRecipient());
				}

			});
		}
		JsonArray jsonPosts = new JsonArray();
		for (Post post : newPosts) {
			JsonObject jsonPost = new JsonObject();
			jsonPost.put("id", post.getId());
			jsonPost.put("sone", post.getSone().getId());
			jsonPost.put("recipient", (post.getRecipient() != null) ? post.getRecipient().getId() : null);
			jsonPost.put("time", post.getTime());
			jsonPosts.add(jsonPost);
		}
		/* load new replies. */
		Set<Reply> newReplies = webInterface.getNewReplies();
		if (currentSone != null) {
			newReplies = Filters.filteredSet(newReplies, new Filter<Reply>() {

				@Override
				public boolean filterObject(Reply reply) {
					return (reply.getPost() != null) && (reply.getPost().getSone() != null) && (currentSone.hasFriend(reply.getPost().getSone().getId()) || currentSone.equals(reply.getPost().getSone()) || currentSone.equals(reply.getPost().getRecipient()));
				}

			});
		}
		JsonArray jsonReplies = new JsonArray();
		for (Reply reply : newReplies) {
			JsonObject jsonReply = new JsonObject();
			jsonReply.put("id", reply.getId());
			jsonReply.put("sone", reply.getSone().getId());
			jsonReply.put("post", reply.getPost().getId());
			jsonReply.put("postSone", reply.getPost().getSone().getId());
			jsonReplies.add(jsonReply);
		}
		return createSuccessJsonObject().put("sones", jsonSones).put("notifications", jsonNotificationInformations).put("newPosts", jsonPosts).put("newReplies", jsonReplies);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean needsFormPassword() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean requiresLogin() {
		return false;
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates a JSON object from the given Sone.
	 *
	 * @param sone
	 *            The Sone to convert to a JSON object
	 * @return The JSON representation of the given Sone
	 */
	private JsonObject createJsonSone(Sone sone) {
		JsonObject jsonSone = new JsonObject();
		jsonSone.put("id", sone.getId());
		jsonSone.put("name", SoneAccessor.getNiceName(sone));
		jsonSone.put("local", sone.getInsertUri() != null);
		jsonSone.put("status", webInterface.getCore().getSoneStatus(sone).name());
		jsonSone.put("modified", webInterface.getCore().isModifiedSone(sone));
		jsonSone.put("locked", webInterface.getCore().isLocked(sone));
		jsonSone.put("lastUpdatedUnknown", sone.getTime() == 0);
		synchronized (dateFormat) {
			jsonSone.put("lastUpdated", dateFormat.format(new Date(sone.getTime())));
		}
		jsonSone.put("lastUpdatedText", GetTimesAjaxPage.getTime(webInterface, System.currentTimeMillis() - sone.getTime()).getText());
		return jsonSone;
	}

	/**
	 * Creates a JSON object that only contains the ID and the last-updated time
	 * of the given notification.
	 *
	 * @see Notification#getId()
	 * @see Notification#getLastUpdatedTime()
	 * @param notification
	 *            The notification
	 * @return A JSON object containing the notification ID and last-updated
	 *         time
	 */
	private JsonObject createJsonNotificationInformation(Notification notification) {
		JsonObject jsonNotification = new JsonObject();
		jsonNotification.put("id", notification.getId());
		jsonNotification.put("lastUpdatedTime", notification.getLastUpdatedTime());
		return jsonNotification;
	}

}
