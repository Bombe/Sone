/*
 * Sone - PostReplyBuilder.java - Copyright © 2013–2016 David Roden
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

package net.pterodactylus.sone.database

import net.pterodactylus.sone.data.PostReply

/**
 * Builder for a [PostReply] object.
 */
interface PostReplyBuilder : ReplyBuilder<PostReplyBuilder> {

	fun to(postId: String): PostReplyBuilder

	/**
	 * Verifies the configuration of this builder and creates a new post reply.
	 *
	 * The following conditions must be met in order for the configuration to be
	 * considered valid:
	 *
	 *  * Exactly one of [randomId] or [withId] must have been called.
	 *  * The [sender][from] must not be `null`.
	 *  * Exactly one of [currentTime] or [withTime] must have been called.
	 *  * The [text][withText] must not be `null` and must contain something other than whitespace.
	 *  * The [post][to] must have been set.
	 *
	 * @return The created post reply
	 * @throws IllegalStateException if this builder’s configuration is not valid
	 */
	fun build(): PostReply

}
