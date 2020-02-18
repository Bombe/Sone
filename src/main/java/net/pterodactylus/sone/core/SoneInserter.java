/*
 * Sone - SoneInserter.java - Copyright © 2010–2020 David Roden
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
import static java.util.concurrent.TimeUnit.*;
import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toList;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codahale.metrics.*;
import com.google.common.base.*;
import net.pterodactylus.sone.core.SoneModificationDetector.LockableFingerprintProvider;
import net.pterodactylus.sone.core.event.InsertionDelayChangedEvent;
import net.pterodactylus.sone.core.event.SoneInsertAbortedEvent;
import net.pterodactylus.sone.core.event.SoneInsertedEvent;
import net.pterodactylus.sone.core.event.SoneInsertingEvent;
import net.pterodactylus.sone.data.AlbumKt;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.SoneStatus;
import net.pterodactylus.sone.data.SoneKt;
import net.pterodactylus.sone.main.SonePlugin;
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
	private final SoneUriCreator soneUriCreator;
	private final long delay;
	private final String soneId;
	private final Histogram soneInsertDurationHistogram;
	private final Meter soneInsertErrorMeter;

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
	public SoneInserter(final Core core, EventBus eventBus, FreenetInterface freenetInterface, MetricRegistry metricRegistry, SoneUriCreator soneUriCreator, final String soneId) {
		this(core, eventBus, freenetInterface, metricRegistry, soneUriCreator, soneId, new SoneModificationDetector(new LockableFingerprintProvider() {
			@Override
			public boolean isLocked() {
				Sone sone = core.getSone(soneId);
				if (sone == null) {
					return false;
				}
				return core.isLocked(sone);
			}

			@Override
			public String getFingerprint() {
				Sone sone = core.getSone(soneId);
				if (sone == null) {
					return null;
				}
				return sone.getFingerprint();
			}
		}, insertionDelay), 1000);
	}

	@VisibleForTesting
	SoneInserter(Core core, EventBus eventBus, FreenetInterface freenetInterface, MetricRegistry metricRegistry, SoneUriCreator soneUriCreator, String soneId, SoneModificationDetector soneModificationDetector, long delay) {
		super("Sone Inserter for “" + soneId + "”", false);
		this.core = core;
		this.eventBus = eventBus;
		this.freenetInterface = freenetInterface;
		this.soneInsertDurationHistogram = metricRegistry.histogram("sone.insert.duration", () -> new Histogram(new ExponentiallyDecayingReservoir(3000, 0)));
		this.soneInsertErrorMeter = metricRegistry.meter("sone.insert.errors");
		this.soneUriCreator = soneUriCreator;
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
		return soneModificationDetector.getLastInsertFingerprint();
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
					Sone sone = core.getSone(soneId);
					if (sone == null) {
						logger.log(Level.WARNING, format("Sone %s has disappeared, exiting inserter.", soneId));
						return;
					}
					InsertInformation insertInformation = new InsertInformation(sone);
					logger.log(Level.INFO, String.format("Inserting Sone “%s”…", sone.getName()));

					boolean success = false;
					try {
						sone.setStatus(SoneStatus.inserting);
						long insertTime = currentTimeMillis();
						eventBus.post(new SoneInsertingEvent(sone));
						Stopwatch stopwatch = Stopwatch.createStarted();
						FreenetURI finalUri = freenetInterface.insertDirectory(soneUriCreator.getInsertUri(sone), insertInformation.generateManifestEntries(), "index.html");
						stopwatch.stop();
						soneInsertDurationHistogram.update(stopwatch.elapsed(MICROSECONDS));
						eventBus.post(new SoneInsertedEvent(sone, stopwatch.elapsed(MILLISECONDS), insertInformation.getFingerprint()));
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
						soneInsertErrorMeter.mark();
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
	 */
	@VisibleForTesting
	class InsertInformation implements Closeable {

		/** All properties of the Sone, copied for thread safety. */
		private final Map<String, Object> soneProperties = new HashMap<>();
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
			Map<String, Object> soneProperties = new HashMap<>();
			soneProperties.put("id", sone.getId());
			soneProperties.put("name", sone.getName());
			soneProperties.put("time", currentTimeMillis());
			soneProperties.put("profile", sone.getProfile());
			soneProperties.put("posts", Ordering.from(Post.NEWEST_FIRST).sortedCopy(sone.getPosts()));
			soneProperties.put("replies", Ordering.from(Reply.TIME_COMPARATOR).reverse().sortedCopy(sone.getReplies()));
			soneProperties.put("likedPostIds", new HashSet<>(sone.getLikedPostIds()));
			soneProperties.put("likedReplyIds", new HashSet<>(sone.getLikedReplyIds()));
			soneProperties.put("albums", SoneKt.getAllAlbums(sone).stream().filter(AlbumKt.notEmpty()::invoke).collect(toList()));
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
			HashMap<String, Object> manifestEntries = new HashMap<>();

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
	 */
	@VisibleForTesting
	static class ManifestCreator implements Closeable {

		private final Core core;
		private final Map<String, Object> soneProperties;
		private final Set<Bucket> buckets = new HashSet<>();

		ManifestCreator(Core core, Map<String, Object> soneProperties) {
			this.core = core;
			this.soneProperties = soneProperties;
		}

		public ManifestElement createManifestElement(String name, String contentType, String templateName) {
			Template template;
			try (InputStream templateInputStream = getClass().getResourceAsStream(templateName);
					InputStreamReader templateInputStreamReader = new InputStreamReader(templateInputStream, utf8Charset)) {
				template = TemplateParser.parse(templateInputStreamReader);
			} catch (IOException | TemplateException e1) {
				logger.log(Level.SEVERE, String.format("Could not parse template “%s”!", templateName), e1);
				return null;
			}

			TemplateContext templateContext = templateContextFactory.createTemplateContext();
			templateContext.set("core", core);
			templateContext.set("currentSone", soneProperties);
			templateContext.set("currentEdition", core.getUpdateChecker().getLatestEdition());
			templateContext.set("version", SonePlugin.getPluginVersion());
			try (StringWriter writer = new StringWriter()) {
				template.render(templateContext, writer);
				RandomAccessBucket bucket = new ArrayBucket(writer.toString().getBytes(Charsets.UTF_8));
				buckets.add(bucket);
				return new ManifestElement(name, bucket, contentType, bucket.size());
			} catch (IOException | TemplateException e1) {
				logger.log(Level.SEVERE, String.format("Could not render template “%s”!", templateName), e1);
				return null;
			}
		}

		public void close() {
			for (Bucket bucket : buckets) {
				bucket.free();
			}
		}

	}

}
