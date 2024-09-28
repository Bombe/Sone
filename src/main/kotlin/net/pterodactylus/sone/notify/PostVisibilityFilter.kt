package net.pterodactylus.sone.notify

import com.google.inject.ImplementedBy
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone

@ImplementedBy(DefaultPostVisibilityFilter::class)
@JvmDefaultWithoutCompatibility
interface PostVisibilityFilter {

	fun isPostVisible(sone: Sone?, post: Post): Boolean

	fun isVisible(currentSone: Sone?) =
			{ p: Post? -> p != null && isPostVisible(currentSone, p) }

}
