package net.pterodactylus.sone.freenet

import freenet.client.HighLevelSimpleClient
import freenet.node.NodeClientCore

fun interface HighLevelSimpleClientCreator {

	fun makeClient(prioClass: Short, forceDontIgnoreTooManyPathComponents: Boolean, realTimeFlag: Boolean): HighLevelSimpleClient

}

class DefaultHighLevelSimpleClientCreator(private val nodeClientCore: NodeClientCore) : HighLevelSimpleClientCreator {

	override fun makeClient(prioClass: Short, forceDontIgnoreTooManyPathComponents: Boolean, realTimeFlag: Boolean): HighLevelSimpleClient =
		nodeClientCore.makeClient(prioClass, forceDontIgnoreTooManyPathComponents, realTimeFlag)

}
