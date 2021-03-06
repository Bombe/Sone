package net.pterodactylus.sone.freenet.wot

import net.pterodactylus.sone.freenet.plugin.*

/**
 * Connector for the web of trust plugin.
 */
interface WebOfTrustConnector {

	/**
	 * Loads all own identities from the Web of Trust plugin.
	 *
	 * @return All own identity
	 * @throws WebOfTrustException if the own identities can not be loaded
	 */
	@Throws(WebOfTrustException::class)
	fun loadAllOwnIdentities(): Set<OwnIdentity>

	/**
	 * Loads all identities that the given identities trusts with a score of
	 * more than 0 and the (optional) given context.
	 *
	 * @param ownIdentity The own identity
	 * @param context The context to filter, or `null`
	 * @return All trusted identities
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	fun loadTrustedIdentities(ownIdentity: OwnIdentity, context: String? = null): Set<Identity>

	/**
	 * Loads all identities known to the given own identity that have the (optional) given context.
	 *
	 * @param ownIdentity The own identity
	 * @param context The context to filter, or `null`
	 * @return All trusted identities
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	fun loadAllIdentities(ownIdentity: OwnIdentity, context: String? = null): Set<Identity>

	/**
	 * Adds the given context to the given identity.
	 *
	 * @param ownIdentity The identity to add the context to
	 * @param context The context to add
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	fun addContext(ownIdentity: OwnIdentity, context: String)

	/**
	 * Removes the given context from the given identity.
	 *
	 * @param ownIdentity The identity to remove the context from
	 * @param context The context to remove
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	fun removeContext(ownIdentity: OwnIdentity, context: String)

	/**
	 * Sets the property with the given name to the given value.
	 *
	 * @param ownIdentity The identity to set the property on
	 * @param name The name of the property to set
	 * @param value The value to set
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	fun setProperty(ownIdentity: OwnIdentity, name: String, value: String)

	/**
	 * Removes the property with the given name.
	 *
	 * @param ownIdentity The identity to remove the property from
	 * @param name The name of the property to remove
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	fun removeProperty(ownIdentity: OwnIdentity, name: String)

	/**
	 * Pings the Web of Trust plugin. If the plugin can not be reached, a
	 * [PluginException] is thrown.
	 *
	 * @throws PluginException if the plugin is not loaded
	 */
	@Throws(PluginException::class)
	fun ping()

	/**
	 * Stops the web of trust connector.
	 */
	fun stop() = Unit

}
