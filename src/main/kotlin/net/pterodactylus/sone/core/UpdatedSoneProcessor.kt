package net.pterodactylus.sone.core

import com.google.common.eventbus.EventBus
import com.google.inject.ImplementedBy
import net.pterodactylus.sone.core.event.NewPostFoundEvent
import net.pterodactylus.sone.core.event.NewPostReplyFoundEvent
import net.pterodactylus.sone.core.event.PostRemovedEvent
import net.pterodactylus.sone.core.event.PostReplyRemovedEvent
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.Sone.SoneStatus
import net.pterodactylus.sone.database.Database
import net.pterodactylus.sone.utils.ifFalse
import net.pterodactylus.util.logging.Logging
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
			logger.fine { "Downloaded Sone $sone can not update stored Sone $storedSone." }
			return
		}
		collectEventsForChanges(storedSone, sone)
				.also { database.storeSone(sone) }
				.forEach(eventBus::post)
		sone.options = storedSone.options
		sone.isKnown = storedSone.isKnown
		sone.status = if (sone.time != 0L) SoneStatus.idle else SoneStatus.unknown
	}

	protected abstract fun soneCanBeUpdated(storedSone: Sone, newSone: Sone): Boolean

	private val Sone.followingTime get() = database.getFollowingTime(id) ?: 0

	private fun collectEventsForChanges(oldSone: Sone, newSone: Sone): List<Any> =
			SoneChangeCollector(oldSone)
					.onNewPost { post -> if (post.time <= newSone.followingTime) post.isKnown = true }
					.newPostEvent { post -> post.isKnown.ifFalse { NewPostFoundEvent(post) } }
					.removedPostEvent { PostRemovedEvent(it) }
					.onNewPostReply { postReply -> if (postReply.time <= newSone.followingTime) postReply.isKnown = true }
					.newPostReplyEvent { postReply -> postReply.isKnown.ifFalse { NewPostReplyFoundEvent(postReply) } }
					.onRemovedPostReply { PostReplyRemovedEvent(it) }
					.detectChanges(newSone)

}

class DefaultUpdateSoneProcessor @Inject constructor(database: Database, eventBus: EventBus) :
		BasicUpdateSoneProcessor(database, eventBus) {

	override fun soneCanBeUpdated(storedSone: Sone, newSone: Sone) =
			newSone.time > storedSone.time

}
