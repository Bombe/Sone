package net.pterodactylus.sone.main

import com.google.common.eventbus.*
import com.google.inject.*
import freenet.client.async.*
import freenet.l10n.BaseL10n.LANGUAGE.*
import freenet.node.*
import freenet.pluginmanager.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.fcp.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.notification.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.mockito.Mockito.*
import java.io.*
import java.util.concurrent.atomic.*
import kotlin.test.*

/**
 * Unit test for [SonePlugin].
 */
@Dirty
class SonePluginTest {

	private val sonePlugin by lazy { SonePlugin { injector } }
	private val pluginRespirator = deepMock<PluginRespirator>()
	private val node = deepMock<Node>()
	private val clientCore = deepMock<NodeClientCore>()
	private val uskManager = deepMock<USKManager>()

	init {
		setField(node, "clientCore", clientCore)
		whenever(pluginRespirator.node).thenReturn(node)
		setField(clientCore, "uskManager", uskManager)
	}

	@Test
	fun `sone plugin can be started`() {
		sonePlugin.setLanguage(ENGLISH)
		sonePlugin.runPlugin(pluginRespirator)
	}

	@Test
	fun `core can be created`() {
		val injector: Injector = runSonePluginWithRealInjector()
		assertThat(injector.getInstance<Core>(), notNullValue())
	}

	@Test
	fun `fcp interface can be created`() {
		val injector: Injector = runSonePluginWithRealInjector()
		assertThat(injector.getInstance<FcpInterface>(), notNullValue())
	}

	@Test
	fun `web interface can be created`() {
		val injector: Injector = runSonePluginWithRealInjector()
		assertThat(injector.getInstance<WebInterface>(), notNullValue())
	}

	@Test
	fun `web of trust connector can be created`() {
		val injector: Injector = runSonePluginWithRealInjector()
		assertThat(injector.getInstance<WebOfTrustConnector>(), notNullValue())
	}

	@Test
	fun `notification handler can be created`() {
		val injector: Injector = runSonePluginWithRealInjector()
		assertThat(injector.getInstance<NotificationHandler>(), notNullValue())
	}

	private fun runSonePluginWithRealInjector(injectorConsumer: (Injector) -> Unit = {}): Injector {
		lateinit var injector: Injector
		val sonePlugin = SonePlugin {
			Guice.createInjector(*it).also {
				injector = it
				injectorConsumer(it)
			}
		}
		sonePlugin.setLanguage(ENGLISH)
		sonePlugin.runPlugin(pluginRespirator)
		return injector
	}

	@Test
	fun `core is being started`() {
		sonePlugin.runPlugin(pluginRespirator)
		val core = injector.getInstance<Core>()
		verify(core).start()
	}

	@Test
	fun `notification handler is being requested`() {
		sonePlugin.runPlugin(pluginRespirator)
		assertThat(getInjected(NotificationHandler::class.java), notNullValue())
	}

	@Test
	fun `first-start event is sent to event bus when first start is true`() {
		File("sone.properties").delete()
		val firstStartReceived = AtomicBoolean()
		runSonePluginWithRealInjector {
			val eventBus = it.getInstance(EventBus::class.java)
			eventBus.register(object : Any() {
				@Subscribe
				fun firstStart(firstStart: FirstStart) {
					firstStartReceived.set(true)
				}
			})
		}
		sonePlugin.runPlugin(pluginRespirator)
		assertThat(firstStartReceived.get(), equalTo(true))
	}

	@Test
	fun `first-start event is not sent to event bus when first start is false`() {
		File("sone.properties").deleteAfter {
			writeText("# empty")
			val firstStartReceived = AtomicBoolean()
			runSonePluginWithRealInjector {
				val eventBus = it.getInstance(EventBus::class.java)
				eventBus.register(object : Any() {
					@Subscribe
					fun firstStart(firstStart: FirstStart) {
						firstStartReceived.set(true)
					}
				})
			}
			sonePlugin.runPlugin(pluginRespirator)
			assertThat(firstStartReceived.get(), equalTo(false))
		}
	}

	private fun <T> getInjected(clazz: Class<T>, annotation: Annotation? = null): T? =
			injected[TypeLiteral.get(clazz) to annotation] as? T

	private val injected =
			mutableMapOf<Pair<TypeLiteral<*>, Annotation?>, Any>()

	private val injector = mock<Injector>().apply {
		fun mockValue(clazz: Class<*>) = false.takeIf { clazz.name == java.lang.Boolean::class.java.name } ?: mock(clazz)
		whenever(getInstance(any<Key<*>>())).then {
			injected.getOrPut((it.getArgument(0) as Key<*>).let { it.typeLiteral to it.annotation }) {
				it.getArgument<Key<*>>(0).typeLiteral.type.typeName.toClass().let(::mockValue)
			}
		}
		whenever(getInstance(any<Class<*>>())).then {
			injected.getOrPut(TypeLiteral.get(it.getArgument(0) as Class<*>) to null) {
				it.getArgument<Class<*>>(0).let(::mockValue)
			}
		}
	}

}

private fun String.toClass(): Class<*> = SonePlugin::class.java.classLoader.loadClass(this)

private fun File.deleteAfter(action: File.() -> Unit) = try {
	action(this)
} finally {
	this.delete()
}
