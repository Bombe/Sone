/**
 * Sone - MarkPostKnownDuringFirstStartHandlerTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.web.notification

import com.google.common.eventbus.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.util.notify.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import java.util.function.*
import kotlin.test.*

/**
 * Unit test for [MarkPostKnownDuringFirstStartHandler].
 */
@Suppress("UnstableApiUsage")
class MarkPostKnownDuringFirstStartHandlerTest {

	private val eventBus = EventBus()
	private val notificationManager = NotificationManager()
	private val markedPosts = mutableListOf<Post>()
	private val handler = MarkPostKnownDuringFirstStartHandler(notificationManager, Consumer { markedPosts += it })

	init {
		eventBus.register(handler)
	}

	@Test
	fun `post is not marked as known if not during first start`() {
		eventBus.post(NewPostFoundEvent(post))
		assertThat(markedPosts, emptyIterable())
	}

	@Test
	fun `new post is marked as known during first start`() {
		notificationManager.firstStart()
		eventBus.post(NewPostFoundEvent(post))
		assertThat(markedPosts, contains(post))
	}

}

private val post: Post = Post.EmptyPost("post")
