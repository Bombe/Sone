package net.pterodactylus.sone.main

import com.google.common.base.*
import com.google.common.eventbus.*
import com.google.inject.*
import com.google.inject.name.Names.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.database.memory.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.config.*
import net.pterodactylus.util.version.Version
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import java.io.*
import java.util.concurrent.atomic.*

class SoneModuleTest {

	private val currentDir: File = File(".")
	private val pluginVersion = Version("", 0, 1, 2)
	private val pluginYear = 2019
	private val pluginHomepage = "home://page"
	private val sonePlugin = mock<SonePlugin>().apply {
		whenever(version).thenReturn(pluginVersion.toString())
		whenever(year).thenReturn(pluginYear)
		whenever(homepage).thenReturn(pluginHomepage)
	}

	@After
	fun removePropertiesFromCurrentDirectory() {
		File(currentDir, "sone.properties").delete()
	}

	@Test
	fun `creator binds configuration when no file is present`() {
		File(currentDir, "sone.properties").delete()
		assertThat(getInstance<Configuration>(), notNullValue())
	}

	@Test
	fun `creator binds first start to true when no file is present`() {
		File(currentDir, "sone.properties").delete()
		assertThat(getInstance(named("FirstStart")), equalTo(true))
	}

	@Test
	fun `config file is created in current directory if not present`() {
		File(currentDir, "sone.properties").delete()
		val configuration = getInstance<Configuration>()
		configuration.save()
		assertThat(File(currentDir, "sone.properties").exists(), equalTo(true))
	}

	@Test
	fun `creator binds configuration when file is present`() {
		File(currentDir, "sone.properties").writeText("Option=old")
		assertThat(getInstance<Configuration>().getStringValue("Option").value, equalTo("old"))
	}

	@Test
	fun `creator binds first start to false when file is present`() {
		File(currentDir, "sone.properties").writeText("Option=old")
		assertThat(getInstance(named("FirstStart")), equalTo(false))
	}

	@Test
	fun `invalid config file leads to new config being created`() {
		File(currentDir, "sone.properties").writeText("Option=old\nbroken")
		val configuration = getInstance<Configuration>()
		assertThat(configuration.getStringValue("Option").getValue(null), nullValue())
	}

	@Test
	fun `invalid config file leads to new config being set to true`() {
		File(currentDir, "sone.properties").writeText("Option=old\nbroken")
		assertThat(getInstance(named("NewConfig")), equalTo(true))
	}

	@Test
	fun `valid config file leads to new config being set to false`() {
		File(currentDir, "sone.properties").writeText("Option=old")
		assertThat(getInstance(named("NewConfig")), equalTo(false))
	}

	@Test
	fun `event bus is bound`() {
		assertThat(getInstance<EventBus>(), notNullValue())
	}

	@Test
	fun `context is bound`() {
		assertThat(getInstance<Context>().context, equalTo("Sone"))
	}

	@Test
	fun `optional context is bound`() {
		assertThat(getInstance<Optional<Context>>().get().context, equalTo("Sone"))
	}

	@Test
	fun `sone plugin is bound`() {
		assertThat(getInstance(), sameInstance(sonePlugin))
	}

	@Test
	fun `version is bound`() {
		assertThat(getInstance(), equalTo(pluginVersion))
	}

	@Test
	fun `plugin version is bound`() {
		assertThat(getInstance(), equalTo(PluginVersion(pluginVersion.toString())))
	}

	@Test
	fun `plugin year is bound`() {
		assertThat(getInstance(), equalTo(PluginYear(pluginYear)))
	}

	@Test
	fun `plugin homepage in bound`() {
		assertThat(getInstance(), equalTo(PluginHomepage(pluginHomepage)))
	}

	@Test
	fun `database is bound correctly`() {
		assertThat(getInstance<Database>(), instanceOf(MemoryDatabase::class.java))
	}

	@Test
	fun `default loader is used without dev options`() {
		assertThat(getInstance<Loaders>(), instanceOf(DefaultLoaders::class.java))
	}

	@Test
	fun `default loaders are used if no path is given`() {
		File(currentDir, "sone.properties").writeText("Developer.LoadFromFilesystem=true")
		assertThat(getInstance<Loaders>(), instanceOf(DefaultLoaders::class.java))
	}

	@Test
	fun `debug loaders are used if path is given`() {
		File(currentDir, "sone.properties").writeText("Developer.LoadFromFilesystem=true\nDeveloper.FilesystemPath=/tmp")
		assertThat(getInstance<Loaders>(), instanceOf(DebugLoaders::class.java))
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
		val injector = createInjector()
		val eventBus: EventBus = getInstance(injector = injector)
		val testObject = getInstance<TestObject>(injector = injector)
		val event = Any()
		eventBus.post(event)
		assertThat(testObject.ref.get(), sameInstance(event))
	}

	private fun createInjector(): Injector = SoneModule(sonePlugin)
			.let { Guice.createInjector(it) }

	private inline fun <reified R : Any> getInstance(annotation: Annotation? = null, injector: Injector = createInjector()): R =
			annotation
					?.let { injector.getInstance(Key.get(object : TypeLiteral<R>() {}, it)) }
					?: injector.getInstance(Key.get(object : TypeLiteral<R>() {}))

}
