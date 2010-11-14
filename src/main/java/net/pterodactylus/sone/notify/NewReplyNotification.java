/*
 * Sone - NewReplyNotification.java - Copyright © 2010 David Roden
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
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.util.notify.TemplateNotification;
import net.pterodactylus.util.template.Template;

/**
 * Notification that signals that new {@link Reply}s have been discovered.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class NewReplyNotification extends TemplateNotification {

	/** The new replies. */
	private final List<Reply> newReplies = Collections.synchronizedList(new ArrayList<Reply>());

	/**
	 * Creates a “new reply” notification.
	 *
	 * @param template
	 *            The template to render
	 */
	public NewReplyNotification(Template template) {
		super(template);
		template.set("replies", newReplies);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether there are any new replies.
	 *
	 * @return {@code true} if there are no new replies, {@code false} if there
	 *         are new replies
	 */
	public boolean isEmpty() {
		return newReplies.isEmpty();
	}

	/**
	 * Adds a discovered reply.
	 *
	 * @param reply
	 *            The new reply
	 */
	public void addReply(Reply reply) {
		newReplies.add(reply);
		touch();
	}

	/**
	 * Removes the given reply from the list of new replies.
	 *
	 * @param reply
	 *            The reply to remove
	 */
	public void removeReply(Reply reply) {
		newReplies.remove(reply);
		touch();
	}

	//
	// ABSTRACTNOTIFICATION METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dismiss() {
		super.dismiss();
		newReplies.clear();
	}

}
