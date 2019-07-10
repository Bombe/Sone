package net.pterodactylus.sone.main

import com.google.inject.*
import freenet.client.async.*
import freenet.l10n.BaseL10n.LANGUAGE.*
import freenet.node.*
import freenet.pluginmanager.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.fcp.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.mockito.Mockito.*
import kotlin.test.*

/**
 * Unit test for [SonePlugin].
 */
@Dirty
class SonePluginTest {

	private var injector = mockInjector()
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

	private fun runSonePluginWithRealInjector(): Injector {
		lateinit var injector: Injector
		val sonePlugin = SonePlugin {
			Guice.createInjector(*it).also {
				injector = it
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

}

private fun mockInjector() = mock<Injector>().apply {
	val injected = mutableMapOf<Pair<TypeLiteral<*>, Annotation?>, Any>()
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

private fun String.toClass(): Class<*> = SonePlugin::class.java.classLoader.loadClass(this)
