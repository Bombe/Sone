/*
 * Sone - PostReplyEvent.java - Copyright © 2013–2015 David Roden
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
 * Base class for {@link PostReply} events.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostReplyEvent {

	/** The post reply the event is about. */
	private final PostReply postReply;

	/**
	 * Creates a new post reply event.
	 *
	 * @param postReply
	 *            The post reply the event is about
	 */
	protected PostReplyEvent(PostReply postReply) {
		this.postReply = postReply;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the post reply the event is about.
	 *
	 * @return The post reply the event is about
	 */
	public PostReply postReply() {
		return postReply;
	}

}
