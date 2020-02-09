/*
 * Sone - PostBuilder.kt - Copyright © 2013–2020 David Roden
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

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone

/**
 * Builder for [Post] objects.
 *
 *
 * A [Post] consists of the following elements:
 *
 *  * an ID,
 *  * a [sender][Sone],
 *  * an optional [recipient][Sone],
 *  * a time,
 *  * and a text.
 *
 * Except for the recipient, all this elements have to be configured on this
 * builder. For the ID you have the possibility to configure either a random ID
 * (which should be used for new posts) or a custom ID you specify (for creating
 * an existing post). For the time you can use the current time (again, for
 * creating new posts) or the given time (for loading posts). It is an error to
 * specify both ways for either the ID or the time.
 */
interface PostBuilder {

	fun copyPost(post: Post): PostBuilder

	fun from(senderId: String): PostBuilder

	fun randomId(): PostBuilder
	fun withId(id: String): PostBuilder

	fun currentTime(): PostBuilder
	fun withTime(time: Long): PostBuilder

	fun withText(text: String): PostBuilder

	fun to(recipientId: String): PostBuilder

	/**
	 * Verifies this builder’s configuration and creates a new post.
	 *
	 * The following conditions must be met in order for this builder to be
	 * configured correctly:
	 *
	 *  * Exactly one of [randomId] or [withId] must have been called.
	 *  * The [sender][from] must not be `null`.
	 *  * Exactly one of [currentTime] or [withTime] must have been called.
	 *  * The [text][withText] must not be `null` and must contain something other than whitespace.
	 *  * The [recipient][to] must either not have been set, or it must have been set to a [Sone] other than [the sender][from].
	 *
	 * @return A new post
	 * @throws IllegalStateException if this builder’s configuration is not valid
	 */
	fun build(): Post

}
