package net.pterodactylus.sone.freenet

import freenet.keys.*
import net.pterodactylus.sone.utils.*

val FreenetURI.routingKeyString: String get() = routingKey.asFreenetBase64
