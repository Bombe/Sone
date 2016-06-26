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
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
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

	/**
	 * Creates a new get times AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public GetTimesAjaxPage(WebInterface webInterface) {
		super("getTimes.ajax", webInterface);
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
		return getTime(webInterface, time);
	}

	//
	// STATIC METHODS
	//

	/**
	 * Returns the formatted relative time for a given time.
	 *
	 * @param webInterface
	 *            The Sone web interface (for l10n access)
	 * @param time
	 *            The time to format the difference from (in milliseconds)
	 * @return The formatted age
	 */
	public static Time getTime(WebInterface webInterface, long time) {
		if (time == 0) {
			return new Time(webInterface.getL10n().getString("View.Sone.Text.UnknownDate"), TimeUnit.HOURS.toMillis(12));
		}
		long age = System.currentTimeMillis() - time;
		String text;
		long refresh;
		if (age < 0) {
			text = webInterface.getL10n().getDefaultString("View.Time.InTheFuture");
			refresh = TimeUnit.MINUTES.toMillis(5);
		} else if (age < TimeUnit.SECONDS.toMillis(20)) {
			text = webInterface.getL10n().getDefaultString("View.Time.AFewSecondsAgo");
			refresh = TimeUnit.SECONDS.toMillis(10);
		} else if (age < TimeUnit.SECONDS.toMillis(45)) {
			text = webInterface.getL10n().getString("View.Time.HalfAMinuteAgo");
			refresh = TimeUnit.SECONDS.toMillis(20);
		} else if (age < TimeUnit.SECONDS.toMillis(90)) {
			text = webInterface.getL10n().getString("View.Time.AMinuteAgo");
			refresh = TimeUnit.MINUTES.toMillis(1);
		} else if (age < TimeUnit.MINUTES.toMillis(30)) {
			text = webInterface.getL10n().getString("View.Time.XMinutesAgo", "min", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(age + TimeUnit.SECONDS.toMillis(30))));
			refresh = TimeUnit.MINUTES.toMillis(1);
		} else if (age < TimeUnit.MINUTES.toMillis(45)) {
			text = webInterface.getL10n().getString("View.Time.HalfAnHourAgo");
			refresh = TimeUnit.MINUTES.toMillis(10);
		} else if (age < TimeUnit.MINUTES.toMillis(90)) {
			text = webInterface.getL10n().getString("View.Time.AnHourAgo");
			refresh = TimeUnit.HOURS.toMillis(1);
		} else if (age < TimeUnit.HOURS.toMillis(21)) {
			text = webInterface.getL10n().getString("View.Time.XHoursAgo", "hour", String.valueOf(TimeUnit.MILLISECONDS.toHours(age + TimeUnit.MINUTES.toMillis(30))));
			refresh = TimeUnit.HOURS.toMillis(1);
		} else if (age < TimeUnit.HOURS.toMillis(42)) {
			text = webInterface.getL10n().getString("View.Time.ADayAgo");
			refresh = TimeUnit.DAYS.toMillis(1);
		} else if (age < TimeUnit.DAYS.toMillis(6)) {
			text = webInterface.getL10n().getString("View.Time.XDaysAgo", "day", String.valueOf(TimeUnit.MILLISECONDS.toDays(age + TimeUnit.HOURS.toMillis(12))));
			refresh = TimeUnit.DAYS.toMillis(1);
		} else if (age < TimeUnit.DAYS.toMillis(11)) {
			text = webInterface.getL10n().getString("View.Time.AWeekAgo");
			refresh = TimeUnit.DAYS.toMillis(1);
		} else if (age < TimeUnit.DAYS.toMillis(28)) {
			text = webInterface.getL10n().getString("View.Time.XWeeksAgo", "week", String.valueOf((TimeUnit.MILLISECONDS.toHours(age) + 84) / (7 * 24)));
			refresh = TimeUnit.DAYS.toMillis(1);
		} else if (age < TimeUnit.DAYS.toMillis(42)) {
			text = webInterface.getL10n().getString("View.Time.AMonthAgo");
			refresh = TimeUnit.DAYS.toMillis(1);
		} else if (age < TimeUnit.DAYS.toMillis(330)) {
			text = webInterface.getL10n().getString("View.Time.XMonthsAgo", "month", String.valueOf((TimeUnit.MILLISECONDS.toDays(age) + 15) / 30));
			refresh = TimeUnit.DAYS.toMillis(1);
		} else if (age < TimeUnit.DAYS.toMillis(540)) {
			text = webInterface.getL10n().getString("View.Time.AYearAgo");
			refresh = TimeUnit.DAYS.toMillis(7);
		} else {
			text = webInterface.getL10n().getString("View.Time.XYearsAgo", "year", String.valueOf((long) ((TimeUnit.MILLISECONDS.toDays(age) + 182.64) / 365.28)));
			refresh = TimeUnit.DAYS.toMillis(7);
		}
		return new Time(text, refresh);
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
