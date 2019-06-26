package net.pterodactylus.sone.core

import com.google.common.eventbus.*
import com.google.inject.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.Sone.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.logging.*
import javax.inject.Inject

/**
 * An `UpdatedSoneProcessor` is called to process a [Sone] after it has been
 * downloaded from Freenet.
 */
@ImplementedBy(DefaultUpdateSoneProcessor::class)
interface UpdatedSoneProcessor {

	fun updateSone(sone: Sone)

}

abstract class BasicUpdateSoneProcessor(private val database: Database, private val eventBus: EventBus) :
		UpdatedSoneProcessor {

	private val logger = Logging.getLogger(UpdatedSoneProcessor::javaClass.name)!!

	override fun updateSone(sone: Sone) {
		val storedSone = database.getSone(sone.id) ?: return
		if (!soneCanBeUpdated(storedSone, sone)) {
			logger.fine("Downloaded Sone $sone can not update stored Sone $storedSone.")
			return
		}

		SoneComparison(storedSone, sone).apply {
			newPosts
					.onEach { post -> if (post.time <= sone.followingTime) post.isKnown = true }
					.mapNotNull { post -> post.isKnown.ifFalse { NewPostFoundEvent(post) } }
					.forEach(eventBus::post)
			removedPosts
					.map { PostRemovedEvent(it) }
					.forEach(eventBus::post)
			newPostReplies
					.onEach { postReply -> if (postReply.time <= sone.followingTime) postReply.isKnown = true }
					.mapNotNull { postReply -> postReply.isKnown.ifFalse { NewPostReplyFoundEvent(postReply) } }
					.forEach(eventBus::post)
			removedPostReplies
					.map { PostReplyRemovedEvent(it) }
					.forEach(eventBus::post)
		}
		database.storeSone(sone)
		sone.options = storedSone.options
		sone.isKnown = storedSone.isKnown
		sone.status = if (sone.time != 0L) SoneStatus.idle else SoneStatus.unknown
	}

	protected abstract fun soneCanBeUpdated(storedSone: Sone, newSone: Sone): Boolean

	private val Sone.followingTime get() = database.getFollowingTime(id) ?: 0

}

class DefaultUpdateSoneProcessor @Inject constructor(database: Database, eventBus: EventBus) :
		BasicUpdateSoneProcessor(database, eventBus) {

	override fun soneCanBeUpdated(storedSone: Sone, newSone: Sone) =
			newSone.time > storedSone.time

}
