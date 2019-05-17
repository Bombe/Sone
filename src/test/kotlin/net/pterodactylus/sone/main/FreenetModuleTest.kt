package net.pterodactylus.sone.main

import com.google.inject.Guice
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.*
import freenet.node.Node
import freenet.pluginmanager.PluginRespirator
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.sameInstance
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [FreenetModule].
 */
class FreenetModuleTest {

	private val sessionManager = mock<SessionManager>()
	private val pluginRespirator = deepMock<PluginRespirator>().apply {
		whenever(getSessionManager("Sone")).thenReturn(sessionManager)
	}
	private val node = pluginRespirator.node!!
	private val highLevelSimpleClient = pluginRespirator.hlSimpleClient!!
	private val toadletContainer: ToadletContainer = pluginRespirator.toadletContainer
	private val module = FreenetModule(pluginRespirator)
	private val injector = Guice.createInjector(module)

	private inline fun <reified T: Any> verifySingletonInstance() {
		val firstInstance = injector.getInstance<T>()
		val secondInstance = injector.getInstance<T>()
		assertThat(firstInstance, sameInstance(secondInstance))
	}

	@Test
	fun `plugin respirator is returned correctly`() {
		assertThat(injector.getInstance(), sameInstance(pluginRespirator))
	}

	@Test
	fun `plugin respirator is returned as singleton`() {
		verifySingletonInstance<PluginRespirator>()
	}

	@Test
	fun `node is returned correctly`() {
		assertThat(injector.getInstance(), sameInstance(node))
	}

	@Test
	fun `node is returned as singleton`() {
		verifySingletonInstance<Node>()
	}

	@Test
	fun `high level simply client is returned correctly`() {
		assertThat(injector.getInstance(), sameInstance(highLevelSimpleClient))
	}

	@Test
	fun `high level simply client is returned as singleton`() {
		verifySingletonInstance<HighLevelSimpleClient>()
	}

	@Test
	fun `session manager is returned correctly`() {
		assertThat(injector.getInstance(), sameInstance(sessionManager))
	}

	@Test
	fun `session manager is returned as singleton`() {
		verifySingletonInstance<SessionManager>()
		verify(pluginRespirator).getSessionManager("Sone")
	}

	@Test
	fun `toadlet container is returned correctly`() {
	    assertThat(injector.getInstance(), sameInstance(toadletContainer))
	}

	@Test
	fun `toadlet container is returned as singleten`() {
		verifySingletonInstance<ToadletContainer>()
	}

}
