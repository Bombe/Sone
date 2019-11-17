package net.pterodactylus.sone.notify

import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import java.util.concurrent.atomic.*

/**
 * Unit test for [ListNotification].
 */
class ListNotificationTest {

	private val template = Template()
	private val listNotification = ListNotification<String>(ID, KEY, template)

	@Test
	fun `creating a list notification sets empty iterable on element key in template context`() {
		assertThat(template.initialContext.get(KEY) as Iterable<*>, emptyIterable())
	}

	@Test
	fun `new list notification has no element`() {
		assertThat(listNotification.elements, emptyIterable())
	}

	@Test
	fun `new list notification is empty`() {
		assertThat(listNotification.isEmpty, equalTo(true))
	}

	@Test
	fun `list notification retains set elements`() {
		listNotification.setElements(listOf("a", "b", "c"))
		assertThat(listNotification.elements, contains("a", "b", "c"))
	}

	@Test
	fun `list notification deduplicates set elements`() {
		listNotification.setElements(listOf("a", "b", "a"))
		assertThat(listNotification.elements, contains("a", "b"))
	}

	@Test
	fun `list notification retains added elements`() {
		listNotification.add("a")
		listNotification.add("b")
		listNotification.add("c")
		assertThat(listNotification.elements, contains("a", "b", "c"))
	}

	@Test
	fun `list notification deduplicates elements`() {
		listNotification.add("a")
		listNotification.add("b")
		listNotification.add("a")
		assertThat(listNotification.elements, contains("a", "b"))
	}

	@Test
	fun `list notification removes correct element`() {
		listNotification.setElements(listOf("a", "b", "c"))
		listNotification.remove("b")
		assertThat(listNotification.elements, contains("a", "c"))
	}

	@Test
	fun `removing the last element dismisses the notification`() {
		val notificationDismissed = AtomicBoolean()
		val notificationListener = NotificationListener { notificationDismissed.set(it == listNotification) }
		listNotification.addNotificationListener(notificationListener)
		listNotification.add("a")
		listNotification.remove("a")
		assertThat(notificationDismissed.get(), equalTo(true))
	}

	@Test
	fun `dismissing the list notification removes all elements`() {
		listNotification.setElements(listOf("a", "b", "c"))
		listNotification.dismiss()
		assertThat(listNotification.elements, emptyIterable())
	}

	@Test
	fun `list notification with different elements is not equal`() {
		val secondNotification = ListNotification<String>(ID, KEY, template)
		listNotification.add("a")
		secondNotification.add("b")
		assertThat(listNotification, not(equalTo(secondNotification)))
	}

	@Test
	fun `list notification with different key is not equal`() {
		val secondNotification = ListNotification<String>(ID, OTHER_KEY, template)
		assertThat(listNotification, not(equalTo(secondNotification)))
	}

	@Test
	fun `copied notifications have the same hash code`() {
		val secondNotification = ListNotification(listNotification)
		listNotification.add("a")
		secondNotification.add("a")
		listNotification.setLastUpdateTime(secondNotification.lastUpdatedTime)
		assertThat(listNotification.hashCode(), equalTo(secondNotification.hashCode()))
	}

	@Test
	fun `list notification is not equal to other objects`() {
		assertThat(listNotification, not(equalTo(Any())))
	}

}

private const val ID = "notification-id"
private const val KEY = "element-key"
private const val OTHER_KEY = "other-key"
