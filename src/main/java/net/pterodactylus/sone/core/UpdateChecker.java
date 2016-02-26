/*
 * Sone - UpdateChecker.java - Copyright © 2011–2015 David Roden
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

import static java.util.logging.Logger.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;

import net.pterodactylus.sone.core.FreenetInterface.Fetched;
import net.pterodactylus.sone.core.event.UpdateFoundEvent;
import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.version.Version;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import freenet.keys.FreenetURI;
import freenet.support.api.Bucket;

/**
 * Watches the official Sone homepage for new releases.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
@Singleton
public class UpdateChecker {

	/** The logger. */
	private static final Logger logger = getLogger(UpdateChecker.class.getName());

	/** The event bus. */
	private final EventBus eventBus;

	/** The Freenet interface. */
	private final FreenetInterface freenetInterface;

	/** The current URI of the homepage. */
	private FreenetURI currentUri;

	/** The latest known edition. */
	private long latestEdition;

	/** The current latest known version. */
	private Version currentLatestVersion = SonePlugin.VERSION;

	/** The release date of the latest version. */
	private long latestVersionDate;

	/**
	 * Creates a new update checker.
	 *
	 * @param eventBus
	 *            The event bus
	 * @param freenetInterface
	 *            The freenet interface to use
	 */
	@Inject
	public UpdateChecker(EventBus eventBus, FreenetInterface freenetInterface) {
		this.eventBus = eventBus;
		this.freenetInterface = freenetInterface;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether a version that is later than the currently running
	 * version has been found.
	 *
	 * @return {@code true} if a new version was found
	 */
	public boolean hasLatestVersion() {
		return currentLatestVersion.compareTo(SonePlugin.VERSION) > 0;
	}

	/**
	 * Returns the latest version. If no new latest version has been found, the
	 * current version is returned.
	 *
	 * @return The latest known version
	 */
	public Version getLatestVersion() {
		return currentLatestVersion;
	}

	/**
	 * Returns the release time of the latest version. If no new latest version
	 * has been found, the returned value is undefined.
	 *
	 * @return The release time of the latest version, if a new version was
	 *         found
	 */
	public long getLatestVersionDate() {
		return latestVersionDate;
	}

	/**
	 * Returns the latest known edition of the Sone homepage.
	 *
	 * @return The latest edition of the Sone homepage
	 */
	public long getLatestEdition() {
		return latestEdition;
	}

	//
	// ACTIONS
	//

	/**
	 * Starts the update checker.
	 */
	public void start() {
		try {
			currentUri = new FreenetURI(SonePlugin.getHomepage());
		} catch (MalformedURLException mue1) {
			/* this can not really happen unless I screw up. */
			logger.log(Level.SEVERE, "Sone Homepage URI invalid!", mue1);
		}
		freenetInterface.registerUsk(currentUri, new FreenetInterface.Callback() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void editionFound(FreenetURI uri, long edition, boolean newKnownGood, boolean newSlot) {
				logger.log(Level.FINEST, String.format("Found update for %s: %d, %s, %s", uri, edition, newKnownGood, newSlot));
				if (newKnownGood || newSlot) {
					Fetched uriResult = freenetInterface.fetchUri(uri.setMetaString(new String[] { "sone.properties" }));
					if (uriResult == null) {
						logger.log(Level.WARNING, String.format("Could not fetch properties of latest homepage: %s", uri));
						return;
					}
					Bucket resultBucket = uriResult.getFetchResult().asBucket();
					try {
						parseProperties(resultBucket.getInputStream(), edition);
						latestEdition = edition;
					} catch (IOException ioe1) {
						logger.log(Level.WARNING, String.format("Could not parse sone.properties of %s!", uri), ioe1);
					} finally {
						resultBucket.free();
					}
				}
			}
		});
	}

	/**
	 * Stops the update checker.
	 */
	public void stop() {
		freenetInterface.unregisterUsk(currentUri);
	}

	//
	// PRIVATE ACTIONS
	//

	/**
	 * Parses the properties of the latest version and fires events, if
	 * necessary.
	 *
	 * @see UpdateFoundEvent
	 * @param propertiesInputStream
	 *            The input stream to parse
	 * @param edition
	 *            The latest edition of the Sone homepage
	 * @throws IOException
	 *             if an I/O error occured
	 */
	private void parseProperties(InputStream propertiesInputStream, long edition) throws IOException {
		Properties properties = new Properties();
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(propertiesInputStream, "UTF-8");
			properties.load(inputStreamReader);
		} finally {
			Closer.close(inputStreamReader);
		}
		String versionString = properties.getProperty("CurrentVersion/Version");
		String releaseTimeString = properties.getProperty("CurrentVersion/ReleaseTime");
		if ((versionString == null) || (releaseTimeString == null)) {
			logger.log(Level.INFO, "Invalid data parsed from properties.");
			return;
		}
		Version version = Version.parse(versionString);
		long releaseTime = 0;
		try {
			releaseTime = Long.parseLong(releaseTimeString);
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		if ((version == null) || (releaseTime == 0)) {
			logger.log(Level.INFO, "Could not parse data from properties.");
			return;
		}
		if (version.compareTo(currentLatestVersion) > 0) {
			currentLatestVersion = version;
			latestVersionDate = releaseTime;
			logger.log(Level.INFO, String.format("Found new version: %s (%tc)", version, new Date(releaseTime)));
			eventBus.post(new UpdateFoundEvent(version, releaseTime, edition));
		}
	}

}
