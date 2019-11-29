package net.pterodactylus.sone.core

import com.google.common.eventbus.*
import net.pterodactylus.sone.fcp.FcpInterface.*
import net.pterodactylus.util.config.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

/**
 * Unit test for [PreferencesLoader].
 */
class PreferencesLoaderTest {

	@Suppress("UnstableApiUsage")
	private val eventBus = EventBus()
	private val preferences = Preferences(eventBus)
	private val configuration = Configuration(MapConfigurationBackend())
	private val preferencesLoader = PreferencesLoader(preferences)

	@Before
	fun setupConfiguration() {
		setupIntValue("InsertionDelay", 15)
		setupIntValue("PostsPerPage", 25)
		setupIntValue("ImagesPerPage", 12)
		setupIntValue("CharactersPerPost", 150)
		setupIntValue("PostCutOffLength", 300)
		setupBooleanValue("RequireFullAccess", true)
		setupBooleanValue("ActivateFcpInterface", true)
		setupIntValue("FcpFullAccessRequired", 1)
	}

	private fun setupIntValue(optionName: String, value: Int) {
		configuration.getIntValue("Option/$optionName").value = value
	}

	private fun setupBooleanValue(optionName: String, value: Boolean) {
		configuration.getBooleanValue("Option/$optionName").value = value
	}

	@Test
	fun `configuration is loaded correctly`() {
		preferencesLoader.loadFrom(configuration)
		assertThat(preferences.insertionDelay, equalTo(15))
		assertThat(preferences.postsPerPage, equalTo(25))
		assertThat(preferences.imagesPerPage, equalTo(12))
		assertThat(preferences.charactersPerPost, equalTo(150))
		assertThat(preferences.postCutOffLength, equalTo(300))
		assertThat(preferences.requireFullAccess, equalTo(true))
		assertThat(preferences.fcpInterfaceActive, equalTo(true))
		assertThat(preferences.fcpFullAccessRequired, equalTo(FullAccessRequired.WRITING))
	}

	@Test
	fun `configuration is loaded correctly with cut off length minus one`() {
		setupIntValue("PostCutOffLength", -1)
		preferencesLoader.loadFrom(configuration)
		assertThat(preferences.postCutOffLength, not(equalTo(-1)))
	}

}
