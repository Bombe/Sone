package net.pterodactylus.sone.core

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import net.pterodactylus.sone.core.event.InsertionDelayChangedEvent
import net.pterodactylus.sone.core.event.StrictFilteringActivatedEvent
import net.pterodactylus.sone.core.event.StrictFilteringDeactivatedEvent
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.ALWAYS
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.NO
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.WRITING
import net.pterodactylus.sone.fcp.event.FcpInterfaceActivatedEvent
import net.pterodactylus.sone.fcp.event.FcpInterfaceDeactivatedEvent
import net.pterodactylus.sone.fcp.event.FullAccessRequiredChanged
import net.pterodactylus.util.config.Configuration
import net.pterodactylus.util.config.MapConfigurationBackend
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * Unit test for [Preferences].
 */
class PreferencesTest {

	private val eventBus = EventBus()
	private val preferences = Preferences(eventBus)

	@Test
	fun `preferences retain insertion delay`() {
		preferences.newInsertionDelay = 15
		assertThat(preferences.insertionDelay, equalTo(15))
	}

	@Test
	fun `preferences sends event on setting insertion delay`() {
		val events = mutableListOf<InsertionDelayChangedEvent>()
		eventBus.register(object {
			@Subscribe
			fun insertionDelayChangedEvent(event: InsertionDelayChangedEvent) =
					events.add(event)
		})
		preferences.newInsertionDelay = 15
		assertThat(events, hasItem(InsertionDelayChangedEvent(15)))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid insertion delay is rejected`() {
		preferences.newInsertionDelay = -15
	}

	@Test
	fun `no event is sent when invalid insertion delay is set`() {
		val events = mutableListOf<InsertionDelayChangedEvent>()
		eventBus.register(object {
			@Subscribe
			fun insertionDelayChanged(event: InsertionDelayChangedEvent) =
					events.add(event)
		})
		try {
			preferences.newInsertionDelay = -15
		} catch (iae: IllegalArgumentException) {
			/* ignore. */
		}

		assertThat(events, emptyIterable())
	}

	@Test
	fun `preferences return default value when insertion delay is set to null`() {
		preferences.newInsertionDelay = null
		assertThat(preferences.insertionDelay, equalTo(60))
	}

	@Test
	fun `preferences start with insertion delay default value`() {
		assertThat(preferences.insertionDelay, equalTo(60))
	}

	@Test
	fun `preferences saves null for default insertion delay setting`() {
		verifySavedOption(nullValue()) { it.getIntValue("Option/InsertionDelay").getValue(null) }
	}

	@Test
	fun `preferences retain posts per page`() {
		preferences.newPostsPerPage = 15
		assertThat(preferences.postsPerPage, equalTo(15))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid posts per page is rejected`() {
		preferences.newPostsPerPage = -15
	}

	@Test
	fun `preferences return default value when posts per page is set to null`() {
		preferences.newPostsPerPage = null
		assertThat(preferences.postsPerPage, equalTo(10))
	}

	@Test
	fun `preferences start with posts per page default value`() {
		assertThat(preferences.postsPerPage, equalTo(10))
	}

	@Test
	fun `preferences retain images per page`() {
		preferences.newImagesPerPage = 15
		assertThat(preferences.imagesPerPage, equalTo(15))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid images per page is rejected`() {
		preferences.newImagesPerPage = -15
	}

	@Test
	fun `preferences return default value when images per page is set to null`() {
		preferences.newImagesPerPage = null
		assertThat(preferences.imagesPerPage, equalTo(9))
	}

	@Test
	fun `preferences start with images per page default value`() {
		assertThat(preferences.imagesPerPage, equalTo(9))
	}

	@Test
	fun `preferences retain characters per post`() {
		preferences.newCharactersPerPost = 150
		assertThat(preferences.charactersPerPost, equalTo(150))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid characters per post is rejected`() {
		preferences.newCharactersPerPost = -15
	}

	@Test
	fun `preferences return default value when characters per post is set to null`() {
		preferences.newCharactersPerPost = null
		assertThat(preferences.charactersPerPost, equalTo(400))
	}

	@Test
	fun `preferences start with characters per post default value`() {
		assertThat(preferences.charactersPerPost, equalTo(400))
	}

	@Test
	fun `preferences retain post cut off length`() {
		preferences.newPostCutOffLength = 150
		assertThat(preferences.postCutOffLength, equalTo(150))
	}

	@Test(expected = IllegalArgumentException::class)
	fun `invalid post cut off length is rejected`() {
		preferences.newPostCutOffLength = -15
	}

	@Test(expected = IllegalArgumentException::class)
	fun `cut off length of minus one is not allowed`() {
		preferences.newPostCutOffLength = -1
	}

	@Test
	fun `preferences return default value when post cut off length is set to null`() {
		preferences.newPostCutOffLength = null
		assertThat(preferences.postCutOffLength, equalTo(200))
	}

	@Test
	fun `preferences start with post cut off length default value`() {
		assertThat(preferences.postCutOffLength, equalTo(200))
	}

	@Test
	fun `preferences retain require full access of true`() {
		preferences.newRequireFullAccess = true
		assertThat(preferences.requireFullAccess, equalTo(true))
	}

	@Test
	fun `preferences retain require full access of false`() {
		preferences.newRequireFullAccess = false
		assertThat(preferences.requireFullAccess, equalTo(false))
	}

	@Test
	fun `preferences return default value when require full access is set to null`() {
		preferences.newRequireFullAccess = null
		assertThat(preferences.requireFullAccess, equalTo(false))
	}

	@Test
	fun `preferences start with require full access default value`() {
		assertThat(preferences.requireFullAccess, equalTo(false))
	}

	@Test
	fun `preferences retain fcp interface active of true`() {
		val events = mutableListOf<FcpInterfaceActivatedEvent>()
		eventBus.register(object {
			@Subscribe
			fun fcpInterfaceActivatedEvent(event: FcpInterfaceActivatedEvent) =
					events.add(event)
		})
		preferences.newFcpInterfaceActive = true
		assertThat(preferences.fcpInterfaceActive, equalTo(true))
		assertThat(events, hasItem<FcpInterfaceActivatedEvent>(instanceOf(FcpInterfaceActivatedEvent::class.java)))
	}

	@Test
	fun `preferences retain fcp interface active of false`() {
		val events = mutableListOf<FcpInterfaceDeactivatedEvent>()
		eventBus.register(object {
			@Subscribe
			fun fcpInterfaceDeactivatedEvent(event: FcpInterfaceDeactivatedEvent) =
					events.add(event)
		})
		preferences.newFcpInterfaceActive = false
		assertThat(preferences.fcpInterfaceActive, equalTo(false))
		assertThat(events, hasItem<FcpInterfaceDeactivatedEvent>(instanceOf(FcpInterfaceDeactivatedEvent::class.java)))
	}

	@Test
	fun `preferences return default value when fcp interface active is set to null`() {
		val events = mutableListOf<FcpInterfaceDeactivatedEvent>()
		eventBus.register(object {
			@Subscribe
			fun fcpInterfaceDeactivatedEvent(event: FcpInterfaceDeactivatedEvent) =
					events.add(event)
		})
		preferences.newFcpInterfaceActive = null
		assertThat(preferences.fcpInterfaceActive, equalTo(false))
		assertThat(events, hasItem<FcpInterfaceDeactivatedEvent>(instanceOf(FcpInterfaceDeactivatedEvent::class.java)))
	}

	@Test
	fun `preferences start with fcp interface active default value`() {
		assertThat(preferences.fcpInterfaceActive, equalTo(false))
	}

	@Test
	fun `preferences retain fcp full access required of no`() {
		verifyFullAccessRequiredChangedEvent(NO)
	}

	private fun verifyFullAccessRequiredChangedEvent(set: FullAccessRequired?, expected: FullAccessRequired = set!!) {
		val events = mutableListOf<FullAccessRequiredChanged>()
		eventBus.register(object {
			@Subscribe
			fun fullAccessRequiredChanged(event: FullAccessRequiredChanged) =
					events.add(event)
		})
		preferences.newFcpFullAccessRequired = set
		assertThat(preferences.fcpFullAccessRequired, equalTo(expected))
		assertThat(events.single().fullAccessRequired, equalTo(expected))
	}

	@Test
	fun `preferences retain fcp full access required of writing`() {
		verifyFullAccessRequiredChangedEvent(WRITING)
	}

	@Test
	fun `preferences retain fcp full access required of always`() {
		verifyFullAccessRequiredChangedEvent(ALWAYS)
	}

	@Test
	fun `preferences return default value when fcp full access required is set to null`() {
		verifyFullAccessRequiredChangedEvent(null, ALWAYS)
	}

	@Test
	fun `preferences start with fcp full access required default value`() {
		assertThat(preferences.fcpFullAccessRequired, equalTo(ALWAYS))
	}

	@Test
	fun `setting insertion delay to valid value sends change event`() {
		testPreferencesChangedEvent("InsertionDelay", { preferences.newInsertionDelay = it }, 30)
	}

	@Test
	fun `setting posts per page to valid value sends change event`() {
		testPreferencesChangedEvent("PostsPerPage", { preferences.newPostsPerPage = it }, 31)
	}

	@Test
	fun `default strict filtering is false`() {
		assertThat(preferences.strictFiltering, equalTo(false))
	}

	@Test
	fun `strict filtering can be set`() {
		preferences.newStrictFiltering = true
		assertThat(preferences.strictFiltering, equalTo(true))
	}

	@Test
	fun `strict filtering returns to default on null`() {
		preferences.newStrictFiltering = true
		preferences.newStrictFiltering = null
		assertThat(preferences.strictFiltering, equalTo(false))
	}

	@Test
	fun `event is generated when strict filtering is activated`() {
		val events = mutableListOf<StrictFilteringActivatedEvent>()
		eventBus.register(object {
			@Subscribe fun strictFilteringActivatedEvent(event: StrictFilteringActivatedEvent) =
					events.add(event)
		})
		preferences.newStrictFiltering = true
		assertThat(events, hasItem<StrictFilteringActivatedEvent>(instanceOf(StrictFilteringActivatedEvent::class.java)))
	}

	@Test
	fun `event is generated when strict filtering is deactivated`() {
		val events = mutableListOf<StrictFilteringDeactivatedEvent>()
		eventBus.register(object {
			@Subscribe fun strictFilteringDeactivatedEvent(event: StrictFilteringDeactivatedEvent) =
					events.add(event)
		})
		preferences.newStrictFiltering = false
		assertThat(events, hasItem<StrictFilteringDeactivatedEvent>(instanceOf(StrictFilteringDeactivatedEvent::class.java)))
	}

	@Test
	fun `default strict filtering is saved as null`() {
		verifySavedOption(nullValue()) { it.getBooleanValue("Option/StrictFiltering").value }
	}

	@Test
	fun `activated strict filtering is saved as true`() {
		preferences.newStrictFiltering = true
		verifySavedOption(equalTo(true)) { it.getBooleanValue("Option/StrictFiltering").value }
	}

	@Test
	fun `deactivated strict filtering is saved as false`() {
		preferences.newStrictFiltering = false
		verifySavedOption(equalTo(false)) { it.getBooleanValue("Option/StrictFiltering").value }
	}

	private fun <T> verifySavedOption(matcher: Matcher<T>, getter: (Configuration) -> T) {
		val configuration = Configuration(MapConfigurationBackend())
		preferences.saveTo(configuration)
		assertThat(getter(configuration), matcher)
	}

	private fun <T : Any> testPreferencesChangedEvent(name: String, setter: (T) -> Unit, value: T) {
		val events = mutableListOf<PreferenceChangedEvent>()
		eventBus.register(object {
			@Subscribe
			fun preferenceChanged(event: PreferenceChangedEvent) =
					events.add(event)
		})
		setter(value)
		assertThat(events, hasItem(PreferenceChangedEvent(name, value)))
	}

}
