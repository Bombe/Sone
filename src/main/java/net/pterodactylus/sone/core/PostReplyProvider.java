/*
 * Sone - PostReplyProvider.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sone.core;

import java.util.List;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;

/**
 * Interface for objects that can provide {@link PostReply}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface PostReplyProvider {

	/**
	 * Returns the reply with the given ID.
	 *
	 * @param id
	 *            The ID of the reply to get
	 * @return The reply, or {@code null} if there is no such reply
	 */
	public PostReply getPostReply(String id);

	/**
	 * Returns all replies for the given post, order ascending by time.
	 *
	 * @param post
	 *            The post to get all replies for
	 * @return All replies for the given post
	 */
	public List<PostReply> getReplies(Post post);

}
