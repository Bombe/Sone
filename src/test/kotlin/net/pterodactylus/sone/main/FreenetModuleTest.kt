package net.pterodactylus.sone.main

import com.google.inject.Guice.createInjector
import com.google.inject.Injector
import com.google.inject.name.Names
import freenet.client.*
import freenet.client.async.ClientContext
import freenet.client.async.USKManager
import freenet.clients.http.*
import freenet.node.*
import freenet.pluginmanager.*
import java.io.File
import net.pterodactylus.sone.freenet.HighLevelSimpleClientCreator
import net.pterodactylus.sone.freenet.plugin.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.mockito.*
import org.mockito.Mockito.*

/**
 * Unit test for [FreenetModule].
 */
class FreenetModuleTest {

	private val sessionManager = mock<SessionManager>()
	private val uskManager = mock<USKManager>()
	private val clientContext = mock<ClientContext>()
	private val pluginRespirator = deepMock<PluginRespirator>().apply {
		whenever(getSessionManager("Sone")).thenReturn(sessionManager)
	}
	private val nodeClientCore = mock<NodeClientCore>().also {
		whenever(it.getUskManager()).thenReturn(uskManager)
		whenever(it.getClientContext()).thenReturn(clientContext)
	}
	private val node = pluginRespirator.node!!.also {
		whenever(it.getClientCore()).thenReturn(nodeClientCore)
	}
	private val highLevelSimpleClient = pluginRespirator.hlSimpleClient!!
	private val toadletContainer: ToadletContainer = pluginRespirator.toadletContainer
	private val pageMaker: PageMaker = pluginRespirator.pageMaker

	@Test
	fun `plugin respirator is returned correctly`() {
		assertThat(injector.getInstance(), sameInstance(pluginRespirator))
	}

	@Test
	fun `node is returned correctly`() {
		assertThat(injector.getInstance(), sameInstance(node))
	}

	@Test
	fun `node is returned as singleton`() {
		injector.verifySingletonInstance<Node>()
	}

	@Test
	fun `high level simply client is returned correctly`() {
		assertThat(injector.getInstance(), sameInstance(highLevelSimpleClient))
	}

	@Test
	fun `high level simply client is returned as singleton`() {
		injector.verifySingletonInstance<HighLevelSimpleClient>()
	}

	@Test
	fun `session manager is returned correctly`() {
		assertThat(injector.getInstance(), sameInstance(sessionManager))
	}

	@Test
	fun `session manager is returned as singleton`() {
		injector.verifySingletonInstance<SessionManager>()
		verify(pluginRespirator).getSessionManager("Sone")
	}

	@Test
	fun `toadlet container is returned correctly`() {
		assertThat(injector.getInstance(), sameInstance(toadletContainer))
	}

	@Test
	fun `toadlet container is returned as singleten`() {
		injector.verifySingletonInstance<ToadletContainer>()
	}

	@Test
	fun `page maker is returned correctly`() {
		assertThat(injector.getInstance(), sameInstance(pageMaker))
	}

	@Test
	fun `page maker is returned as singleton`() {
		injector.verifySingletonInstance<PageMaker>()
	}

	@Test
	@Suppress("DEPRECATION")
	fun `plugin respirator facade is returned correctly`() {
		val pluginRespiratorFacade = injector.getInstance<PluginRespiratorFacade>()
		pluginRespiratorFacade.getPluginTalker(mock(), "test.plugin", "test-request-1")
		verify(pluginRespirator).getPluginTalker(any(), ArgumentMatchers.eq("test.plugin"), ArgumentMatchers.eq("test-request-1"))
	}

	@Test
	fun `plugin respirator facade is returned as singleton`() {
		injector.verifySingletonInstance<PluginRespiratorFacade>()
	}

	@Test
	fun `plugin connector is returned correctly`() {
		assertThat(injector.getInstance<PluginConnector>(), notNullValue())
	}

	@Test
	fun `plugin connector facade is returned as singleton`() {
		injector.verifySingletonInstance<PluginConnector>()
	}

	@Test
	fun `high level simple client creator is returned correctly`() {
		assertThat(injector.getInstance<HighLevelSimpleClientCreator>(), notNullValue());
	}

	@Test
	fun `client context is returned correctly`() {
		assertThat(injector.getInstance<ClientContext>(), sameInstance(clientContext))
	}

	@Test
	fun `usk manager is returned correctly`() {
		assertThat(injector.getInstance<USKManager>(), sameInstance(uskManager))
	}

	@Test
	fun `node user dir is returned correctly`() {
		assertThat(createInjector("/node/user-dir").getInstance<String>(Names.named("NodeUserDir")), equalTo("/node/user-dir"))
	}

	private fun createInjector(databasePath: String = tempFolder.newFolder().path): Injector {
		whenever(node.userDir).thenReturn(File(databasePath))
		return createInjector(FreenetModule(pluginRespirator))
	}

	@Rule
	@JvmField
	val tempFolder = TemporaryFolder()

	private val injector by lazy { createInjector() }

}
