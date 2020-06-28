package net.pterodactylus.sone.notify

import com.google.inject.ImplementedBy
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import java.util.function.Predicate

@ImplementedBy(DefaultReplyVisibilityFilter::class)
interface ReplyVisibilityFilter {

	fun isReplyVisible(sone: Sone?, reply: PostReply): Boolean

	@JvmDefault
	fun isVisible(currentSone: Sone?): Predicate<PostReply> =
			Predicate { reply: PostReply -> isReplyVisible(currentSone, reply) }

}
