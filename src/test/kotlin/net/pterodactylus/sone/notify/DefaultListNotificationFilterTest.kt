package net.pterodactylus.sone.notify

import com.google.inject.Guice
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.createLocalSone
import net.pterodactylus.sone.test.createPost
import net.pterodactylus.sone.test.createPostReply
import net.pterodactylus.sone.test.verifySingletonInstance
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.notify.TemplateNotification
import net.pterodactylus.util.template.Template
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.Test

/**
 * Unit test for [DefaultListNotificationFilter].
 */
class DefaultListNotificationFilterTest {

	private val listNotificationFilter = DefaultListNotificationFilter(showAllPosts, showAllReplies)

	@Test
	fun `filter is only created once`() {
		val injector = Guice.createInjector()
		injector.verifySingletonInstance<ListNotificationFilter>()
	}

	@Test
	fun `new sone notifications are not removed if not logged in`() {
		val notification = createNewSoneNotification()
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(notification), null)
		assertThat(filteredNotifications, contains<Notification>(notification))
	}

	@Test
	fun `new sone notifications are removed if logged in and new sones should not be shown`() {
		val notification = createNewSoneNotification()
		val localSone = createLocalSone()
		localSone.options.isShowNewSoneNotifications = false
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(notification), localSone)
		assertThat(filteredNotifications, emptyIterable())
	}

	@Test
	fun `new sone notifications are not removed if logged in and new sones should be shown`() {
		val notification = createNewSoneNotification()
		val localSone = createLocalSone()
		localSone.options.isShowNewSoneNotifications = true
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(notification), localSone)
		assertThat(filteredNotifications, contains<Notification>(notification))
	}

	@Test
	fun `new post notification is not shown if options set accordingly`() {
		val newPostNotification = createNewPostNotification()
		newPostNotification.add(createPost())
		val localSone = createLocalSone()
		localSone.options.isShowNewPostNotifications = false
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(newPostNotification), localSone)
		assertThat(filteredNotifications, emptyIterable())
	}

	@Test
	fun `new post notification is not shown if no new posts are visible`() {
		val localSone = createLocalSone()
		localSone.options.isShowNewPostNotifications = true
		val newPostNotification = createNewPostNotification()
		newPostNotification.add(createPost())
		val listNotificationFilter = DefaultListNotificationFilter(showNoPosts, showAllReplies)
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(newPostNotification), localSone)
		assertThat(filteredNotifications, emptyIterable())
	}

	@Test
	fun `new post notification is shown if new posts are visible`() {
		val localSone = createLocalSone()
		localSone.options.isShowNewPostNotifications = true
		val newPostNotification = createNewPostNotification()
		newPostNotification.add(createPost())
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(newPostNotification), localSone)
		assertThat(filteredNotifications, contains<Notification>(newPostNotification))
	}

	@Test
	fun `new post notification is not shown if new posts are visible but local sone is null`() {
		val localSone = createLocalSone()
		localSone.options.isShowNewPostNotifications = true
		val newPostNotification = createNewPostNotification()
		newPostNotification.add(createPost())
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(newPostNotification), null)
		assertThat(filteredNotifications, emptyIterable())
	}

	@Test
	fun `new post notification contains only visible posts`() {
		val localSone = createLocalSone()
		localSone.options.isShowNewPostNotifications = true
		val newPostNotification = createNewPostNotification()
		newPostNotification.add(createPost())
		newPostNotification.add(createPost())
		val listNotificationFilter = DefaultListNotificationFilter(matchThisPost(newPostNotification.elements[1]), showAllReplies)
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(newPostNotification), localSone)
		assertThat((filteredNotifications[0] as ListNotification<Post>).elements, contains(newPostNotification.elements[1]))
	}

	@Test
	fun `new reply notification contains only visible replies`() {
		val localSone = createLocalSone()
		localSone.options.isShowNewReplyNotifications = true
		val newReplyNotification = createNewReplyNotification()
		newReplyNotification.add(createPostReply())
		newReplyNotification.add(createPostReply())
		val listNotificationFilter = DefaultListNotificationFilter(showAllPosts, matchThisReply(newReplyNotification.elements[1]))
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(newReplyNotification), localSone)
		assertThat(filteredNotifications, hasSize(1))
		assertThat((filteredNotifications[0] as ListNotification<PostReply?>).elements[0], equalTo(newReplyNotification.elements[1]))
	}

	@Test
	fun `new reply notification is not modified if all replies are visible`() {
		val localSone = createLocalSone()
		localSone.options.isShowNewReplyNotifications = true
		val newReplyNotification = createNewReplyNotification()
		newReplyNotification.add(createPostReply())
		newReplyNotification.add(createPostReply())
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(newReplyNotification), localSone)
		assertThat(filteredNotifications, contains<Notification>(newReplyNotification))
	}

	@Test
	fun `new reply notification is not shown if no replies are visible`() {
		val localSone = createLocalSone()
		localSone.options.isShowNewReplyNotifications = true
		val newReplyNotification = createNewReplyNotification()
		newReplyNotification.add(createPostReply())
		newReplyNotification.add(createPostReply())
		val listNotificationFilter = DefaultListNotificationFilter(showAllPosts, showNoReplies)
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(newReplyNotification), localSone)
		assertThat(filteredNotifications, emptyIterable())
	}

	@Test
	fun `new reply notification is not shown if deactivated in options`() {
		val localSone = createLocalSone()
		localSone.options.isShowNewReplyNotifications = false
		val newReplyNotification = createNewReplyNotification()
		newReplyNotification.add(createPostReply())
		newReplyNotification.add(createPostReply())
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(newReplyNotification), localSone)
		assertThat(filteredNotifications, emptyIterable())
	}

	@Test
	fun `new reply notification is not shown if current sone is null`() {
		val newReplyNotification = createNewReplyNotification()
		newReplyNotification.add(createPostReply())
		newReplyNotification.add(createPostReply())
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(newReplyNotification), null)
		assertThat(filteredNotifications, emptyIterable())
	}

	@Test
	fun `mention notification contains only visible posts`() {
		val mentionNotification = createMentionNotification()
		mentionNotification.add(createPost())
		mentionNotification.add(createPost())
		val listNotificationFilter = DefaultListNotificationFilter(matchThisPost(mentionNotification.elements[1]), showAllReplies)
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(mentionNotification), null)
		assertThat(filteredNotifications, hasSize(1))
		assertThat((filteredNotifications[0] as ListNotification<Post?>).elements[0], equalTo(mentionNotification.elements[1]))
	}

	@Test
	fun `mention notification is not shown if no posts are visible`() {
		val mentionNotification = createMentionNotification()
		mentionNotification.add(createPost())
		mentionNotification.add(createPost())
		val listNotificationFilter = DefaultListNotificationFilter(showNoPosts, showAllReplies)
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(mentionNotification), null)
		assertThat(filteredNotifications, emptyIterable())
	}


	@Test
	fun `unfilterable notification is not filtered`() {
		val notification: Notification = TemplateNotification("random-notification", Template())
		val filteredNotifications = listNotificationFilter.filterNotifications(listOf(notification), null)
		assertThat(filteredNotifications, contains(notification))
	}

}

private fun createNewSoneNotification() =
		ListNotification<Sone>("new-sone-notification", "", Template())

private fun createNewPostNotification() =
		ListNotification<Post>("new-post-notification", "", Template())

private fun createNewReplyNotification() =
		ListNotification<PostReply>("new-reply-notification", "", Template())

private fun createMentionNotification() =
		ListNotification<Post>("mention-notification", "", Template())
