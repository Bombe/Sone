package net.pterodactylus.sone.core

import com.google.common.eventbus.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.fcp.FcpInterface.*
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.*
import net.pterodactylus.sone.fcp.event.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [Preferences].
 */
class PreferencesTest {

	private val eventBus = mock<EventBus>()
	private val preferences = Preferences(eventBus)
	private val eventsCaptor = capture<Any>()

	@Test
	fun `preferences retain insertion delay`() {
		preferences.insertionDelay = 15
		assertThat(preferences.insertionDelay, equalTo(15))
	}

	@Test
	fun `preferences sends event on setting insertion delay`() {
		preferences.insertionDelay = 15
		verify(eventBus, atLeastOnce()).post(eventsCaptor.capture())
		assertThat(eventsCaptor.allValues, hasItem(InsertionDelayChangedEvent(15)))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid insertion delay is rejected`() {
		preferences.insertionDelay = -15
	}

	@Test
	fun `no event is sent when invalid insertion delay is set`() {
		try {
			preferences.insertionDelay = -15
		} catch (iae: IllegalArgumentException) {
			/* ignore. */
		}

		verify(eventBus, never()).post(any())
	}

	@Test
	fun `preferences return default value when insertion delay is set to null`() {
		preferences.setInsertionDelay(null)
		assertThat(preferences.insertionDelay, equalTo(60))
	}

	@Test
	fun `preferences start with insertion delay default value`() {
		assertThat(preferences.insertionDelay, equalTo(60))
	}

	@Test
	fun `preferences retain posts per page`() {
		preferences.postsPerPage = 15
		assertThat(preferences.postsPerPage, equalTo(15))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid posts per page is rejected`() {
		preferences.postsPerPage = -15
	}

	@Test
	fun `preferences return default value when posts per page is set to null`() {
		preferences.setPostsPerPage(null)
		assertThat(preferences.postsPerPage, equalTo(10))
	}

	@Test
	fun `preferences start with posts per page default value`() {
		assertThat(preferences.postsPerPage, equalTo(10))
	}

	@Test
	fun `preferences retain images per page`() {
		preferences.imagesPerPage = 15
		assertThat(preferences.imagesPerPage, equalTo(15))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid images per page is rejected`() {
		preferences.imagesPerPage = -15
	}

	@Test
	fun `preferences return default value when images per page is set to null`() {
		preferences.setImagesPerPage(null)
		assertThat(preferences.imagesPerPage, equalTo(9))
	}

	@Test
	fun `preferences start with images per page default value`() {
		assertThat(preferences.imagesPerPage, equalTo(9))
	}

	@Test
	fun `preferences retain characters per post`() {
		preferences.charactersPerPost = 150
		assertThat(preferences.charactersPerPost, equalTo(150))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid characters per post is rejected`() {
		preferences.charactersPerPost = -15
	}

	@Test
	fun `preferences return default value when characters per post is set to null`() {
		preferences.setCharactersPerPost(null)
		assertThat(preferences.charactersPerPost, equalTo(400))
	}

	@Test
	fun `preferences start with characters per post default value`() {
		assertThat(preferences.charactersPerPost, equalTo(400))
	}

	@Test
	fun `preferences retain post cut off length`() {
		preferences.postCutOffLength = 150
		assertThat(preferences.postCutOffLength, equalTo(150))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid post cut off length is rejected`() {
		preferences.postCutOffLength = -15
	}

	@Test(expected = IllegalArgumentException::class)
	fun `cut off length of minus one is not allowed`() {
		preferences.postCutOffLength = -1
	}

	@Test
	fun `preferences return default value when post cut off length is set to null`() {
		preferences.setPostCutOffLength(null)
		assertThat(preferences.postCutOffLength, equalTo(200))
	}

	@Test
	fun `preferences start with post cut off length default value`() {
		assertThat(preferences.postCutOffLength, equalTo(200))
	}

	@Test
	fun `preferences retain require full access of true`() {
		preferences.isRequireFullAccess = true
		assertThat(preferences.isRequireFullAccess, equalTo(true))
	}

	@Test
	fun `preferences retain require full access of false`() {
		preferences.isRequireFullAccess = false
		assertThat(preferences.isRequireFullAccess, equalTo(false))
	}

	@Test
	fun `preferences return default value when require full access is set to null`() {
		preferences.setRequireFullAccess(null)
		assertThat(preferences.isRequireFullAccess, equalTo(false))
	}

	@Test
	fun `preferences start with require full access default value`() {
		assertThat(preferences.isRequireFullAccess, equalTo(false))
	}

	@Test
	fun `preferences retain positive trust`() {
		preferences.positiveTrust = 15
		assertThat(preferences.positiveTrust, equalTo(15))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid positive trust is rejected`() {
		preferences.positiveTrust = -15
	}

	@Test
	fun `preferences return default value when positive trust is set to null`() {
		preferences.setPositiveTrust(null)
		assertThat(preferences.positiveTrust, equalTo(75))
	}

	@Test
	fun `preferences start with positive trust default value`() {
		assertThat(preferences.positiveTrust, equalTo(75))
	}

	@Test
	fun `preferences retain negative trust`() {
		preferences.negativeTrust = -15
		assertThat(preferences.negativeTrust, equalTo(-15))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid negative trust is rejected`() {
		preferences.negativeTrust = 150
	}

	@Test
	fun `preferences return default value when negative trust is set to null`() {
		preferences.setNegativeTrust(null)
		assertThat(preferences.negativeTrust, equalTo(-25))
	}

	@Test
	fun `preferences start with negative trust default value`() {
		assertThat(preferences.negativeTrust, equalTo(-25))
	}

	@Test
	fun `preferences retain trust comment`() {
		preferences.trustComment = "Trust"
		assertThat(preferences.trustComment, equalTo("Trust"))
	}

	@Test
	fun `preferences return default value when trust comment is set to null`() {
		preferences.trustComment = null
		assertThat(preferences.trustComment,
				equalTo("Set from Sone Web Interface"))
	}

	@Test
	fun `preferences start with trust comment default value`() {
		assertThat(preferences.trustComment,
				equalTo("Set from Sone Web Interface"))
	}

	@Test
	fun `preferences retain fcp interface active of true`() {
		preferences.isFcpInterfaceActive = true
		assertThat(preferences.isFcpInterfaceActive, equalTo(true))
		verify(eventBus).post(any(FcpInterfaceActivatedEvent::class.java))
	}

	@Test
	fun `preferences retain fcp interface active of false`() {
		preferences.isFcpInterfaceActive = false
		assertThat(preferences.isFcpInterfaceActive, equalTo(false))
		verify(eventBus).post(any(FcpInterfaceDeactivatedEvent::class.java))
	}

	@Test
	fun `preferences return default value when fcp interface active is set to null`() {
		preferences.setFcpInterfaceActive(null)
		assertThat(preferences.isFcpInterfaceActive, equalTo(false))
		verify(eventBus).post(any(FcpInterfaceDeactivatedEvent::class.java))
	}

	@Test
	fun `preferences start with fcp interface active default value`() {
		assertThat(preferences.isFcpInterfaceActive, equalTo(false))
	}

	@Test
	fun `preferences retain fcp full access required of no`() {
		preferences.fcpFullAccessRequired = NO
		assertThat(preferences.fcpFullAccessRequired, equalTo(NO))
		verifyFullAccessRequiredChangedEvent(NO)
	}

	private fun verifyFullAccessRequiredChangedEvent(
			fullAccessRequired: FullAccessRequired) {
		verify(eventBus).post(eventsCaptor.capture())
		assertThat(eventsCaptor.value, instanceOf(FullAccessRequiredChanged::class.java))
		assertThat((eventsCaptor.value as FullAccessRequiredChanged).fullAccessRequired,
				equalTo(fullAccessRequired))
	}

	@Test
	fun `preferences retain fcp full access required of writing`() {
		preferences.fcpFullAccessRequired = WRITING
		assertThat(preferences.fcpFullAccessRequired, equalTo(WRITING))
		verifyFullAccessRequiredChangedEvent(WRITING)
	}

	@Test
	fun `preferences retain fcp full access required of always`() {
		preferences.fcpFullAccessRequired = ALWAYS
		assertThat(preferences.fcpFullAccessRequired, equalTo(ALWAYS))
		verifyFullAccessRequiredChangedEvent(ALWAYS)
	}

	@Test
	fun `preferences return default value when fcp full access required is set to null`() {
		preferences.fcpFullAccessRequired = null
		assertThat(preferences.fcpFullAccessRequired, equalTo(ALWAYS))
		verifyFullAccessRequiredChangedEvent(ALWAYS)
	}

	@Test
	fun `preferences start with fcp full access required default value`() {
		assertThat(preferences.fcpFullAccessRequired, equalTo(ALWAYS))
	}

	@Test
	fun `setting insertion delay to valid value sends change event`() {
		preferences.insertionDelay = 30
		verify(eventBus, atLeastOnce()).post(eventsCaptor.capture())
		assertThat(eventsCaptor.allValues, hasItem(PreferenceChangedEvent("InsertionDelay", 30)))
	}

	@Test
	fun `setting posts per page to valid value sends change event`() {
		preferences.postsPerPage = 30
		verify(eventBus, atLeastOnce()).post(eventsCaptor.capture())
		assertThat(eventsCaptor.allValues, hasItem(PreferenceChangedEvent("PostsPerPage", 30)))
	}

}
