/*
 * Sone - AbstractSoneCommand.java - Copyright © 2011–2019 David Roden
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

package net.pterodactylus.sone.fcp

import com.google.common.base.Optional
import freenet.node.FSParseException
import freenet.support.SimpleFieldSet
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder
import net.pterodactylus.sone.freenet.fcp.AbstractCommand
import net.pterodactylus.sone.freenet.fcp.Command
import net.pterodactylus.sone.freenet.fcp.FcpException
import net.pterodactylus.sone.template.SoneAccessor
import net.pterodactylus.sone.utils.asOptional
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.throwOnNullIf

/**
 * Abstract base implementation of a [Command] with Sone-related helper
 * methods.
 */
abstract class AbstractSoneCommand
@JvmOverloads protected constructor(
		protected val core: Core,
		@get:JvmName("requiresWriteAccess")
		val requiresWriteAccess: Boolean = false) : AbstractCommand() {

	@Throws(FcpException::class)
	protected fun getSone(simpleFieldSet: SimpleFieldSet, parameterName: String, localOnly: Boolean): Sone =
			getSone(simpleFieldSet, parameterName, localOnly, true).get()

	@Throws(FcpException::class)
	protected fun getSone(simpleFieldSet: SimpleFieldSet, parameterName: String, localOnly: Boolean, mandatory: Boolean): Optional<Sone> {
		val soneId = simpleFieldSet.get(parameterName)
				.throwOnNullIf(mandatory) { FcpException("Could not load Sone ID from “$parameterName”.") }
				?: return Optional.absent()
		val sone = core.getSone(soneId)
		if (mandatory && sone == null || sone != null && localOnly && !sone.isLocal) {
			throw FcpException("Could not load Sone from “$soneId”.")
		}
		return sone.asOptional()
	}

	@Throws(FcpException::class)
	protected fun getPost(simpleFieldSet: SimpleFieldSet, parameterName: String): Post {
		try {
			val postId = simpleFieldSet.getString(parameterName)
			return core.getPost(postId)
					?: throw FcpException("Could not load post from “$postId”.")
		} catch (fspe1: FSParseException) {
			throw FcpException("Could not post ID from “$parameterName”.", fspe1)
		}
	}

	@Throws(FcpException::class)
	protected fun getReply(simpleFieldSet: SimpleFieldSet, parameterName: String): PostReply {
		try {
			val replyId = simpleFieldSet.getString(parameterName)
			return core.getPostReply(replyId)
					?: throw FcpException("Could not load reply from “$replyId”.")
		} catch (fspe1: FSParseException) {
			throw FcpException("Could not reply ID from “$parameterName”.", fspe1)
		}
	}

	protected fun encodePost(post: Post, prefix: String, includeReplies: Boolean): SimpleFieldSet = SimpleFieldSetBuilder().apply {
		put(prefix + "ID", post.id)
		put(prefix + "Sone", post.sone.id)
		post.recipientId.let { put(prefix + "Recipient", it) }
		put(prefix + "Time", post.time)
		put(prefix + "Text", encodeString(post.text))
		put(encodeLikes(core.getLikes(post), "${prefix}Likes."))
		if (includeReplies) {
			val replies = core.getReplies(post.id)
			put(encodeReplies(replies, prefix))
		}
	}.get()

	protected fun encodePosts(posts: Collection<Post>, prefix: String, includeReplies: Boolean): SimpleFieldSet = SimpleFieldSetBuilder().apply {
		put(prefix + "Count", posts.size)
		posts.forEachIndexed { postIndex, post ->
			put(encodePost(post, "$prefix$postIndex.", includeReplies))
		}
	}.get()

	private fun encodeReplies(replies: Collection<PostReply>, prefix: String): SimpleFieldSet = SimpleFieldSetBuilder().apply {
		put(prefix + "Replies.Count", replies.size)
		replies.forEachIndexed { replyIndex, reply ->
			val replyPrefix = "${prefix}Replies.$replyIndex."
			put(replyPrefix + "ID", reply.id)
			put(replyPrefix + "Sone", reply.sone.id)
			put(replyPrefix + "Time", reply.time)
			put(replyPrefix + "Text", encodeString(reply.text))
			put(encodeLikes(core.getLikes(reply), "${replyPrefix}Likes."))
		}
	}.get()

	override fun toString() = "${javaClass.name}[requiresWriteAccess=$requiresWriteAccess]"
}

fun encodeString(text: String) = text
		.replace("\\\\".toRegex(), "\\\\\\\\")
		.replace("\n".toRegex(), "\\\\n")
		.replace("\r".toRegex(), "\\\\r")

fun encodeSone(sone: Sone, prefix: String, localSone: Optional<Sone>): SimpleFieldSet = SimpleFieldSetBuilder().apply {
	put(prefix + "ID", sone.id)
	put(prefix + "Name", sone.name)
	put(prefix + "NiceName", SoneAccessor.getNiceName(sone))
	put(prefix + "LastUpdated", sone.time)
	localSone.let { put(prefix + "Followed", it.hasFriend(sone.id).toString()) }
	val profile = sone.profile
	put(prefix + "Field.Count", profile.fields.size)
	profile.fields.forEachIndexed { fieldIndex, field ->
		put(prefix + "Field." + fieldIndex + ".Name", field.name)
		put(prefix + "Field." + fieldIndex + ".Value", field.value)
	}
}.get()

fun encodeSones(sones: Collection<Sone>, prefix: String): SimpleFieldSet = SimpleFieldSetBuilder().apply {
	put(prefix + "Count", sones.size)
	sones.forEachIndexed { soneIndex, sone ->
		put(encodeSone(sone, "$prefix$soneIndex.", Optional.absent()))
	}
}.get()

fun encodeLikes(likes: Collection<Sone>, prefix: String): SimpleFieldSet = SimpleFieldSetBuilder().apply {
	put(prefix + "Count", likes.size)
	likes.forEachIndexed { index, sone -> put("$prefix$index.ID", sone.id) }
}.get()
