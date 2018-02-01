package net.pterodactylus.sone.core;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.service.Service;

import freenet.keys.FreenetURI;

import com.google.inject.ImplementedBy;

/**
 * Downloads and parses Sone and {@link Core#updateSone(Sone) updates the
 * core}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
@ImplementedBy(SoneDownloaderImpl.class)
public interface SoneDownloader extends Service {

	void addSone(Sone sone);
	void fetchSone(Sone sone, FreenetURI soneUri);
	Sone fetchSone(Sone sone, FreenetURI soneUri, boolean fetchOnly);

	Runnable fetchSoneWithUriAction(Sone sone);
	Runnable fetchSoneAction(Sone sone);

}
