/*
 * Sone - GetTimesAjaxPage.java - Copyright © 2010–2016 David Roden
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.web.ajax;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.freenet.L10nFilter;
import net.pterodactylus.sone.text.TimeText;
import net.pterodactylus.sone.text.TimeTextConverter;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;

/**
 * Ajax page that returns a formatted, relative timestamp for replies or posts.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetTimesAjaxPage extends JsonPage {

	/** Formatter for tooltips. */
	private static final DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, HH:mm:ss");
	private final TimeTextConverter timeTextConverter;
	private final L10nFilter l10nFilter;

	/**
	 * Creates a new get times AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public GetTimesAjaxPage(WebInterface webInterface, TimeTextConverter timeTextConverter, L10nFilter l10nFilter) {
		super("getTimes.ajax", webInterface);
		this.timeTextConverter = timeTextConverter;
		this.l10nFilter = l10nFilter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
		String allIds = request.getHttpRequest().getParam("posts");
		ObjectNode postTimes = new ObjectNode(instance);
		if (allIds.length() > 0) {
			String[] ids = allIds.split(",");
			for (String id : ids) {
				Optional<Post> post = webInterface.getCore().getPost(id);
				if (!post.isPresent()) {
					continue;
				}
				ObjectNode postTime = new ObjectNode(instance);
				Time time = getTime(post.get().getTime());
				postTime.put("timeText", time.getText());
				postTime.put("refreshTime", TimeUnit.MILLISECONDS.toSeconds(time.getRefresh()));
				synchronized (dateFormat) {
					postTime.put("tooltip", dateFormat.format(new Date(post.get().getTime())));
				}
				postTimes.put(id, postTime);
			}
		}
		ObjectNode replyTimes = new ObjectNode(instance);
		allIds = request.getHttpRequest().getParam("replies");
		if (allIds.length() > 0) {
			String[] ids = allIds.split(",");
			for (String id : ids) {
				Optional<PostReply> reply = webInterface.getCore().getPostReply(id);
				if (!reply.isPresent()) {
					continue;
				}
				ObjectNode replyTime = new ObjectNode(instance);
				Time time = getTime(reply.get().getTime());
				replyTime.put("timeText", time.getText());
				replyTime.put("refreshTime", TimeUnit.MILLISECONDS.toSeconds(time.getRefresh()));
				synchronized (dateFormat) {
					replyTime.put("tooltip", dateFormat.format(new Date(reply.get().getTime())));
				}
				replyTimes.put(id, replyTime);
			}
		}
		return createSuccessJsonObject().put("postTimes", postTimes).put("replyTimes", replyTimes);
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
	 * Returns the formatted relative time for a given time.
	 *
	 * @param time
	 *            The time to format the difference from (in milliseconds)
	 * @return The formatted age
	 */
	private Time getTime(long time) {
		TimeText timeText = timeTextConverter.getTimeText(time);
		return new Time(l10nFilter.format(null, timeText.getL10nText(), Collections.<String, Object>emptyMap()), timeText.getRefreshTime());
	}

	/**
	 * Container for a formatted time.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class Time {

		/** The formatted time. */
		private final String text;

		/** The time after which to refresh the time. */
		private final long refresh;

		/**
		 * Creates a new formatted time container.
		 *
		 * @param text
		 *            The formatted time
		 * @param refresh
		 *            The time after which to refresh the time (in milliseconds)
		 */
		public Time(String text, long refresh) {
			this.text = text;
			this.refresh = refresh;
		}

		/**
		 * Returns the formatted time.
		 *
		 * @return The formatted time
		 */
		public String getText() {
			return text;
		}

		/**
		 * Returns the time after which to refresh the time.
		 *
		 * @return The time after which to refresh the time (in milliseconds)
		 */
		public long getRefresh() {
			return refresh;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return text;
		}

	}

}
