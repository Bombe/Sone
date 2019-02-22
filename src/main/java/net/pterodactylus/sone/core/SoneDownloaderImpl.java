/*
 * Sone - SoneDownloaderImpl.java - Copyright © 2010–2019 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.core;

import static freenet.support.io.Closer.close;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.logging.Logger.getLogger;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.SoneStatus;
import net.pterodactylus.util.service.AbstractService;

import freenet.client.FetchResult;
import freenet.client.async.ClientContext;
import freenet.client.async.USKCallback;
import freenet.keys.FreenetURI;
import freenet.keys.USK;
import freenet.node.RequestStarter;
import freenet.support.api.Bucket;

/**
 * The Sone downloader is responsible for download Sones as they are updated.
 */
public class SoneDownloaderImpl extends AbstractService implements SoneDownloader {

	/** The logger. */
	private static final Logger logger = getLogger(SoneDownloaderImpl.class.getName());

	/** The maximum protocol version. */
	private static final int MAX_PROTOCOL_VERSION = 0;

	/** The core. */
	private final UpdatedSoneProcessor updatedSoneProcessor;
	private final SoneParser soneParser;

	/** The Freenet interface. */
	private final FreenetInterface freenetInterface;

	/** The sones to update. */
	private final Set<Sone> sones = new HashSet<>();

	@Inject
	SoneDownloaderImpl(UpdatedSoneProcessor updatedSoneProcessor, FreenetInterface freenetInterface, SoneParser soneParser) {
		super("Sone Downloader", false);
		this.updatedSoneProcessor = updatedSoneProcessor;
		this.freenetInterface = freenetInterface;
		this.soneParser = soneParser;
	}

	//
	// ACTIONS
	//

	/**
	 * Adds the given Sone to the set of Sones that will be watched for updates.
	 *
	 * @param sone
	 * 		The Sone to add
	 */
	@Override
	public void addSone(final Sone sone) {
		if (!sones.add(sone)) {
			freenetInterface.unregisterUsk(sone);
		}
		final USKCallback uskCallback = new USKCallback() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void onFoundEdition(long edition, USK key,
					ClientContext clientContext, boolean metadata,
					short codec, byte[] data, boolean newKnownGood,
					boolean newSlotToo) {
				logger.log(Level.FINE, format(
						"Found USK update for Sone “%s” at %s, new known good: %s, new slot too: %s.",
						sone, key, newKnownGood, newSlotToo));
				if (edition > sone.getLatestEdition()) {
					sone.setLatestEdition(edition);
					new Thread(fetchSoneAsSskAction(sone),
							"Sone Downloader").start();
				}
			}

			@Override
			public short getPollingPriorityProgress() {
				return RequestStarter.INTERACTIVE_PRIORITY_CLASS;
			}

			@Override
			public short getPollingPriorityNormal() {
				return RequestStarter.INTERACTIVE_PRIORITY_CLASS;
			}
		};
		if (soneHasBeenActiveRecently(sone)) {
			freenetInterface.registerActiveUsk(sone.getRequestUri(),
					uskCallback);
		} else {
			freenetInterface.registerPassiveUsk(sone.getRequestUri(),
					uskCallback);
		}
	}

	private boolean soneHasBeenActiveRecently(Sone sone) {
		return (currentTimeMillis() - sone.getTime()) < DAYS.toMillis(7);
	}

	private void fetchSoneAsSsk(Sone sone) {
		fetchSone(sone, sone.getRequestUri().sskForUSK(), false);
	}

	/**
	 * Fetches the Sone from the given URI.
	 *
	 * @param sone
	 * 		The Sone to fetch
	 * @param soneUri
	 * 		The URI of the Sone to fetch
	 * @param fetchOnly
	 * 		{@code true} to only fetch and parse the Sone, {@code false}
	 * 		to {@link Core#updateSone(Sone) update} it in the core
	 * @return The downloaded Sone, or {@code null} if the Sone could not be
	 *         downloaded
	 */
	@Override
	public Sone fetchSone(Sone sone, FreenetURI soneUri, boolean fetchOnly) {
		logger.log(Level.FINE, String.format("Starting fetch for Sone “%s” from %s…", sone, soneUri));
		FreenetURI requestUri = soneUri.setMetaString(new String[] { "sone.xml" });
		sone.setStatus(SoneStatus.downloading);
		try {
			Fetched fetchResults = freenetInterface.fetchUri(requestUri);
			if (fetchResults == null) {
				/* TODO - mark Sone as bad. */
				return null;
			}
			logger.log(Level.FINEST, String.format("Got %d bytes back.", fetchResults.getFetchResult().size()));
			Sone parsedSone = parseSone(sone, fetchResults.getFetchResult(), fetchResults.getFreenetUri());
			if (parsedSone != null) {
				if (!fetchOnly) {
					parsedSone.setStatus((parsedSone.getTime() == 0) ? SoneStatus.unknown : SoneStatus.idle);
					updatedSoneProcessor.updateSone(parsedSone);
					addSone(parsedSone);
				}
			}
			return parsedSone;
		} finally {
			sone.setStatus((sone.getTime() == 0) ? SoneStatus.unknown : SoneStatus.idle);
		}
	}

	/**
	 * Parses a Sone from a fetch result.
	 *
	 * @param originalSone
	 * 		The sone to parse, or {@code null} if the Sone is yet unknown
	 * @param fetchResult
	 * 		The fetch result
	 * @param requestUri
	 * 		The requested URI
	 * @return The parsed Sone, or {@code null} if the Sone could not be parsed
	 */
	private Sone parseSone(Sone originalSone, FetchResult fetchResult, FreenetURI requestUri) {
		logger.log(Level.FINEST, String.format("Parsing FetchResult (%d bytes, %s) for %s…", fetchResult.size(), fetchResult.getMimeType(), originalSone));
		Bucket soneBucket = fetchResult.asBucket();
		InputStream soneInputStream = null;
		try {
			soneInputStream = soneBucket.getInputStream();
			Sone parsedSone = soneParser.parseSone(originalSone,
					soneInputStream);
			if (parsedSone != null) {
				parsedSone.setLatestEdition(requestUri.getEdition());
			}
			return parsedSone;
		} catch (Exception e1) {
			logger.log(Level.WARNING, String.format("Could not parse Sone from %s!", requestUri), e1);
		} finally {
			close(soneInputStream);
			close(soneBucket);
		}
		return null;
	}

	@Override
	public Runnable fetchSoneAsUskAction(final Sone sone) {
		return new Runnable() {
			@Override
			public void run() {
				fetchSone(sone, sone.getRequestUri(), false);
			}
		};
	}

	@Override
	public Runnable fetchSoneAsSskAction(final Sone sone) {
		return new Runnable() {
			@Override
			public void run() {
				fetchSoneAsSsk(sone);
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	protected void serviceStop() {
		for (Sone sone : sones) {
			freenetInterface.unregisterUsk(sone);
		}
	}

}
