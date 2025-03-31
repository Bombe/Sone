package net.pterodactylus.sone.main

import com.google.inject.*
import freenet.client.*
import freenet.client.async.ClientContext
import freenet.client.async.USKManager
import freenet.clients.http.*
import freenet.node.*
import freenet.pluginmanager.*
import net.pterodactylus.sone.freenet.DefaultHighLevelSimpleClientCreator
import net.pterodactylus.sone.freenet.HighLevelSimpleClientCreator
import net.pterodactylus.sone.freenet.plugin.*
import jakarta.inject.Provider
import jakarta.inject.Singleton

/**
 * Guice [Module] that supplies some objects that are in fact supplied by the Freenet node.
 */
class FreenetModule(private val pluginRespirator: PluginRespirator) : Module {

	override fun configure(binder: Binder): Unit = binder.run {
		bind(PluginRespirator::class.java).toInstance(pluginRespirator)
		bind(PluginRespiratorFacade::class.java).toInstance(FredPluginRespiratorFacade(pluginRespirator))
		bind(PluginConnector::class.java).to(FredPluginConnector::class.java).`in`(Singleton::class.java)
		bind(Node::class.java).toInstance(pluginRespirator.node)
		bind(HighLevelSimpleClient::class.java).toInstance(pluginRespirator.hlSimpleClient)
		bind(ToadletContainer::class.java).toInstance(pluginRespirator.toadletContainer)
		bind(PageMaker::class.java).toInstance(pluginRespirator.pageMaker)
	}

	@Provides
	@Singleton
	fun getSessionManager() = pluginRespirator.getSessionManager("Sone")!!

	@Provides
	fun getNodeClientCore(node: Node): NodeClientCore =
		node.getClientCore()

	@Provides
	fun getHighLevelSimpleClientCreator(nodeClientCore: NodeClientCore): HighLevelSimpleClientCreator =
		DefaultHighLevelSimpleClientCreator(nodeClientCore)

	@Provides
	fun getClientContext(nodeClientCore: NodeClientCore): ClientContext =
		nodeClientCore.getClientContext()

	@Provides
	fun getUskManager(nodeClientCore: NodeClientCore): USKManager =
		nodeClientCore.getUskManager()

}
