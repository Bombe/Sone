package net.pterodactylus.sone.freenet.wot

val dummyWebOfTrustConnector = object : WebOfTrustConnector {
	override fun loadAllOwnIdentities(): Set<OwnIdentity> = emptySet()
	override fun loadTrustedIdentities(ownIdentity: OwnIdentity, context: String?): Set<Identity> = emptySet()
	override fun loadAllIdentities(ownIdentity: OwnIdentity, context: String?): Set<Identity> = emptySet()
	override fun addContext(ownIdentity: OwnIdentity, context: String) = Unit
	override fun removeContext(ownIdentity: OwnIdentity, context: String) = Unit
	override fun setProperty(ownIdentity: OwnIdentity, name: String, value: String) = Unit
	override fun removeProperty(ownIdentity: OwnIdentity, name: String) = Unit
	override fun ping() = Unit
}

open class DelegatingWebOfTrustConnector(private val delegate: WebOfTrustConnector) : WebOfTrustConnector by delegate

fun WebOfTrustConnector.overridePing(override: () -> Unit): WebOfTrustConnector = object : DelegatingWebOfTrustConnector(this) {
	override fun ping() = override()
}
