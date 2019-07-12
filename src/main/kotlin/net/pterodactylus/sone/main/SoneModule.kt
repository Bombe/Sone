package net.pterodactylus.sone.main

import com.google.common.base.*
import com.google.common.eventbus.*
import com.google.inject.*
import com.google.inject.matcher.*
import com.google.inject.name.Names.*
import com.google.inject.spi.*
import freenet.l10n.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.database.memory.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.util.config.*
import net.pterodactylus.util.config.ConfigurationException
import net.pterodactylus.util.version.Version
import java.io.*

class SoneModule(private val sonePlugin: SonePlugin) : AbstractModule() {

	override fun configure() {
		val sonePropertiesFile = File("sone.properties")
		val firstStart = !sonePropertiesFile.exists()
		var newConfig = false
		val configuration = try {
			Configuration(MapConfigurationBackend(sonePropertiesFile, false))
		} catch (ce: ConfigurationException) {
			sonePropertiesFile.delete()
			newConfig = true
			Configuration(MapConfigurationBackend(sonePropertiesFile, true))
		}
		val context = Context("Sone")
		val loaders = configuration.getStringValue("Developer.LoadFromFilesystem")
				.getValue(null)
				?.let {
					configuration.getStringValue("Developer.FilesystemPath")
							.getValue(null)
							?.let { DebugLoaders(it) }
				}
		val eventBus = EventBus()

		bind(Configuration::class.java).toInstance(configuration)
		bind(EventBus::class.java).toInstance(eventBus)
		bind(Boolean::class.java).annotatedWith(named("FirstStart")).toInstance(firstStart)
		bind(Boolean::class.java).annotatedWith(named("NewConfig")).toInstance(newConfig)
		bind(Context::class.java).toInstance(context)
		bind(object : TypeLiteral<Optional<Context>>() {}).toInstance(Optional.of(context))
		bind(SonePlugin::class.java).toInstance(sonePlugin)
		bind(Version::class.java).toInstance(sonePlugin.version.drop(1).parseVersion())
		bind(PluginVersion::class.java).toInstance(PluginVersion(sonePlugin.version))
		bind(PluginYear::class.java).toInstance(PluginYear(sonePlugin.year))
		bind(PluginHomepage::class.java).toInstance(PluginHomepage(sonePlugin.homepage))
		bind(Database::class.java).to(MemoryDatabase::class.java).`in`(Singleton::class.java)
		bind(BaseL10n::class.java).toInstance(sonePlugin.l10n().base)
		loaders?.let { bind(Loaders::class.java).toInstance(it) }

		bindListener(Matchers.any(), object : TypeListener {
			override fun <I> hear(typeLiteral: TypeLiteral<I>, typeEncounter: TypeEncounter<I>) {
				typeEncounter.register(InjectionListener { injectee -> eventBus.register(injectee) })
			}
		})
	}

	@Provides
	@Singleton
	fun getCore(configuration: Configuration, freenetInterface: FreenetInterface, identityManager: IdentityManager, soneDownloader: SoneDownloader, imageInserter: ImageInserter, updateChecker: UpdateChecker, webOfTrustUpdater: WebOfTrustUpdater, eventBus: EventBus, database: Database) =
			Core(configuration, freenetInterface, identityManager, soneDownloader, imageInserter, updateChecker, webOfTrustUpdater, eventBus, database).apply {
				debugInformation.showVersionInformation = configuration.getBooleanValue("Debug/ShowVersionInformation").getValue(false)
			}

}

private fun String.parseVersion(): Version = Version.parse(this)
