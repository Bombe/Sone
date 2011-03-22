/*
 * Sone - UpdateChecker.java - Copyright © 2011 David Roden
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.util.collection.Pair;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.version.Version;
import freenet.client.FetchResult;
import freenet.keys.FreenetURI;
import freenet.support.api.Bucket;

/**
 * Watches the official Sone homepage for new releases.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UpdateChecker {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(UpdateChecker.class);

	/** The key of the Sone homepage. */
	private static final String SONE_HOMEPAGE = "USK@nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI,DuQSUZiI~agF8c-6tjsFFGuZ8eICrzWCILB60nT8KKo,AQACAAE/sone/";

	/** The current latest known edition. */
	private static final int LATEST_EDITION = 25;

	/** The Freenet interface. */
	private final FreenetInterface freenetInterface;

	/** The update listener manager. */
	private final UpdateListenerManager updateListenerManager = new UpdateListenerManager();

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
	 * @param freenetInterface
	 *            The freenet interface to use
	 */
	public UpdateChecker(FreenetInterface freenetInterface) {
		this.freenetInterface = freenetInterface;
	}

	//
	// EVENT LISTENER MANAGEMENT
	//

	/**
	 * Adds the given listener to the list of registered listeners.
	 *
	 * @param updateListener
	 *            The listener to add
	 */
	public void addUpdateListener(UpdateListener updateListener) {
		updateListenerManager.addListener(updateListener);
	}

	/**
	 * Removes the given listener from the list of registered listeners.
	 *
	 * @param updateListener
	 *            The listener to remove
	 */
	public void removeUpdateListener(UpdateListener updateListener) {
		updateListenerManager.removeListener(updateListener);
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
			currentUri = new FreenetURI(SONE_HOMEPAGE + LATEST_EDITION);
		} catch (MalformedURLException mue1) {
			/* this can not really happen unless I screw up. */
			logger.log(Level.SEVERE, "Sone Homepage URI invalid!", mue1);
		}
		freenetInterface.registerUsk(currentUri, new FreenetInterface.Callback() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void editionFound(FreenetURI uri, long edition, boolean newKnownGood, boolean newSlot) {
				logger.log(Level.FINEST, "Found update for %s: %d, %s, %s", new Object[] { uri, edition, newKnownGood, newSlot });
				if (newKnownGood || newSlot) {
					Pair<FreenetURI, FetchResult> uriResult = freenetInterface.fetchUri(uri.setMetaString(new String[] { "sone.properties" }));
					if (uriResult == null) {
						logger.log(Level.WARNING, "Could not fetch properties of latest homepage: %s", uri);
						return;
					}
					Bucket resultBucket = uriResult.getRight().asBucket();
					try {
						parseProperties(resultBucket.getInputStream(), edition);
						latestEdition = edition;
					} catch (IOException ioe1) {
						logger.log(Level.WARNING, "Could not parse sone.properties of " + uri, ioe1);
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
	 * @see UpdateListener#updateFound(Version, long, long)
	 * @see UpdateListenerManager#fireUpdateFound(Version, long, long)
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
			logger.log(Level.INFO, "Found new version: %s (%tc)", new Object[] { version, new Date(releaseTime) });
			updateListenerManager.fireUpdateFound(version, releaseTime, edition);
		}
	}

}
