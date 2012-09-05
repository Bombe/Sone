/*
 * Sone - TrustUpdater.java - Copyright © 2012 David Roden
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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.freenet.plugin.PluginException;
import net.pterodactylus.sone.freenet.wot.DefaultIdentity;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.freenet.wot.Trust;
import net.pterodactylus.sone.freenet.wot.WebOfTrustConnector;
import net.pterodactylus.sone.freenet.wot.WebOfTrustException;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.service.AbstractService;

/**
 * Updates identity’s trust in a background thread because getting updates from
 * the WebOfTrust plugin can potentially last quite long.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TrustUpdater extends AbstractService {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(TrustUpdater.class);

	/** Stop job. */
	private static final TrustUpdateJob stopJob = new TrustUpdateJob(null, null);

	/** The web of trust connector. */
	private final WebOfTrustConnector webOfTrustConnector;

	/** The queue for jobs. */
	private final BlockingQueue<TrustUpdateJob> updateJobs = new LinkedBlockingQueue<TrustUpdateJob>();

	/**
	 * Creates a new trust updater.
	 *
	 * @param webOfTrustConnector
	 *            The web of trust connector
	 */
	public TrustUpdater(WebOfTrustConnector webOfTrustConnector) {
		super("Trust Updater");
		this.webOfTrustConnector = webOfTrustConnector;
	}

	//
	// ACTIONS
	//

	/**
	 * Retrieves the trust relation between the truster and the trustee. This
	 * method will return immediately and perform a trust update in the
	 * background.
	 *
	 * @param truster
	 *            The identity giving the trust
	 * @param trustee
	 *            The identity receiving the trust
	 */
	public void getTrust(OwnIdentity truster, Identity trustee) {
		GetTrustJob getTrustJob = new GetTrustJob(truster, trustee);
		if (!updateJobs.contains(getTrustJob)) {
			logger.log(Level.FINER, "Adding Trust Update Job: " + getTrustJob);
			try {
				updateJobs.put(getTrustJob);
			} catch (InterruptedException ie1) {
				/* the queue is unbounded so it should never block. */
			}
		}
	}

	/**
	 * Updates the trust relation between the truster and the trustee. This
	 * method will return immediately and perform a trust update in the
	 * background.
	 *
	 * @param truster
	 *            The identity giving the trust
	 * @param trustee
	 *            The identity receiving the trust
	 * @param score
	 *            The new level of trust (from -100 to 100, may be {@code null}
	 *            to remove the trust completely)
	 * @param comment
	 *            The comment of the trust relation
	 */
	public void setTrust(OwnIdentity truster, Identity trustee, Integer score, String comment) {
		SetTrustJob setTrustJob = new SetTrustJob(truster, trustee, score, comment);
		if (updateJobs.contains(setTrustJob)) {
			updateJobs.remove(setTrustJob);
		}
		logger.log(Level.FINER, "Adding Trust Update Job: " + setTrustJob);
		try {
			updateJobs.put(setTrustJob);
		} catch (InterruptedException e) {
			/* the queue is unbounded so it should never block. */
		}
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
			try {
				TrustUpdateJob updateJob = updateJobs.take();
				if (shouldStop() || (updateJob == stopJob)) {
					break;
				}
				logger.log(Level.FINE, "Running Trust Update Job: " + updateJob);
				long startTime = System.currentTimeMillis();
				updateJob.run();
				long endTime = System.currentTimeMillis();
				logger.log(Level.FINE, "Trust Update Job finished, took " + (endTime - startTime) + " ms.");
			} catch (InterruptedException ie1) {
				/* happens, ignore, loop. */
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void serviceStop() {
		try {
			updateJobs.put(stopJob);
		} catch (InterruptedException ie1) {
			/* the queue is unbounded so it should never block. */
		}
	}

	/**
	 * Base class for trust update jobs.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class TrustUpdateJob {

		/** The identity giving the trust. */
		protected final OwnIdentity truster;

		/** The identity receiving the trust. */
		protected final Identity trustee;

		/**
		 * Creates a new trust update job.
		 *
		 * @param truster
		 *            The identity giving the trust
		 * @param trustee
		 *            The identity receiving the trust
		 */
		public TrustUpdateJob(OwnIdentity truster, Identity trustee) {
			this.truster = truster;
			this.trustee = trustee;
		}

		//
		// ACCESSORS
		//

		/**
		 * Performs the actual update operation.
		 * <p>
		 * The implementation of this class does nothing.
		 */
		public void run() {
			/* does nothing. */
		}

		//
		// OBJECT METHODS
		//

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object object) {
			if ((object == null) || !object.getClass().equals(getClass())) {
				return false;
			}
			TrustUpdateJob updateJob = (TrustUpdateJob) object;
			return ((truster == null) ? (updateJob.truster == null) : updateJob.truster.equals(truster)) && ((trustee == null) ? (updateJob.trustee == null) : updateJob.trustee.equals(trustee));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return ((truster == null) ? 0 : truster.hashCode()) ^ ((trustee == null) ? 0 : trustee.hashCode());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return String.format("%s[truster=%s,trustee=%s]", getClass().getSimpleName(), (truster == null) ? null : truster.getId(), (trustee == null) ? null : trustee.getId());
		}

	}

	/**
	 * Update job that sets the trust relation between two identities.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private class SetTrustJob extends TrustUpdateJob {

		/** The score of the relation. */
		private final Integer score;

		/** The comment of the relation. */
		private final String comment;

		/**
		 * Creates a new set trust job.
		 *
		 * @param truster
		 *            The identity giving the trust
		 * @param trustee
		 *            The identity receiving the trust
		 * @param score
		 *            The score of the trust (from -100 to 100, may be
		 *            {@code null} to remote the trust relation completely)
		 * @param comment
		 *            The comment of the trust relation
		 */
		public SetTrustJob(OwnIdentity truster, Identity trustee, Integer score, String comment) {
			super(truster, trustee);
			this.score = score;
			this.comment = comment;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("synthetic-access")
		public void run() {
			try {
				if (score != null) {
					if (trustee instanceof DefaultIdentity) {
						((DefaultIdentity) trustee).setTrust(truster, new Trust(score, null, 0));
					}
					webOfTrustConnector.setTrust(truster, trustee, score, comment);
				} else {
					if (trustee instanceof DefaultIdentity) {
						((DefaultIdentity) trustee).setTrust(truster, null);
					}
					webOfTrustConnector.removeTrust(truster, trustee);
				}
			} catch (WebOfTrustException wote1) {
				logger.log(Level.WARNING, "Could not set Trust value for " + truster + " -> " + trustee + " to " + score + " (" + comment + ")!", wote1);
			}
		}

	}

	/**
	 * Update job that retrieves the trust relation between two identities.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private class GetTrustJob extends TrustUpdateJob {

		/**
		 * Creates a new trust update job.
		 *
		 * @param truster
		 *            The identity giving the trust
		 * @param trustee
		 *            The identity receiving the trust
		 */
		public GetTrustJob(OwnIdentity truster, Identity trustee) {
			super(truster, trustee);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("synthetic-access")
		public void run() {
			try {
				Trust trust = webOfTrustConnector.getTrust(truster, trustee);
				if (trustee instanceof DefaultIdentity) {
					((DefaultIdentity) trustee).setTrust(truster, trust);
				}
			} catch (PluginException pe1) {
				logger.log(Level.WARNING, "Could not get Trust value for " + truster + " -> " + trustee + "!", pe1);
			}
		}
	}

}
