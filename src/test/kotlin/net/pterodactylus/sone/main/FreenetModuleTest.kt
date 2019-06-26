package net.pterodactylus.sone.main

import com.google.inject.*
import freenet.client.*
import freenet.clients.http.*
import freenet.node.*
import freenet.pluginmanager.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

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
	private val pageMaker: PageMaker = pluginRespirator.pageMaker
	private val module = FreenetModule(pluginRespirator)
	private val injector = Guice.createInjector(module)

	private inline fun <reified T : Any> verifySingletonInstance() {
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

	@Test
	fun `page maker is returned correctly`() {
		assertThat(injector.getInstance(), sameInstance(pageMaker))
	}

	@Test
	fun `page maker is returned as singleten`() {
		verifySingletonInstance<PageMaker>()
	}

}
