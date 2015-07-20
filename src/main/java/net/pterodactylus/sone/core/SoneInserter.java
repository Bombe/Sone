/*
 * Sone - SoneInserter.java - Copyright © 2010–2013 David Roden
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

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.logging.Logger.getLogger;
import static net.pterodactylus.sone.data.Album.NOT_EMPTY;

import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.SoneModificationDetector.LockableFingerprintProvider;
import net.pterodactylus.sone.core.event.InsertionDelayChangedEvent;
import net.pterodactylus.sone.core.event.SoneInsertAbortedEvent;
import net.pterodactylus.sone.core.event.SoneInsertedEvent;
import net.pterodactylus.sone.core.event.SoneInsertingEvent;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.SoneStatus;
import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.service.AbstractService;
import net.pterodactylus.util.template.HtmlFilter;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateContextFactory;
import net.pterodactylus.util.template.TemplateException;
import net.pterodactylus.util.template.TemplateParser;
import net.pterodactylus.util.template.XmlFilter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import freenet.keys.FreenetURI;
import freenet.support.api.Bucket;
import freenet.support.api.ManifestElement;
import freenet.support.api.RandomAccessBucket;
import freenet.support.io.ArrayBucket;

/**
 * A Sone inserter is responsible for inserting a Sone if it has changed.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneInserter extends AbstractService {

	/** The logger. */
	private static final Logger logger = getLogger(SoneInserter.class.getName());

	/** The insertion delay (in seconds). */
	private static final AtomicInteger insertionDelay = new AtomicInteger(60);

	/** The template factory used to create the templates. */
	private static final TemplateContextFactory templateContextFactory = new TemplateContextFactory();

	static {
		templateContextFactory.addAccessor(Object.class, new ReflectionAccessor());
		templateContextFactory.addFilter("xml", new XmlFilter());
		templateContextFactory.addFilter("html", new HtmlFilter());
	}

	/** The UTF-8 charset. */
	private static final Charset utf8Charset = Charset.forName("UTF-8");

	/** The core. */
	private final Core core;

	/** The event bus. */
	private final EventBus eventBus;

	/** The Freenet interface. */
	private final FreenetInterface freenetInterface;

	private final SoneModificationDetector soneModificationDetector;
	private final long delay;
	private final String soneId;

	/**
	 * Creates a new Sone inserter.
	 *
	 * @param core
	 *            The core
	 * @param eventBus
	 *            The event bus
	 * @param freenetInterface
	 *            The freenet interface
	 * @param soneId
	 *            The ID of the Sone to insert
	 */
	public SoneInserter(final Core core, EventBus eventBus, FreenetInterface freenetInterface, final String soneId) {
		this(core, eventBus, freenetInterface, soneId, new SoneModificationDetector(new LockableFingerprintProvider() {
			@Override
			public boolean isLocked() {
				final Optional<Sone> sone = core.getSone(soneId);
				if (!sone.isPresent()) {
					return false;
				}
				return core.isLocked(sone.get());
			}

			@Override
			public String getFingerprint() {
				final Optional<Sone> sone = core.getSone(soneId);
				if (!sone.isPresent()) {
					return null;
				}
				return sone.get().getFingerprint();
			}
		}, insertionDelay), 1000);
	}

	@VisibleForTesting
	SoneInserter(Core core, EventBus eventBus, FreenetInterface freenetInterface, String soneId, SoneModificationDetector soneModificationDetector, long delay) {
		super("Sone Inserter for “" + soneId + "”", false);
		this.core = core;
		this.eventBus = eventBus;
		this.freenetInterface = freenetInterface;
		this.soneId = soneId;
		this.soneModificationDetector = soneModificationDetector;
		this.delay = delay;
	}

	//
	// ACCESSORS
	//

	@VisibleForTesting
	static AtomicInteger getInsertionDelay() {
		return insertionDelay;
	}

	/**
	 * Changes the insertion delay, i.e. the time the Sone inserter waits after it
	 * has noticed a Sone modification before it starts the insert.
	 *
	 * @param insertionDelay
	 *            The insertion delay (in seconds)
	 */
	private static void setInsertionDelay(int insertionDelay) {
		SoneInserter.insertionDelay.set(insertionDelay);
	}

	/**
	 * Returns the fingerprint of the last insert.
	 *
	 * @return The fingerprint of the last insert
	 */
	public String getLastInsertFingerprint() {
		return soneModificationDetector.getOriginalFingerprint();
	}

	/**
	 * Sets the fingerprint of the last insert.
	 *
	 * @param lastInsertFingerprint
	 *            The fingerprint of the last insert
	 */
	public void setLastInsertFingerprint(String lastInsertFingerprint) {
		soneModificationDetector.setFingerprint(lastInsertFingerprint);
	}

	/**
	 * Returns whether the Sone inserter has detected a modification of the
	 * Sone.
	 *
	 * @return {@code true} if the Sone has been modified, {@code false}
	 *         otherwise
	 */
	public boolean isModified() {
		return soneModificationDetector.isModified();
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
				/* check every second. */
				sleep(delay);

				if (soneModificationDetector.isEligibleForInsert()) {
					Optional<Sone> soneOptional = core.getSone(soneId);
					if (!soneOptional.isPresent()) {
						logger.log(Level.WARNING, format("Sone %s has disappeared, exiting inserter.", soneId));
						return;
					}
					Sone sone = soneOptional.get();
					InsertInformation insertInformation = new InsertInformation(sone);
					logger.log(Level.INFO, String.format("Inserting Sone “%s”…", sone.getName()));

					boolean success = false;
					try {
						sone.setStatus(SoneStatus.inserting);
						long insertTime = currentTimeMillis();
						eventBus.post(new SoneInsertingEvent(sone));
						FreenetURI finalUri = freenetInterface.insertDirectory(sone.getInsertUri(), insertInformation.generateManifestEntries(), "index.html");
						eventBus.post(new SoneInsertedEvent(sone, currentTimeMillis() - insertTime, insertInformation.getFingerprint()));
						/* at this point we might already be stopped. */
						if (shouldStop()) {
							/* if so, bail out, don’t change anything. */
							break;
						}
						sone.setTime(insertTime);
						sone.setLatestEdition(finalUri.getEdition());
						core.touchConfiguration();
						success = true;
						logger.log(Level.INFO, String.format("Inserted Sone “%s” at %s.", sone.getName(), finalUri));
					} catch (SoneException se1) {
						eventBus.post(new SoneInsertAbortedEvent(sone, se1));
						logger.log(Level.WARNING, String.format("Could not insert Sone “%s”!", sone.getName()), se1);
					} finally {
						insertInformation.close();
						sone.setStatus(SoneStatus.idle);
					}

					/*
					 * reset modification counter if Sone has not been modified
					 * while it was inserted.
					 */
					if (success) {
						synchronized (sone) {
							if (insertInformation.getFingerprint().equals(sone.getFingerprint())) {
								logger.log(Level.FINE, String.format("Sone “%s” was not modified further, resetting counter…", sone));
								soneModificationDetector.setFingerprint(insertInformation.getFingerprint());
								core.touchConfiguration();
							}
						}
					}
				}
			} catch (Throwable t1) {
				logger.log(Level.SEVERE, "SoneInserter threw an Exception!", t1);
			}
		}
	}

	@Subscribe
	public void insertionDelayChanged(InsertionDelayChangedEvent insertionDelayChangedEvent) {
		setInsertionDelay(insertionDelayChangedEvent.getInsertionDelay());
	}

	/**
	 * Container for information that are required to insert a Sone. This
	 * container merely exists to copy all relevant data without holding a lock
	 * on the {@link Sone} object for too long.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	@VisibleForTesting
	class InsertInformation implements Closeable {

		/** All properties of the Sone, copied for thread safety. */
		private final Map<String, Object> soneProperties = new HashMap<String, Object>();
		private final String fingerprint;
		private final ManifestCreator manifestCreator;

		/**
		 * Creates a new insert information container.
		 *
		 * @param sone
		 *            The sone to insert
		 */
		public InsertInformation(Sone sone) {
			this.fingerprint = sone.getFingerprint();
			Map<String, Object> soneProperties = new HashMap<String, Object>();
			soneProperties.put("id", sone.getId());
			soneProperties.put("name", sone.getName());
			soneProperties.put("time", currentTimeMillis());
			soneProperties.put("requestUri", sone.getRequestUri());
			soneProperties.put("profile", sone.getProfile());
			soneProperties.put("posts", Ordering.from(Post.TIME_COMPARATOR).sortedCopy(sone.getPosts()));
			soneProperties.put("replies", Ordering.from(Reply.TIME_COMPARATOR).reverse().sortedCopy(sone.getReplies()));
			soneProperties.put("likedPostIds", new HashSet<String>(sone.getLikedPostIds()));
			soneProperties.put("likedReplyIds", new HashSet<String>(sone.getLikedReplyIds()));
			soneProperties.put("albums", FluentIterable.from(sone.getRootAlbum().getAlbums()).transformAndConcat(Album.FLATTENER).filter(NOT_EMPTY).toList());
			manifestCreator = new ManifestCreator(core, soneProperties);
		}

		//
		// ACCESSORS
		//

		@VisibleForTesting
		String getFingerprint() {
			return fingerprint;
		}

		//
		// ACTIONS
		//

		/**
		 * Generates all manifest entries required to insert this Sone.
		 *
		 * @return The manifest entries for the Sone insert
		 */
		public HashMap<String, Object> generateManifestEntries() {
			HashMap<String, Object> manifestEntries = new HashMap<String, Object>();

			/* first, create an index.html. */
			manifestEntries.put("index.html", manifestCreator.createManifestElement(
					"index.html", "text/html; charset=utf-8",
					"/templates/insert/index.html"));

			/* now, store the sone. */
			manifestEntries.put("sone.xml", manifestCreator.createManifestElement(
					"sone.xml", "text/xml; charset=utf-8",
					"/templates/insert/sone.xml"));

			return manifestEntries;
		}

		@Override
		public void close() {
			manifestCreator.close();
		}

	}

	/**
	 * Creates manifest elements for an insert by rendering a template.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	@VisibleForTesting
	static class ManifestCreator implements Closeable {

		private final Core core;
		private final Map<String, Object> soneProperties;
		private final Set<Bucket> buckets = new HashSet<Bucket>();

		ManifestCreator(Core core, Map<String, Object> soneProperties) {
			this.core = core;
			this.soneProperties = soneProperties;
		}

		public ManifestElement createManifestElement(String name, String contentType, String templateName) {
			InputStreamReader templateInputStreamReader = null;
			InputStream templateInputStream = null;
			Template template;
			try {
				templateInputStream = getClass().getResourceAsStream(templateName);
				templateInputStreamReader = new InputStreamReader(templateInputStream, utf8Charset);
				template = TemplateParser.parse(templateInputStreamReader);
			} catch (TemplateException te1) {
				logger.log(Level.SEVERE, String.format("Could not parse template “%s”!", templateName), te1);
				return null;
			} finally {
				Closer.close(templateInputStreamReader);
				Closer.close(templateInputStream);
			}

			TemplateContext templateContext = templateContextFactory.createTemplateContext();
			templateContext.set("core", core);
			templateContext.set("currentSone", soneProperties);
			templateContext.set("currentEdition", core.getUpdateChecker().getLatestEdition());
			templateContext.set("version", SonePlugin.VERSION);
			StringWriter writer = new StringWriter();
			try {
				template.render(templateContext, writer);
				RandomAccessBucket bucket = new ArrayBucket(writer.toString().getBytes(Charsets.UTF_8));
				buckets.add(bucket);
				return new ManifestElement(name, bucket, contentType, bucket.size());
			} catch (TemplateException te1) {
				logger.log(Level.SEVERE, String.format("Could not render template “%s”!", templateName), te1);
				return null;
			} finally {
				Closer.close(writer);
			}
		}

		public void close() {
			for (Bucket bucket : buckets) {
				bucket.free();
			}
		}

	}

}
