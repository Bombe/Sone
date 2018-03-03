/*
 * Sone - MarkPostReplyKnownEvent.java - Copyright © 2013–2016 David Roden
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

package net.pterodactylus.sone.core.event;

import net.pterodactylus.sone.data.PostReply;

/**
 * Event that signals that a {@link PostReply} has been marked as
 * {@link PostReply#isKnown() known}.
 */
public class MarkPostReplyKnownEvent extends PostReplyEvent {

	/**
	 * Creates a new “post reply marked known” event.
	 *
	 * @param postReply
	 *            The post reply that was marked as known
	 */
	public MarkPostReplyKnownEvent(PostReply postReply) {
		super(postReply);
	}

}
