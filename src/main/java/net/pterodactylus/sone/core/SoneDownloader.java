package net.pterodactylus.sone.core;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.service.Service;

import freenet.keys.FreenetURI;

/**
 * Downloads and parses Sone and {@link Core#updateSone(Sone) updates the
 * core}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface SoneDownloader extends Service {

	void addSone(Sone sone);
	void fetchSone(Sone sone);
	void fetchSone(Sone sone, FreenetURI soneUri);
	Sone fetchSone(Sone sone, FreenetURI soneUri, boolean fetchOnly);

	Runnable fetchSoneWithUriAction(Sone sone);
	Runnable fetchSoneAction(Sone sone);

}
