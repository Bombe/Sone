/*
 * Sone - GetStatusAjaxPage.java - Copyright © 2010–2013 David Roden
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

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.notify.ListNotificationFilters;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.notify.Notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

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
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
		final Sone currentSone = getCurrentSone(request.getToadletContext(), false);
		/* load Sones. always return the status of the current Sone. */
		Set<Sone> sones = new HashSet<Sone>(Collections.singleton(getCurrentSone(request.getToadletContext(), false)));
		String loadSoneIds = request.getHttpRequest().getParam("soneIds");
		if (loadSoneIds.length() > 0) {
			String[] soneIds = loadSoneIds.split(",");
			for (String soneId : soneIds) {
				/* just add it, we skip null further down. */
				sones.add(webInterface.getCore().getSone(soneId).orNull());
			}
		}
		ArrayNode jsonSones = new ArrayNode(instance);
		for (Sone sone : sones) {
			if (sone == null) {
				continue;
			}
			jsonSones.add(createJsonSone(sone));
		}
		/* load notifications. */
		List<Notification> notifications = ListNotificationFilters.filterNotifications(webInterface.getNotifications().getNotifications(), currentSone);
		Collections.sort(notifications, Notification.CREATED_TIME_SORTER);
		/* load new posts. */
		Collection<Post> newPosts = webInterface.getNewPosts();
		if (currentSone != null) {
			newPosts = Collections2.filter(newPosts, new Predicate<Post>() {

				@Override
				public boolean apply(Post post) {
					return ListNotificationFilters.isPostVisible(currentSone, post);
				}

			});
		}
		ArrayNode jsonPosts = new ArrayNode(instance);
		for (Post post : newPosts) {
			ObjectNode jsonPost = new ObjectNode(instance);
			jsonPost.put("id", post.getId());
			jsonPost.put("sone", post.getSone().getId());
			jsonPost.put("recipient", post.getRecipientId().orNull());
			jsonPost.put("time", post.getTime());
			jsonPosts.add(jsonPost);
		}
		/* load new replies. */
		Collection<PostReply> newReplies = webInterface.getNewReplies();
		if (currentSone != null) {
			newReplies = Collections2.filter(newReplies, new Predicate<PostReply>() {

				@Override
				public boolean apply(PostReply reply) {
					return ListNotificationFilters.isReplyVisible(currentSone, reply);
				}

			});
		}
		/* remove replies to unknown posts. */
		newReplies = Collections2.filter(newReplies, PostReply.HAS_POST_FILTER);
		ArrayNode jsonReplies = new ArrayNode(instance);
		for (PostReply reply : newReplies) {
			ObjectNode jsonReply = new ObjectNode(instance);
			jsonReply.put("id", reply.getId());
			jsonReply.put("sone", reply.getSone().getId());
			jsonReply.put("post", reply.getPostId());
			jsonReply.put("postSone", reply.getPost().get().getSone().getId());
			jsonReplies.add(jsonReply);
		}
		return createSuccessJsonObject().put("loggedIn", currentSone != null).put("options", createJsonOptions(currentSone)).put("sones", jsonSones).put("notificationHash", notifications.hashCode()).put("newPosts", jsonPosts).put("newReplies", jsonReplies);
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
	private JsonNode createJsonSone(Sone sone) {
		ObjectNode jsonSone = new ObjectNode(instance);
		jsonSone.put("id", sone.getId());
		jsonSone.put("name", SoneAccessor.getNiceName(sone));
		jsonSone.put("local", sone.getInsertUri() != null);
		jsonSone.put("status", sone.getStatus().name());
		jsonSone.put("modified", webInterface.getCore().isModifiedSone(sone));
		jsonSone.put("locked", webInterface.getCore().isLocked(sone));
		jsonSone.put("lastUpdatedUnknown", sone.getTime() == 0);
		synchronized (dateFormat) {
			jsonSone.put("lastUpdated", dateFormat.format(new Date(sone.getTime())));
		}
		jsonSone.put("lastUpdatedText", GetTimesAjaxPage.getTime(webInterface, sone.getTime()).getText());
		return jsonSone;
	}

	/**
	 * Creates a JSON object that contains all options that are currently in
	 * effect for the given Sone (or overall, if the given Sone is {@code null}
	 * ).
	 *
	 * @param currentSone
	 *            The current Sone (may be {@code null})
	 * @return The current options
	 */
	private static JsonNode createJsonOptions(Sone currentSone) {
		ObjectNode options = new ObjectNode(instance);
		if (currentSone != null) {
			options.put("ShowNotification/NewSones", currentSone.getOptions().getBooleanOption("ShowNotification/NewSones").get());
			options.put("ShowNotification/NewPosts", currentSone.getOptions().getBooleanOption("ShowNotification/NewPosts").get());
			options.put("ShowNotification/NewReplies", currentSone.getOptions().getBooleanOption("ShowNotification/NewReplies").get());
		}
		return options;
	}

}
