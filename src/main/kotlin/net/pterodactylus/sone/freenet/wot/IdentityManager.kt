package net.pterodactylus.sone.freenet.wot

import net.pterodactylus.util.service.Service

import com.google.common.eventbus.EventBus
import com.google.inject.ImplementedBy

/**
 * Connects to a [WebOfTrustConnector] and sends identity events to an
 * [EventBus].
 */
@ImplementedBy(IdentityManagerImpl::class)
interface IdentityManager : Service {

	val isConnected: Boolean
	val allOwnIdentities: Set<OwnIdentity>

}
