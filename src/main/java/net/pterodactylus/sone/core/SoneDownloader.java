package net.pterodactylus.sone.core;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.service.Service;

import freenet.keys.FreenetURI;

import com.google.inject.ImplementedBy;

/**
 * Downloads and parses Sone and {@link Core#updateSone(Sone) updates the
 * core}.
 */
@ImplementedBy(SoneDownloaderImpl.class)
public interface SoneDownloader extends Service {

	void addSone(Sone sone);
	Sone fetchSone(Sone sone, FreenetURI soneUri, boolean fetchOnly);

	Runnable fetchSoneAsUskAction(Sone sone);
	Runnable fetchSoneAsSskAction(Sone sone);

}
