package net.pterodactylus.sone.notify

import com.google.inject.ImplementedBy
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import java.util.function.Predicate

@ImplementedBy(DefaultPostVisibilityFilter::class)
interface PostVisibilityFilter {

	fun isPostVisible(sone: Sone?, post: Post): Boolean

	@JvmDefault
	fun isVisible(currentSone: Sone?): Predicate<Post?> =
			Predicate { p: Post? -> p != null && isPostVisible(currentSone, p) }

}
