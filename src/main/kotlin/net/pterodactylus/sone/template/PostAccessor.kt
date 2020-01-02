/*
 * Sone - PostAccessor.java - Copyright © 2010–2019 David Roden
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
package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.util.template.*

/**
 * Accessor for [Post] objects that adds additional properties:
 *
 * * `replies`: All replies to this post, sorted by time, oldest first
 * * `likes`: All Sones that have liked the post
 * * `liked`: `true` if the current Sone from the [template context][TemplateContext] has liked the post
 * * `new`: `true` if the post is not known
 * * `bookmarked`: `true` if the post is bookmarked
 */
class PostAccessor(private val core: Core) : ReflectionAccessor() {

	override fun get(templateContext: TemplateContext?, `object`: Any?, member: String): Any? =
			(`object` as Post).let { post ->
				when (member) {
					"replies" -> core.getReplies(post.id).filter { Reply.FUTURE_REPLY_FILTER.apply(it) }
					"likes" -> core.getLikes(post)
					"liked" -> (templateContext?.get("currentSone") as? Sone)?.isLikedPostId(post.id) ?: false
					"new" -> !post.isKnown
					"bookmarked" -> core.isBookmarked(post)
					else -> super.get(templateContext, `object`, member)
				}
			}

}
