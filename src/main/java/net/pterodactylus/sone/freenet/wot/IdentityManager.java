package net.pterodactylus.sone.freenet.wot;

import java.util.Set;

import net.pterodactylus.util.service.Service;

import com.google.common.eventbus.EventBus;

/**
 * Connects to a {@link WebOfTrustConnector} and sends identity events to an
 * {@link EventBus}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface IdentityManager extends Service {

	boolean isConnected();
	Set<OwnIdentity> getAllOwnIdentities();

}
