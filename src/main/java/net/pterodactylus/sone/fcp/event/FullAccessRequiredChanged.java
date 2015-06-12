package net.pterodactylus.sone.fcp.event;

import net.pterodactylus.sone.fcp.FcpInterface;
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired;

/**
 * Event that signals that the {@link FcpInterface}’s {@link
 * FullAccessRequired} parameter was changed in the configuration.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FullAccessRequiredChanged {

	private final FullAccessRequired fullAccessRequired;

	public FullAccessRequiredChanged(FullAccessRequired fullAccessRequired) {
		this.fullAccessRequired = fullAccessRequired;
	}

	public FullAccessRequired getFullAccessRequired() {
		return fullAccessRequired;
	}

}
