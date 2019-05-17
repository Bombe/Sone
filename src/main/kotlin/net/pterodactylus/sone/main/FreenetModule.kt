package net.pterodactylus.sone.main

import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Provides
import freenet.client.HighLevelSimpleClient
import freenet.clients.http.*
import freenet.node.Node
import freenet.pluginmanager.PluginRespirator
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Guice [Module] that supplies some objects that are in fact supplied by the Freenet node.
 */
class FreenetModule(private val pluginRespirator: PluginRespirator): Module {

	override fun configure(binder: Binder): Unit = binder.run {
		bind(PluginRespirator::class.java).toProvider(Provider<PluginRespirator> { pluginRespirator })
		pluginRespirator.node!!.let { node -> bind(Node::class.java).toProvider(Provider<Node> { node }) }
		bind(HighLevelSimpleClient::class.java).toProvider(Provider<HighLevelSimpleClient> { pluginRespirator.hlSimpleClient!! })
		bind(ToadletContainer::class.java).toProvider(Provider<ToadletContainer> { pluginRespirator.toadletContainer })
	}

	@Provides @Singleton
	fun getSessionManager() = pluginRespirator.getSessionManager("Sone")!!

}
