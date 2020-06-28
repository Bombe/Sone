package net.pterodactylus.sone.notify

import com.google.inject.ImplementedBy
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone

@ImplementedBy(DefaultReplyVisibilityFilter::class)
interface ReplyVisibilityFilter {

	fun isReplyVisible(sone: Sone?, reply: PostReply): Boolean

	@JvmDefault
	fun isVisible(currentSone: Sone?) =
			{ reply: PostReply -> isReplyVisible(currentSone, reply) }

}
