package net.pterodactylus.sone.freenet

import freenet.keys.*
import freenet.support.Base64.*

val FreenetURI.routingKeyString: String get() = encode(routingKey)
