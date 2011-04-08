/*
 * Sone - GetTimesAjaxPage.java - Copyright © 2010–2011 David Roden
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.json.JsonObject;
import net.pterodactylus.util.number.Digits;

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
	protected JsonObject createJsonObject(Request request) {
		long now = System.currentTimeMillis();
		String allIds = request.getHttpRequest().getParam("posts");
		JsonObject postTimes = new JsonObject();
		if (allIds.length() > 0) {
			String[] ids = allIds.split(",");
			for (String id : ids) {
				Post post = webInterface.getCore().getPost(id, false);
				if (post == null) {
					continue;
				}
				long age = now - post.getTime();
				JsonObject postTime = new JsonObject();
				Time time = getTime(age);
				postTime.put("timeText", time.getText());
				postTime.put("refreshTime", time.getRefresh() / Time.SECOND);
				postTime.put("tooltip", dateFormat.format(new Date(post.getTime())));
				postTimes.put(id, postTime);
			}
		}
		JsonObject replyTimes = new JsonObject();
		allIds = request.getHttpRequest().getParam("replies");
		if (allIds.length() > 0) {
			String[] ids = allIds.split(",");
			for (String id : ids) {
				Reply reply = webInterface.getCore().getReply(id, false);
				if (reply == null) {
					continue;
				}
				long age = now - reply.getTime();
				JsonObject replyTime = new JsonObject();
				Time time = getTime(age);
				replyTime.put("timeText", time.getText());
				replyTime.put("refreshTime", time.getRefresh() / Time.SECOND);
				replyTime.put("tooltip", dateFormat.format(new Date(reply.getTime())));
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
	 * Returns the formatted relative time for a given age.
	 *
	 * @param age
	 *            The age to format (in milliseconds)
	 * @return The formatted age
	 */
	private Time getTime(long age) {
		String text;
		long refresh;
		if (age < 0) {
			text = webInterface.getL10n().getDefaultString("View.Time.InTheFuture");
			refresh = 5 * Time.MINUTE;
		} else if (age < 20 * Time.SECOND) {
			text = webInterface.getL10n().getDefaultString("View.Time.AFewSecondsAgo");
			refresh = 10 * Time.SECOND;
		} else if (age < 45 * Time.SECOND) {
			text = webInterface.getL10n().getString("View.Time.HalfAMinuteAgo");
			refresh = 20 * Time.SECOND;
		} else if (age < 90 * Time.SECOND) {
			text = webInterface.getL10n().getString("View.Time.AMinuteAgo");
			refresh = Time.MINUTE;
		} else if (age < 30 * Time.MINUTE) {
			text = webInterface.getL10n().getString("View.Time.XMinutesAgo", "min", String.valueOf((int) Digits.round(age / Time.MINUTE, 1)));
			refresh = 1 * Time.MINUTE;
		} else if (age < 45 * Time.MINUTE) {
			text = webInterface.getL10n().getString("View.Time.HalfAnHourAgo");
			refresh = 10 * Time.MINUTE;
		} else if (age < 90 * Time.MINUTE) {
			text = webInterface.getL10n().getString("View.Time.AnHourAgo");
			refresh = Time.HOUR;
		} else if (age < 21 * Time.HOUR) {
			text = webInterface.getL10n().getString("View.Time.XHoursAgo", "hour", String.valueOf((int) Digits.round(age / Time.HOUR, 1)));
			refresh = Time.HOUR;
		} else if (age < 42 * Time.HOUR) {
			text = webInterface.getL10n().getString("View.Time.ADayAgo");
			refresh = Time.DAY;
		} else if (age < 6 * Time.DAY) {
			text = webInterface.getL10n().getString("View.Time.XDaysAgo", "day", String.valueOf((int) Digits.round(age / Time.DAY, 1)));
			refresh = Time.DAY;
		} else if (age < 11 * Time.DAY) {
			text = webInterface.getL10n().getString("View.Time.AWeekAgo");
			refresh = Time.DAY;
		} else if (age < 4 * Time.WEEK) {
			text = webInterface.getL10n().getString("View.Time.XWeeksAgo", "week", String.valueOf((int) Digits.round(age / Time.WEEK, 1)));
			refresh = Time.DAY;
		} else if (age < 6 * Time.WEEK) {
			text = webInterface.getL10n().getString("View.Time.AMonthAgo");
			refresh = Time.DAY;
		} else if (age < 11 * Time.MONTH) {
			text = webInterface.getL10n().getString("View.Time.XMonthsAgo", "month", String.valueOf((int) Digits.round(age / Time.MONTH, 1)));
			refresh = Time.DAY;
		} else if (age < 18 * Time.MONTH) {
			text = webInterface.getL10n().getString("View.Time.AYearAgo");
			refresh = Time.WEEK;
		} else {
			text = webInterface.getL10n().getString("View.Time.XYearsAgo", "year", String.valueOf((int) Digits.round(age / Time.YEAR, 1)));
			refresh = Time.WEEK;
		}
		return new Time(text, refresh);
	}

	/**
	 * Container for a formatted time.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class Time {

		/** Number of milliseconds in a second. */
		private static final long SECOND = 1000;

		/** Number of milliseconds in a minute. */
		private static final long MINUTE = 60 * SECOND;

		/** Number of milliseconds in an hour. */
		private static final long HOUR = 60 * MINUTE;

		/** Number of milliseconds in a day. */
		private static final long DAY = 24 * HOUR;

		/** Number of milliseconds in a week. */
		private static final long WEEK = 7 * DAY;

		/** Number of milliseconds in a 30-day month. */
		private static final long MONTH = 30 * DAY;

		/** Number of milliseconds in a year. */
		private static final long YEAR = 365 * DAY;

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

	}

}
