package net.pterodactylus.sone.main

import com.google.common.base.*
import com.google.common.eventbus.*
import com.google.inject.Guice.*
import com.google.inject.name.Names.*
import freenet.l10n.*
import freenet.node.*
import freenet.pluginmanager.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.database.memory.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.config.*
import net.pterodactylus.util.version.Version
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.mockito.Mockito.*
import java.io.*
import java.util.concurrent.atomic.*
import kotlin.test.*

const val versionString = "v80"

class SoneModuleTest {

	private val currentDir: File = File(".")
	private val pluginVersion = Version("", 80)
	private val pluginYear = 2019
	private val pluginHomepage = "home://page"
	private val l10n = deepMock<PluginL10n>()
	private val sonePlugin = mock<SonePlugin>().apply {
		whenever(version).thenReturn(versionString)
		whenever(year).thenReturn(pluginYear)
		whenever(homepage).thenReturn(pluginHomepage)
		whenever(l10n()).thenReturn(l10n)
	}

	private val injector by lazy {
		createInjector(
				SoneModule(sonePlugin, EventBus()),
				FreenetInterface::class.isProvidedByDeepMock(),
				PluginRespirator::class.isProvidedByDeepMock()
		)
	}

	@AfterTest
	fun removePropertiesFromCurrentDirectory() {
		File(currentDir, "sone.properties").delete()
	}

	@Test
	fun `creator binds configuration when no file is present`() {
		File(currentDir, "sone.properties").delete()
		assertThat(injector.getInstance<Configuration>(), notNullValue())
	}

	@Test
	fun `creator binds first start to true when no file is present`() {
		File(currentDir, "sone.properties").delete()
		assertThat(injector.getInstance(named("FirstStart")), equalTo(true))
	}

	@Test
	fun `config file is created in current directory if not present`() {
		File(currentDir, "sone.properties").delete()
		val configuration = injector.getInstance<Configuration>()
		configuration.save()
		assertThat(File(currentDir, "sone.properties").exists(), equalTo(true))
	}

	@Test
	fun `creator binds configuration when file is present`() {
		File(currentDir, "sone.properties").writeText("Option=old")
		assertThat(injector.getInstance<Configuration>().getStringValue("Option").value, equalTo("old"))
	}

	@Test
	fun `creator binds first start to false when file is present`() {
		File(currentDir, "sone.properties").writeText("Option=old")
		assertThat(injector.getInstance(named("FirstStart")), equalTo(false))
	}

	@Test
	fun `invalid config file leads to new config being created`() {
		File(currentDir, "sone.properties").writeText("Option=old\nbroken")
		val configuration = injector.getInstance<Configuration>()
		assertThat(configuration.getStringValue("Option").getValue(null), nullValue())
	}

	@Test
	fun `invalid config file leads to new config being set to true`() {
		File(currentDir, "sone.properties").writeText("Option=old\nbroken")
		assertThat(injector.getInstance(named("NewConfig")), equalTo(true))
	}

	@Test
	fun `valid config file leads to new config being set to false`() {
		File(currentDir, "sone.properties").writeText("Option=old")
		assertThat(injector.getInstance(named("NewConfig")), equalTo(false))
	}

	@Test
	fun `debug information flag is read from config`() {
		File(currentDir, "sone.properties").writeText("Debug/ShowVersionInformation=true")
		assertThat(injector.getInstance<Core>().debugInformation.showVersionInformation, equalTo(true))
	}

	@Test
	fun `event bus is bound`() {
		assertThat(injector.getInstance<EventBus>(), notNullValue())
	}

	@Test
	fun `context is bound`() {
		assertThat(injector.getInstance<Context>().context, equalTo("Sone"))
	}

	@Test
	fun `optional context is bound`() {
		assertThat(injector.getInstance<Optional<Context>>().get().context, equalTo("Sone"))
	}

	@Test
	fun `sone plugin is bound`() {
		assertThat(injector.getInstance(), sameInstance(sonePlugin))
	}

	@Test
	fun `version is bound`() {
		assertThat(injector.getInstance(), equalTo(pluginVersion))
	}

	@Test
	fun `plugin version is bound`() {
		assertThat(injector.getInstance(), equalTo(PluginVersion(versionString)))
	}

	@Test
	fun `plugin year is bound`() {
		assertThat(injector.getInstance(), equalTo(PluginYear(pluginYear)))
	}

	@Test
	fun `plugin homepage in bound`() {
		assertThat(injector.getInstance(), equalTo(PluginHomepage(pluginHomepage)))
	}

	@Test
	fun `database is bound correctly`() {
		assertThat(injector.getInstance<Database>(), instanceOf(MemoryDatabase::class.java))
	}

	@Test
	fun `base l10n is bound correctly`() {
		assertThat(injector.getInstance(), sameInstance(l10n.base))
	}

	@Test
	fun `default loader is used without dev options`() {
		assertThat(injector.getInstance<Loaders>(), instanceOf(DefaultLoaders::class.java))
	}

	@Test
	fun `default loaders are used if no path is given`() {
		File(currentDir, "sone.properties").writeText("Developer.LoadFromFilesystem=true")
		assertThat(injector.getInstance<Loaders>(), instanceOf(DefaultLoaders::class.java))
	}

	@Test
	fun `debug loaders are used if path is given`() {
		File(currentDir, "sone.properties").writeText("Developer.LoadFromFilesystem=true\nDeveloper.FilesystemPath=/tmp")
		assertThat(injector.getInstance<Loaders>(), instanceOf(DebugLoaders::class.java))
	}

	class TestObject {
		val ref: AtomicReference<Any?> = AtomicReference()
		@Subscribe
		fun testEvent(event: Any?) {
			ref.set(event)
		}
	}

	@Test
	fun `created objects are registered with event bus`() {
		val eventBus: EventBus = injector.getInstance()
		val testObject = injector.getInstance<TestObject>()
		val event = Any()
		eventBus.post(event)
		assertThat(testObject.ref.get(), sameInstance(event))
	}

	@Test
	fun `core is created as singleton`() {
		val firstCore = injector.getInstance<Core>()
		val secondCore = injector.getInstance<Core>()
		assertThat(secondCore, sameInstance(firstCore))
	}

	@Test
	fun `core is registered with event bus`() {
		val eventBus = mock<EventBus>()
		val injector = createInjector(
				SoneModule(sonePlugin, eventBus),
				FreenetInterface::class.isProvidedByDeepMock(),
				PluginRespirator::class.isProvidedByDeepMock()
		)
		val core = injector.getInstance<Core>()
		verify(eventBus).register(core)
	}

}
