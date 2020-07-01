/**
 * Sone - NewElementsTest.kt - Copyright © 2020 David ‘Bombe’ Roden
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
package net.pterodactylus.sone.web

import com.google.inject.Guice
import com.google.inject.name.Names.named
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.notify.ListNotification
import net.pterodactylus.sone.notify.matchThisPost
import net.pterodactylus.sone.notify.matchThisReply
import net.pterodactylus.sone.notify.showAllPosts
import net.pterodactylus.sone.notify.showAllReplies
import net.pterodactylus.sone.test.createPost
import net.pterodactylus.sone.test.createPostReply
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.isProvidedBy
import net.pterodactylus.sone.test.key
import net.pterodactylus.util.template.Template
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Test

class NewElementsTest {

	private val newPostNotification = ListNotification<Post>("", "", Template())
	private val newReplyNotification = ListNotification<PostReply>("", "", Template())
	private val localPostNotification = ListNotification<Post>("", "", Template())
	private val localReplyNotification = ListNotification<PostReply>("", "", Template())
	private val newElements = NewElements(newPostNotification, newReplyNotification, localPostNotification, localReplyNotification, showAllPosts, showAllReplies)

	init {
		localPostNotification.add(post1)
		newPostNotification.add(post2)
		localReplyNotification.add(reply1)
		newReplyNotification.add(reply2)
	}

	@Test
	fun `new elements container can be created by guice`() {
		val injector = Guice.createInjector(
				key<ListNotification<Post>>(named("newRemotePost")).isProvidedBy(newPostNotification),
				key<ListNotification<Post>>(named("localPost")).isProvidedBy(localPostNotification),
				key<ListNotification<PostReply>>(named("newRemotePostReply")).isProvidedBy(newReplyNotification),
				key<ListNotification<PostReply>>(named("localReply")).isProvidedBy(localReplyNotification)
		)
		injector.getInstance<NewElements>()
	}

	@Test
	fun `new posts include new and local posts`() {
		assertThat(newElements.newPosts, containsInAnyOrder(post1, post2))
	}

	@Test
	fun `new posts are filtered`() {
		val newElements = NewElements(newPostNotification, newReplyNotification, localPostNotification, localReplyNotification, matchThisPost(post2), showAllReplies)
		assertThat(newElements.newPosts, contains(post2))
	}

	@Test
	fun `new replies include new and local replies`() {
		assertThat(newElements.newReplies, containsInAnyOrder(reply1, reply2))
	}

	@Test
	fun `new replies are filtered`() {
		val newElements = NewElements(newPostNotification, newReplyNotification, localPostNotification, localReplyNotification, showAllPosts, matchThisReply(reply2))
		assertThat(newElements.newReplies, containsInAnyOrder(reply2))
	}

}

private val post1 = createPost()
private val post2 = createPost()
private val reply1 = createPostReply()
private val reply2 = createPostReply()
