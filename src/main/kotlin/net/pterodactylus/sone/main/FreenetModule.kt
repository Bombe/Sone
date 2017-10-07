package net.pterodactylus.sone.main

import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Provides
import freenet.client.HighLevelSimpleClient
import freenet.node.Node
import freenet.pluginmanager.PluginRespirator
import javax.inject.Singleton

/**
 * Guice [Module] that supplies some objects that are in fact supplied by the Freenet node.
 */
class FreenetModule(private val pluginRespirator: PluginRespirator): Module {

	override fun configure(binder: Binder): Unit = binder.run {
		bind(PluginRespirator::class.java).toProvider { pluginRespirator }
		pluginRespirator.node!!.let { node -> bind(Node::class.java).toProvider { node } }
		bind(HighLevelSimpleClient::class.java).toProvider { pluginRespirator.hlSimpleClient!! }
	}

	@Provides @Singleton
	fun getSessionManager() = pluginRespirator.getSessionManager("Sone")!!

}
