/*
 * Sone - SoneRescuer.java - Copyright © 2011–2019 David Roden
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

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.service.AbstractService;
import freenet.keys.FreenetURI;

/**
 * The Sone rescuer downloads older editions of a Sone and updates the currently
 * stored Sone with it.
 */
public class SoneRescuer extends AbstractService {

	/** The core. */
	private final Core core;

	/** The Sone downloader. */
	private final SoneDownloader soneDownloader;

	/** The Sone being rescued. */
	private final Sone sone;

	/** Whether the rescuer is currently fetching a Sone. */
	private volatile boolean fetching;

	/** The currently tried edition. */
	private volatile long currentEdition;

	/** Whether the last fetch was successful. */
	private volatile boolean lastFetchSuccessful = true;

	/**
	 * Creates a new Sone rescuer.
	 *
	 * @param core
	 *            The core
	 * @param soneDownloader
	 *            The Sone downloader
	 * @param sone
	 *            The Sone to rescue
	 */
	public SoneRescuer(Core core, SoneDownloader soneDownloader, Sone sone) {
		super("Sone Rescuer for " + sone.getName());
		this.core = core;
		this.soneDownloader = soneDownloader;
		this.sone = sone;
		currentEdition = sone.getRequestUri().getEdition();
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether the Sone rescuer is currently fetching a Sone.
	 *
	 * @return {@code true} if the Sone rescuer is currently fetching a Sone
	 */
	@SuppressWarnings("unused") // used in rescue.html
	public boolean isFetching() {
		return fetching;
	}

	/**
	 * Returns the edition that is currently being downloaded.
	 *
	 * @return The edition that is currently being downloaded
	 */
	@SuppressWarnings("unused") // used in rescue.html
	public long getCurrentEdition() {
		return currentEdition;
	}

	/**
	 * Returns whether the Sone rescuer can download a next edition.
	 *
	 * @return {@code true} if the Sone rescuer can download a next edition,
	 *         {@code false} if the last edition was already tried
	 */
	public boolean hasNextEdition() {
		return currentEdition > 0;
	}

	/**
	 * Returns the next edition the Sone rescuer can download.
	 *
	 * @return The next edition the Sone rescuer can download
	 */
	@SuppressWarnings("unused") // used in rescue.html
	public long getNextEdition() {
		return currentEdition - 1;
	}

	/**
	 * Sets the edition to rescue.
	 *
	 * @param edition
	 *            The edition to rescue
	 * @return This Sone rescuer
	 */
	public void setEdition(long edition) {
		currentEdition = edition;
	}

	/**
	 * Sets whether the last fetch was successful.
	 *
	 * @return {@code true} if the last fetch was successful, {@code false}
	 *         otherwise
	 */
	@SuppressWarnings("unused") // used in rescue.html
	public boolean isLastFetchSuccessful() {
		return lastFetchSuccessful;
	}

	//
	// ACTIONS
	//

	/**
	 * Starts the next fetch. If you want to fetch a different edition than “the
	 * next older one,” remember to call {@link #setEdition(long)} before
	 * calling this method.
	 */
	public void startNextFetch() {
		fetching = true;
		notifySyncObject();
	}

	//
	// SERVICE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void serviceRun() {
		while (!shouldStop()) {
			while (!shouldStop() && !fetching) {
				sleep();
			}
			if (fetching) {
				core.lockSone(sone);
				FreenetURI soneUri = sone.getRequestUri().setKeyType("SSK").setDocName("Sone-" + currentEdition).setMetaString(new String[] { "sone.xml" });
				System.out.println("URI: " + soneUri);
				Sone fetchedSone = soneDownloader.fetchSone(sone, soneUri, true);
				System.out.println("Sone: " + fetchedSone);
				lastFetchSuccessful = (fetchedSone != null);
				if (lastFetchSuccessful) {
					core.updateSone(fetchedSone, true);
				}
				fetching = false;
			}
		}
	}

}
