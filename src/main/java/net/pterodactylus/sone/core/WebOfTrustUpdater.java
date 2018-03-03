package net.pterodactylus.sone.core;

import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.util.service.Service;

import com.google.inject.ImplementedBy;

/**
 * Updates WebOfTrust identity data.
 */
@ImplementedBy(WebOfTrustUpdaterImpl.class)
public interface WebOfTrustUpdater extends Service {

	void setTrust(OwnIdentity truster, Identity trustee, Integer score, String comment);
	boolean addContextWait(OwnIdentity ownIdentity, String context);
	void removeContext(OwnIdentity ownIdentity, String context);
	void setProperty(OwnIdentity ownIdentity, String propertyName, String propertyValue);
	void removeProperty(OwnIdentity ownIdentity, String propertyName);

}
