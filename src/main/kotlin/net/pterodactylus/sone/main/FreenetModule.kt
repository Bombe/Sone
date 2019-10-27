package net.pterodactylus.sone.main

import com.google.inject.*
import freenet.client.*
import freenet.clients.http.*
import freenet.node.*
import freenet.pluginmanager.*
import net.pterodactylus.sone.freenet.plugin.*
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Guice [Module] that supplies some objects that are in fact supplied by the Freenet node.
 */
class FreenetModule(private val pluginRespirator: PluginRespirator) : Module {

	override fun configure(binder: Binder): Unit = binder.run {
		bind(PluginRespiratorFacade::class.java).toProvider(Provider { FredPluginRespiratorFacade(pluginRespirator) }).`in`(Singleton::class.java)
		bind(Node::class.java).toProvider(Provider { pluginRespirator.node })
		bind(HighLevelSimpleClient::class.java).toProvider(Provider<HighLevelSimpleClient> { pluginRespirator.hlSimpleClient!! })
		bind(ToadletContainer::class.java).toProvider(Provider<ToadletContainer> { pluginRespirator.toadletContainer })
		bind(PageMaker::class.java).toProvider(Provider<PageMaker> { pluginRespirator.pageMaker })
	}

	@Provides
	@Singleton
	fun getSessionManager() = pluginRespirator.getSessionManager("Sone")!!

}
