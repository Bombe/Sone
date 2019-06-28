/*
 * Sone - PostEvent.java - Copyright © 2013–2019 David Roden
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

package net.pterodactylus.sone.core.event

import net.pterodactylus.sone.data.Post

/**
 * Base class for post events.
 */
open class PostEvent
/**
 * Creates a new post event.
 *
 * @param post
 * The post the event is about
 */
protected constructor(
		/** The post the event is about.  */
		private val post: Post) {

	//
	// ACCESSORS
	//

	/**
	 * Returns the post the event is about.
	 *
	 * @return The post the event is about
	 */
	fun post(): Post {
		return post
	}

}
