/*
 * Sone - Core.java - Copyright © 2010–2020 David Roden
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

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.primitives.Longs.tryParse;
import static java.lang.String.format;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static net.pterodactylus.sone.data.AlbumKt.getAllImages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.codahale.metrics.*;
import net.pterodactylus.sone.core.ConfigurationSoneParser.InvalidAlbumFound;
import net.pterodactylus.sone.core.ConfigurationSoneParser.InvalidImageFound;
import net.pterodactylus.sone.core.ConfigurationSoneParser.InvalidParentAlbumFound;
import net.pterodactylus.sone.core.ConfigurationSoneParser.InvalidPostFound;
import net.pterodactylus.sone.core.ConfigurationSoneParser.InvalidPostReplyFound;
import net.pterodactylus.sone.core.event.*;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Client;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Profile.Field;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.SoneStatus;
import net.pterodactylus.sone.data.SoneKt;
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent;
import net.pterodactylus.sone.data.TemporaryImage;
import net.pterodactylus.sone.database.AlbumBuilder;
import net.pterodactylus.sone.database.Database;
import net.pterodactylus.sone.database.DatabaseException;
import net.pterodactylus.sone.database.ImageBuilder;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostProvider;
import net.pterodactylus.sone.database.PostReplyBuilder;
import net.pterodactylus.sone.database.PostReplyProvider;
import net.pterodactylus.sone.database.SoneBuilder;
import net.pterodactylus.sone.database.SoneProvider;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.IdentityManager;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.freenet.wot.event.IdentityAddedEvent;
import net.pterodactylus.sone.freenet.wot.event.IdentityRemovedEvent;
import net.pterodactylus.sone.freenet.wot.event.IdentityUpdatedEvent;
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityAddedEvent;
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityRemovedEvent;
import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.service.AbstractService;
import net.pterodactylus.util.thread.NamedThreadFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import kotlin.jvm.functions.Function1;

/**
 * The Sone core.
 */
@Singleton
public class Core extends AbstractService implements SoneProvider, PostProvider, PostReplyProvider {

	/** The logger. */
	private static final Logger logger = getLogger(Core.class.getName());

	/** The start time. */
	private final long startupTime = System.currentTimeMillis();

	private final AtomicBoolean debug = new AtomicBoolean(false);

	/** The preferences. */
	private final Preferences preferences;

	/** The event bus. */
	private final EventBus eventBus;

	/** The configuration. */
	private final Configuration configuration;

	/** Whether we’re currently saving the configuration. */
	private boolean storingConfiguration = false;

	/** The identity manager. */
	private final IdentityManager identityManager;

	/** Interface to freenet. */
	private final FreenetInterface freenetInterface;

	/** The Sone downloader. */
	private final SoneDownloader soneDownloader;

	/** The image inserter. */
	private final ImageInserter imageInserter;

	/** Sone downloader thread-pool. */
	private final ExecutorService soneDownloaders = Executors.newFixedThreadPool(10, new NamedThreadFactory("Sone Downloader %2$d"));

	/** The update checker. */
	private final UpdateChecker updateChecker;

	/** The trust updater. */
	private final WebOfTrustUpdater webOfTrustUpdater;

	/** Locked local Sones. */
	/* synchronize on itself. */
	private final Set<Sone> lockedSones = new HashSet<>();

	/** Sone inserters. */
	/* synchronize access on this on sones. */
	private final Map<Sone, SoneInserter> soneInserters = new HashMap<>();

	/** Sone rescuers. */
	/* synchronize access on this on sones. */
	private final Map<Sone, SoneRescuer> soneRescuers = new HashMap<>();

	/** All known Sones. */
	private final Set<String> knownSones = new HashSet<>();

	/** The post database. */
	private final Database database;

	/** Trusted identities, sorted by own identities. */
	private final Multimap<OwnIdentity, Identity> trustedIdentities = Multimaps.synchronizedSetMultimap(HashMultimap.<OwnIdentity, Identity>create());

	/** All temporary images. */
	private final Map<String, TemporaryImage> temporaryImages = new HashMap<>();

	/** Ticker for threads that mark own elements as known. */
	private final ScheduledExecutorService localElementTicker = Executors.newScheduledThreadPool(1);

	/** The time the configuration was last touched. */
	private volatile long lastConfigurationUpdate;

	private final MetricRegistry metricRegistry;
	private final Histogram configurationSaveTimeHistogram;

	private final SoneUriCreator soneUriCreator;

	@Inject
	public Core(Configuration configuration, FreenetInterface freenetInterface, IdentityManager identityManager, SoneDownloader soneDownloader, ImageInserter imageInserter, UpdateChecker updateChecker, WebOfTrustUpdater webOfTrustUpdater, EventBus eventBus, Database database, MetricRegistry metricRegistry, SoneUriCreator soneUriCreator) {
		super("Sone Core");
		this.configuration = configuration;
		this.freenetInterface = freenetInterface;
		this.identityManager = identityManager;
		this.soneDownloader = soneDownloader;
		this.imageInserter = imageInserter;
		this.updateChecker = updateChecker;
		this.webOfTrustUpdater = webOfTrustUpdater;
		this.eventBus = eventBus;
		this.database = database;
		this.metricRegistry = metricRegistry;
		this.soneUriCreator = soneUriCreator;
		preferences = new Preferences(eventBus);
		this.configurationSaveTimeHistogram = metricRegistry.histogram("configuration.save.duration", () -> new Histogram(new ExponentiallyDecayingReservoir(3000, 0)));
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the time Sone was started.
	 *
	 * @return The startup time (in milliseconds since Jan 1, 1970 UTC)
	 */
	public long getStartupTime() {
		return startupTime;
	}

	@Nonnull
	public boolean getDebug() {
		return debug.get();
	}

	public void setDebug() {
		debug.set(true);
		eventBus.post(new DebugActivatedEvent());
	}

	/**
	 * Returns the options used by the core.
	 *
	 * @return The options of the core
	 */
	public Preferences getPreferences() {
		return preferences;
	}

	/**
	 * Returns the identity manager used by the core.
	 *
	 * @return The identity manager
	 */
	public IdentityManager getIdentityManager() {
		return identityManager;
	}

	/**
	 * Returns the update checker.
	 *
	 * @return The update checker
	 */
	public UpdateChecker getUpdateChecker() {
		return updateChecker;
	}

	/**
	 * Returns the Sone rescuer for the given local Sone.
	 *
	 * @param sone
	 *            The local Sone to get the rescuer for
	 * @return The Sone rescuer for the given Sone
	 */
	public SoneRescuer getSoneRescuer(Sone sone) {
		checkNotNull(sone, "sone must not be null");
		checkArgument(sone.isLocal(), "sone must be local");
		synchronized (soneRescuers) {
			SoneRescuer soneRescuer = soneRescuers.get(sone);
			if (soneRescuer == null) {
				soneRescuer = new SoneRescuer(this, soneDownloader, sone);
				soneRescuers.put(sone, soneRescuer);
				soneRescuer.start();
			}
			return soneRescuer;
		}
	}

	/**
	 * Returns whether the given Sone is currently locked.
	 *
	 * @param sone
	 *            The sone to check
	 * @return {@code true} if the Sone is locked, {@code false} if it is not
	 */
	public boolean isLocked(Sone sone) {
		synchronized (lockedSones) {
			return lockedSones.contains(sone);
		}
	}

	public SoneBuilder soneBuilder() {
		return database.newSoneBuilder();
	}

	/**
	 * {@inheritDocs}
	 */
	@Nonnull
	@Override
	public Collection<Sone> getSones() {
		return database.getSones();
	}

	@Nonnull
	@Override
	public Function1<String, Sone> getSoneLoader() {
		return database.getSoneLoader();
	}

	/**
	 * Returns the Sone with the given ID, regardless whether it’s local or
	 * remote.
	 *
	 * @param id
	 *            The ID of the Sone to get
	 * @return The Sone with the given ID, or {@code null} if there is no such
	 *         Sone
	 */
	@Override
	@Nullable
	public Sone getSone(@Nonnull String id) {
		return database.getSone(id);
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Collection<Sone> getLocalSones() {
		return database.getLocalSones();
	}

	/**
	 * Returns the local Sone with the given ID, optionally creating a new Sone.
	 *
	 * @param id
	 *            The ID of the Sone
	 * @return The Sone with the given ID, or {@code null}
	 */
	public Sone getLocalSone(String id) {
		Sone sone = database.getSone(id);
		if ((sone != null) && sone.isLocal()) {
			return sone;
		}
		return null;
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Collection<Sone> getRemoteSones() {
		return database.getRemoteSones();
	}

	/**
	 * Returns the remote Sone with the given ID.
	 *
	 *
	 * @param id
	 *            The ID of the remote Sone to get
	 * @return The Sone with the given ID
	 */
	public Sone getRemoteSone(String id) {
		return database.getSone(id);
	}

	/**
	 * Returns whether the given Sone has been modified.
	 *
	 * @param sone
	 *            The Sone to check for modifications
	 * @return {@code true} if a modification has been detected in the Sone,
	 *         {@code false} otherwise
	 */
	public boolean isModifiedSone(Sone sone) {
		return soneInserters.containsKey(sone) && soneInserters.get(sone).isModified();
	}

	/**
	 * Returns a post builder.
	 *
	 * @return A new post builder
	 */
	public PostBuilder postBuilder() {
		return database.newPostBuilder();
	}

	@Nullable
	@Override
	public Post getPost(@Nonnull String postId) {
		return database.getPost(postId);
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Collection<Post> getPosts(String soneId) {
		return database.getPosts(soneId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Post> getDirectedPosts(final String recipientId) {
		checkNotNull(recipientId, "recipient must not be null");
		return database.getDirectedPosts(recipientId);
	}

	/**
	 * Returns a post reply builder.
	 *
	 * @return A new post reply builder
	 */
	public PostReplyBuilder postReplyBuilder() {
		return database.newPostReplyBuilder();
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public PostReply getPostReply(String replyId) {
		return database.getPostReply(replyId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PostReply> getReplies(final String postId) {
		return database.getReplies(postId);
	}

	/**
	 * Returns all Sones that have liked the given post.
	 *
	 * @param post
	 *            The post to get the liking Sones for
	 * @return The Sones that like the given post
	 */
	public Set<Sone> getLikes(Post post) {
		Set<Sone> sones = new HashSet<>();
		for (Sone sone : getSones()) {
			if (sone.getLikedPostIds().contains(post.getId())) {
				sones.add(sone);
			}
		}
		return sones;
	}

	/**
	 * Returns all Sones that have liked the given reply.
	 *
	 * @param reply
	 *            The reply to get the liking Sones for
	 * @return The Sones that like the given reply
	 */
	public Set<Sone> getLikes(PostReply reply) {
		Set<Sone> sones = new HashSet<>();
		for (Sone sone : getSones()) {
			if (sone.getLikedReplyIds().contains(reply.getId())) {
				sones.add(sone);
			}
		}
		return sones;
	}

	/**
	 * Returns whether the given post is bookmarked.
	 *
	 * @param post
	 *            The post to check
	 * @return {@code true} if the given post is bookmarked, {@code false}
	 *         otherwise
	 */
	public boolean isBookmarked(Post post) {
		return database.isPostBookmarked(post);
	}

	/**
	 * Returns all currently known bookmarked posts.
	 *
	 * @return All bookmarked posts
	 */
	public Set<Post> getBookmarkedPosts() {
		return database.getBookmarkedPosts();
	}

	public AlbumBuilder albumBuilder() {
		return database.newAlbumBuilder();
	}

	/**
	 * Returns the album with the given ID, optionally creating a new album if
	 * an album with the given ID can not be found.
	 *
	 * @param albumId
	 *            The ID of the album
	 * @return The album with the given ID, or {@code null} if no album with the
	 *         given ID exists
	 */
	@Nullable
	public Album getAlbum(@Nonnull String albumId) {
		return database.getAlbum(albumId);
	}

	public ImageBuilder imageBuilder() {
		return database.newImageBuilder();
	}

	/**
	 * Returns the image with the given ID, creating it if necessary.
	 *
	 * @param imageId
	 *            The ID of the image
	 * @return The image with the given ID
	 */
	@Nullable
	public Image getImage(String imageId) {
		return getImage(imageId, true);
	}

	/**
	 * Returns the image with the given ID, optionally creating it if it does
	 * not exist.
	 *
	 * @param imageId
	 *            The ID of the image
	 * @param create
	 *            {@code true} to create an image if none exists with the given
	 *            ID
	 * @return The image with the given ID, or {@code null} if none exists and
	 *         none was created
	 */
	@Nullable
	public Image getImage(String imageId, boolean create) {
		Image image = database.getImage(imageId);
		if (image != null) {
			return image;
		}
		if (!create) {
			return null;
		}
		Image newImage = database.newImageBuilder().withId(imageId).build();
		database.storeImage(newImage);
		return newImage;
	}

	/**
	 * Returns the temporary image with the given ID.
	 *
	 * @param imageId
	 *            The ID of the temporary image
	 * @return The temporary image, or {@code null} if there is no temporary
	 *         image with the given ID
	 */
	public TemporaryImage getTemporaryImage(String imageId) {
		synchronized (temporaryImages) {
			return temporaryImages.get(imageId);
		}
	}

	//
	// ACTIONS
	//

	/**
	 * Locks the given Sone. A locked Sone will not be inserted by
	 * {@link SoneInserter} until it is {@link #unlockSone(Sone) unlocked}
	 * again.
	 *
	 * @param sone
	 *            The sone to lock
	 */
	public void lockSone(Sone sone) {
		synchronized (lockedSones) {
			if (lockedSones.add(sone)) {
				eventBus.post(new SoneLockedEvent(sone));
			}
		}
	}

	/**
	 * Unlocks the given Sone.
	 *
	 * @see #lockSone(Sone)
	 * @param sone
	 *            The sone to unlock
	 */
	public void unlockSone(Sone sone) {
		synchronized (lockedSones) {
			if (lockedSones.remove(sone)) {
				eventBus.post(new SoneUnlockedEvent(sone));
			}
		}
	}

	/**
	 * Adds a local Sone from the given own identity.
	 *
	 * @param ownIdentity
	 *            The own identity to create a Sone from
	 * @return The added (or already existing) Sone
	 */
	public Sone addLocalSone(OwnIdentity ownIdentity) {
		if (ownIdentity == null) {
			logger.log(Level.WARNING, "Given OwnIdentity is null!");
			return null;
		}
		logger.info(String.format("Adding Sone from OwnIdentity: %s", ownIdentity));
		Sone sone = database.newSoneBuilder().local().from(ownIdentity).build();
		String property = fromNullable(ownIdentity.getProperty("Sone.LatestEdition")).or("0");
		sone.setLatestEdition(fromNullable(tryParse(property)).or(0L));
		sone.setClient(new Client("Sone", SonePlugin.getPluginVersion()));
		sone.setKnown(true);
		SoneInserter soneInserter = new SoneInserter(this, eventBus, freenetInterface, metricRegistry, soneUriCreator, ownIdentity.getId());
		soneInserter.insertionDelayChanged(new InsertionDelayChangedEvent(preferences.getInsertionDelay()));
		eventBus.register(soneInserter);
		synchronized (soneInserters) {
			soneInserters.put(sone, soneInserter);
		}
		loadSone(sone);
		database.storeSone(sone);
		sone.setStatus(SoneStatus.idle);
		if (sone.getPosts().isEmpty() && sone.getReplies().isEmpty() && getAllImages(sone.getRootAlbum()).isEmpty()) {
			// dirty hack
			lockSone(sone);
			eventBus.post(new SoneLockedOnStartup(sone));
		}
		soneInserter.start();
		return sone;
	}

	/**
	 * Creates a new Sone for the given own identity.
	 *
	 * @param ownIdentity
	 *            The own identity to create a Sone for
	 * @return The created Sone
	 */
	public Sone createSone(OwnIdentity ownIdentity) {
		if (!webOfTrustUpdater.addContextWait(ownIdentity, "Sone")) {
			logger.log(Level.SEVERE, String.format("Could not add “Sone” context to own identity: %s", ownIdentity));
			return null;
		}
		Sone sone = addLocalSone(ownIdentity);

		followSone(sone, "nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI");
		touchConfiguration();
		return sone;
	}

	/**
	 * Adds the Sone of the given identity.
	 *
	 * @param identity
	 *            The identity whose Sone to add
	 * @return The added or already existing Sone
	 */
	public Sone addRemoteSone(Identity identity) {
		if (identity == null) {
			logger.log(Level.WARNING, "Given Identity is null!");
			return null;
		}
		String property = fromNullable(identity.getProperty("Sone.LatestEdition")).or("0");
		long latestEdition = fromNullable(tryParse(property)).or(0L);
		Sone existingSone = getSone(identity.getId());
		if ((existingSone != null )&& existingSone.isLocal()) {
			return existingSone;
		}
		boolean newSone = existingSone == null;
		Sone sone = !newSone ? existingSone : database.newSoneBuilder().from(identity).build();
		sone.setLatestEdition(latestEdition);
		if (newSone) {
			synchronized (knownSones) {
				newSone = !knownSones.contains(sone.getId());
			}
			sone.setKnown(!newSone);
			if (newSone) {
				eventBus.post(new NewSoneFoundEvent(sone));
				for (Sone localSone : getLocalSones()) {
					if (localSone.getOptions().isAutoFollow()) {
						followSone(localSone, sone.getId());
					}
				}
			}
		}
		database.storeSone(sone);
		soneDownloader.addSone(sone);
		soneDownloaders.execute(soneDownloader.fetchSoneAsUskAction(sone));
		return sone;
	}

	/**
	 * Lets the given local Sone follow the Sone with the given ID.
	 *
	 * @param sone
	 *            The local Sone that should follow another Sone
	 * @param soneId
	 *            The ID of the Sone to follow
	 */
	public void followSone(Sone sone, String soneId) {
		checkNotNull(sone, "sone must not be null");
		checkNotNull(soneId, "soneId must not be null");
		database.addFriend(sone, soneId);
		@SuppressWarnings("ConstantConditions") // we just followed, this can’t be null.
		long now = database.getFollowingTime(soneId);
		Sone followedSone = getSone(soneId);
		if (followedSone == null) {
			return;
		}
		for (Post post : followedSone.getPosts()) {
			if (post.getTime() < now) {
				markPostKnown(post);
			}
		}
		for (PostReply reply : followedSone.getReplies()) {
			if (reply.getTime() < now) {
				markReplyKnown(reply);
			}
		}
		touchConfiguration();
	}

	/**
	 * Lets the given local Sone unfollow the Sone with the given ID.
	 *
	 * @param sone
	 *            The local Sone that should unfollow another Sone
	 * @param soneId
	 *            The ID of the Sone being unfollowed
	 */
	public void unfollowSone(Sone sone, String soneId) {
		checkNotNull(sone, "sone must not be null");
		checkNotNull(soneId, "soneId must not be null");
		database.removeFriend(sone, soneId);
		touchConfiguration();
	}

	/**
	 * Updates the stored Sone with the given Sone.
	 *
	 * @param sone
	 *            The updated Sone
	 */
	public void updateSone(Sone sone) {
		updateSone(sone, false);
	}

	/**
	 * Updates the stored Sone with the given Sone. If {@code soneRescueMode} is
	 * {@code true}, an older Sone than the current Sone can be given to restore
	 * an old state.
	 *
	 * @param sone
	 *            The Sone to update
	 * @param soneRescueMode
	 *            {@code true} if the stored Sone should be updated regardless
	 *            of the age of the given Sone
	 */
	public void updateSone(final Sone sone, boolean soneRescueMode) {
		Sone storedSone = getSone(sone.getId());
		if (storedSone != null) {
			if (!soneRescueMode && !(sone.getTime() > storedSone.getTime())) {
				logger.log(Level.FINE, String.format("Downloaded Sone %s is not newer than stored Sone %s.", sone, storedSone));
				return;
			}
			List<Object> events =
					collectEventsForChangesInSone(storedSone, sone);
			database.storeSone(sone);
			for (Object event : events) {
				eventBus.post(event);
			}
			sone.setOptions(storedSone.getOptions());
			sone.setKnown(storedSone.isKnown());
			sone.setStatus((sone.getTime() == 0) ? SoneStatus.unknown : SoneStatus.idle);
			if (sone.isLocal()) {
				touchConfiguration();
			}
		}
	}

	private List<Object> collectEventsForChangesInSone(Sone oldSone, Sone newSone) {
		List<Object> events = new ArrayList<>();
		SoneComparison soneComparison = new SoneComparison(oldSone, newSone);
		for (Post newPost : soneComparison.getNewPosts()) {
			if (newPost.getSone().equals(newSone)) {
				newPost.setKnown(true);
			} else if (newPost.getTime() < database.getFollowingTime(newSone.getId())) {
				newPost.setKnown(true);
			} else if (!newPost.isKnown()) {
				events.add(new NewPostFoundEvent(newPost));
			}
		}
		for (Post post : soneComparison.getRemovedPosts()) {
			events.add(new PostRemovedEvent(post));
		}
		for (PostReply postReply : soneComparison.getNewPostReplies()) {
			if (postReply.getSone().equals(newSone)) {
				database.setPostReplyKnown(postReply);
			} else if (postReply.getTime() < database.getFollowingTime(newSone.getId())) {
				database.setPostReplyKnown(postReply);
			} else if (!postReply.isKnown()) {
				events.add(new NewPostReplyFoundEvent(postReply));
			}
		}
		for (PostReply postReply : soneComparison.getRemovedPostReplies()) {
			events.add(new PostReplyRemovedEvent(postReply));
		}
		return events;
	}

	/**
	 * Deletes the given Sone. This will remove the Sone from the
	 * {@link #getLocalSones() local Sones}, stop its {@link SoneInserter} and
	 * remove the context from its identity.
	 *
	 * @param sone
	 *            The Sone to delete
	 */
	public void deleteSone(Sone sone) {
		if (!(sone.getIdentity() instanceof OwnIdentity)) {
			logger.log(Level.WARNING, String.format("Tried to delete Sone of non-own identity: %s", sone));
			return;
		}
		if (!getLocalSones().contains(sone)) {
			logger.log(Level.WARNING, String.format("Tried to delete non-local Sone: %s", sone));
			return;
		}
		SoneInserter soneInserter = soneInserters.remove(sone);
		soneInserter.stop();
		database.removeSone(sone);
		webOfTrustUpdater.removeContext((OwnIdentity) sone.getIdentity(), "Sone");
		webOfTrustUpdater.removeProperty((OwnIdentity) sone.getIdentity(), "Sone.LatestEdition");
		try {
			configuration.getLongValue("Sone/" + sone.getId() + "/Time").setValue(null);
		} catch (ConfigurationException ce1) {
			logger.log(Level.WARNING, "Could not remove Sone from configuration!", ce1);
		}
	}

	/**
	 * Marks the given Sone as known. If the Sone was not {@link Post#isKnown()
	 * known} before, a {@link MarkSoneKnownEvent} is fired.
	 *
	 * @param sone
	 *            The Sone to mark as known
	 */
	public void markSoneKnown(Sone sone) {
		if (!sone.isKnown()) {
			sone.setKnown(true);
			synchronized (knownSones) {
				knownSones.add(sone.getId());
			}
			eventBus.post(new MarkSoneKnownEvent(sone));
			touchConfiguration();
		}
	}

	/**
	 * Loads and updates the given Sone from the configuration. If any error is
	 * encountered, loading is aborted and the given Sone is not changed.
	 *
	 * @param sone
	 *            The Sone to load and update
	 */
	public void loadSone(Sone sone) {
		if (!sone.isLocal()) {
			logger.log(Level.FINE, String.format("Tried to load non-local Sone: %s", sone));
			return;
		}
		logger.info(String.format("Loading local Sone: %s", sone));

		/* load Sone. */
		String sonePrefix = "Sone/" + sone.getId();
		Long soneTime = configuration.getLongValue(sonePrefix + "/Time").getValue(null);
		if (soneTime == null) {
			logger.log(Level.INFO, "Could not load Sone because no Sone has been saved.");
			return;
		}
		String lastInsertFingerprint = configuration.getStringValue(sonePrefix + "/LastInsertFingerprint").getValue("");

		/* load profile. */
		ConfigurationSoneParser configurationSoneParser = new ConfigurationSoneParser(configuration, sone);
		Profile profile = configurationSoneParser.parseProfile();

		/* load posts. */
		Collection<Post> posts;
		try {
			posts = configurationSoneParser.parsePosts(database);
		} catch (InvalidPostFound ipf) {
			logger.log(Level.WARNING, "Invalid post found, aborting load!");
			return;
		}

		/* load replies. */
		Collection<PostReply> replies;
		try {
			replies = configurationSoneParser.parsePostReplies(database);
		} catch (InvalidPostReplyFound iprf) {
			logger.log(Level.WARNING, "Invalid reply found, aborting load!");
			return;
		}

		/* load post likes. */
		Set<String> likedPostIds =
				configurationSoneParser.parseLikedPostIds();

		/* load reply likes. */
		Set<String> likedReplyIds =
				configurationSoneParser.parseLikedPostReplyIds();

		/* load albums. */
		List<Album> topLevelAlbums;
		try {
			topLevelAlbums =
					configurationSoneParser.parseTopLevelAlbums(database);
		} catch (InvalidAlbumFound iaf) {
			logger.log(Level.WARNING, "Invalid album found, aborting load!");
			return;
		} catch (InvalidParentAlbumFound ipaf) {
			logger.log(Level.WARNING, format("Invalid parent album ID: %s",
					ipaf.getAlbumParentId()));
			return;
		}

		/* load images. */
		try {
			configurationSoneParser.parseImages(database);
		} catch (InvalidImageFound iif) {
			logger.log(WARNING, "Invalid image found, aborting load!");
			return;
		} catch (InvalidParentAlbumFound ipaf) {
			logger.log(Level.WARNING,
					format("Invalid album image (%s) encountered, aborting load!",
							ipaf.getAlbumParentId()));
			return;
		}

		/* load avatar. */
		String avatarId = configuration.getStringValue(sonePrefix + "/Profile/Avatar").getValue(null);
		if (avatarId != null) {
			final Map<String, Image> images =
					configurationSoneParser.getImages();
			profile.setAvatar(images.get(avatarId));
		}

		/* load options. */
		sone.getOptions().setAutoFollow(configuration.getBooleanValue(sonePrefix + "/Options/AutoFollow").getValue(false));
		sone.getOptions().setSoneInsertNotificationEnabled(configuration.getBooleanValue(sonePrefix + "/Options/EnableSoneInsertNotifications").getValue(false));
		sone.getOptions().setShowNewSoneNotifications(configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewSones").getValue(true));
		sone.getOptions().setShowNewPostNotifications(configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewPosts").getValue(true));
		sone.getOptions().setShowNewReplyNotifications(configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewReplies").getValue(true));
		sone.getOptions().setShowCustomAvatars(LoadExternalContent.valueOf(configuration.getStringValue(sonePrefix + "/Options/ShowCustomAvatars").getValue(LoadExternalContent.NEVER.name())));
		sone.getOptions().setLoadLinkedImages(LoadExternalContent.valueOf(configuration.getStringValue(sonePrefix + "/Options/LoadLinkedImages").getValue(LoadExternalContent.NEVER.name())));

		/* if we’re still here, Sone was loaded successfully. */
		synchronized (sone) {
			sone.setTime(soneTime);
			sone.setProfile(profile);
			sone.setPosts(posts);
			sone.setReplies(replies);
			sone.setLikePostIds(likedPostIds);
			sone.setLikeReplyIds(likedReplyIds);
			for (Album album : sone.getRootAlbum().getAlbums()) {
				sone.getRootAlbum().removeAlbum(album);
			}
			for (Album album : topLevelAlbums) {
				sone.getRootAlbum().addAlbum(album);
			}
			synchronized (soneInserters) {
				soneInserters.get(sone).setLastInsertFingerprint(lastInsertFingerprint);
			}
		}
		for (Post post : posts) {
			post.setKnown(true);
		}
		for (PostReply reply : replies) {
			database.setPostReplyKnown(reply);
		}

		logger.info(String.format("Sone loaded successfully: %s", sone));
	}

	/**
	 * Creates a new post.
	 *
	 * @param sone
	 *            The Sone that creates the post
	 * @param recipient
	 *            The recipient Sone, or {@code null} if this post does not have
	 *            a recipient
	 * @param text
	 *            The text of the post
	 * @return The created post
	 */
	public Post createPost(Sone sone, @Nullable Sone recipient, String text) {
		checkNotNull(text, "text must not be null");
		checkArgument(text.trim().length() > 0, "text must not be empty");
		if (!sone.isLocal()) {
			logger.log(Level.FINE, String.format("Tried to create post for non-local Sone: %s", sone));
			return null;
		}
		PostBuilder postBuilder = database.newPostBuilder();
		postBuilder.from(sone.getId()).randomId().currentTime().withText(text.trim());
		if (recipient != null) {
			postBuilder.to(recipient.getId());
		}
		final Post post = postBuilder.build();
		database.storePost(post);
		eventBus.post(new NewPostFoundEvent(post));
		sone.addPost(post);
		touchConfiguration();
		localElementTicker.schedule(new MarkPostKnown(post), 10, TimeUnit.SECONDS);
		return post;
	}

	/**
	 * Deletes the given post.
	 *
	 * @param post
	 *            The post to delete
	 */
	public void deletePost(Post post) {
		if (!post.getSone().isLocal()) {
			logger.log(Level.WARNING, String.format("Tried to delete post of non-local Sone: %s", post.getSone()));
			return;
		}
		database.removePost(post);
		eventBus.post(new PostRemovedEvent(post));
		markPostKnown(post);
		touchConfiguration();
	}

	/**
	 * Marks the given post as known, if it is currently not a known post
	 * (according to {@link Post#isKnown()}).
	 *
	 * @param post
	 *            The post to mark as known
	 */
	public void markPostKnown(Post post) {
		post.setKnown(true);
		eventBus.post(new MarkPostKnownEvent(post));
		touchConfiguration();
		for (PostReply reply : getReplies(post.getId())) {
			markReplyKnown(reply);
		}
	}

	public void bookmarkPost(Post post) {
		database.bookmarkPost(post);
	}

	/**
	 * Removes the given post from the bookmarks.
	 *
	 * @param post
	 *            The post to unbookmark
	 */
	public void unbookmarkPost(Post post) {
		database.unbookmarkPost(post);
	}

	/**
	 * Creates a new reply.
	 *
	 * @param sone
	 *            The Sone that creates the reply
	 * @param post
	 *            The post that this reply refers to
	 * @param text
	 *            The text of the reply
	 * @return The created reply
	 */
	public PostReply createReply(Sone sone, Post post, String text) {
		checkNotNull(text, "text must not be null");
		checkArgument(text.trim().length() > 0, "text must not be empty");
		if (!sone.isLocal()) {
			logger.log(Level.FINE, String.format("Tried to create reply for non-local Sone: %s", sone));
			return null;
		}
		PostReplyBuilder postReplyBuilder = postReplyBuilder();
		postReplyBuilder.randomId().from(sone.getId()).to(post.getId()).currentTime().withText(text.trim());
		final PostReply reply = postReplyBuilder.build();
		database.storePostReply(reply);
		eventBus.post(new NewPostReplyFoundEvent(reply));
		sone.addReply(reply);
		touchConfiguration();
		localElementTicker.schedule(new MarkReplyKnown(reply), 10, TimeUnit.SECONDS);
		return reply;
	}

	/**
	 * Deletes the given reply.
	 *
	 * @param reply
	 *            The reply to delete
	 */
	public void deleteReply(PostReply reply) {
		Sone sone = reply.getSone();
		if (!sone.isLocal()) {
			logger.log(Level.FINE, String.format("Tried to delete non-local reply: %s", reply));
			return;
		}
		database.removePostReply(reply);
		markReplyKnown(reply);
		sone.removeReply(reply);
		touchConfiguration();
	}

	/**
	 * Marks the given reply as known, if it is currently not a known reply
	 * (according to {@link Reply#isKnown()}).
	 *
	 * @param reply
	 *            The reply to mark as known
	 */
	public void markReplyKnown(PostReply reply) {
		boolean previouslyKnown = reply.isKnown();
		database.setPostReplyKnown(reply);
		eventBus.post(new MarkPostReplyKnownEvent(reply));
		if (!previouslyKnown) {
			touchConfiguration();
		}
	}

	/**
	 * Creates a new album for the given Sone.
	 *
	 * @param sone
	 *            The Sone to create the album for
	 * @param parent
	 *            The parent of the album (may be {@code null} to create a
	 *            top-level album)
	 * @return The new album
	 */
	public Album createAlbum(Sone sone, Album parent) {
		Album album = database.newAlbumBuilder().randomId().by(sone).build();
		database.storeAlbum(album);
		parent.addAlbum(album);
		return album;
	}

	/**
	 * Deletes the given album. The owner of the album has to be a local Sone,
	 * and the album has to be {@link Album#isEmpty() empty} to be deleted.
	 *
	 * @param album
	 *            The album to remove
	 */
	public void deleteAlbum(Album album) {
		checkNotNull(album, "album must not be null");
		checkArgument(album.getSone().isLocal(), "album’s Sone must be a local Sone");
		if (!album.isEmpty()) {
			return;
		}
		album.getParent().removeAlbum(album);
		database.removeAlbum(album);
		touchConfiguration();
	}

	/**
	 * Creates a new image.
	 *
	 * @param sone
	 *            The Sone creating the image
	 * @param album
	 *            The album the image will be inserted into
	 * @param temporaryImage
	 *            The temporary image to create the image from
	 * @return The newly created image
	 */
	public Image createImage(Sone sone, Album album, TemporaryImage temporaryImage) {
		checkNotNull(sone, "sone must not be null");
		checkNotNull(album, "album must not be null");
		checkNotNull(temporaryImage, "temporaryImage must not be null");
		checkArgument(sone.isLocal(), "sone must be a local Sone");
		checkArgument(sone.equals(album.getSone()), "album must belong to the given Sone");
		Image image = database.newImageBuilder().withId(temporaryImage.getId()).build().modify().setSone(sone).setCreationTime(System.currentTimeMillis()).update();
		album.addImage(image);
		database.storeImage(image);
		imageInserter.insertImage(temporaryImage, image);
		return image;
	}

	/**
	 * Deletes the given image. This method will also delete a matching
	 * temporary image.
	 *
	 * @see #deleteTemporaryImage(String)
	 * @param image
	 *            The image to delete
	 */
	public void deleteImage(Image image) {
		checkNotNull(image, "image must not be null");
		checkArgument(image.getSone().isLocal(), "image must belong to a local Sone");
		deleteTemporaryImage(image.getId());
		image.getAlbum().removeImage(image);
		database.removeImage(image);
		touchConfiguration();
	}

	/**
	 * Creates a new temporary image.
	 *
	 * @param mimeType
	 *            The MIME type of the temporary image
	 * @param imageData
	 *            The encoded data of the image
	 * @return The temporary image
	 */
	public TemporaryImage createTemporaryImage(String mimeType, byte[] imageData) {
		TemporaryImage temporaryImage = new TemporaryImage();
		temporaryImage.setMimeType(mimeType).setImageData(imageData);
		synchronized (temporaryImages) {
			temporaryImages.put(temporaryImage.getId(), temporaryImage);
		}
		return temporaryImage;
	}

	/**
	 * Deletes the temporary image with the given ID.
	 *
	 * @param imageId
	 *            The ID of the temporary image to delete
	 */
	public void deleteTemporaryImage(String imageId) {
		checkNotNull(imageId, "imageId must not be null");
		synchronized (temporaryImages) {
			temporaryImages.remove(imageId);
		}
		Image image = getImage(imageId, false);
		if (image != null) {
			imageInserter.cancelImageInsert(image);
		}
	}

	/**
	 * Notifies the core that the configuration, either of the core or of a
	 * single local Sone, has changed, and that the configuration should be
	 * saved.
	 */
	public void touchConfiguration() {
		lastConfigurationUpdate = System.currentTimeMillis();
	}

	//
	// SERVICE METHODS
	//

	/**
	 * Starts the core.
	 */
	@Override
	public void serviceStart() {
		loadConfiguration();
		updateChecker.start();
		identityManager.start();
		webOfTrustUpdater.init();
		webOfTrustUpdater.start();
		database.startAsync();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serviceRun() {
		long lastSaved = System.currentTimeMillis();
		while (!shouldStop()) {
			sleep(1000);
			long now = System.currentTimeMillis();
			if (shouldStop() || ((lastConfigurationUpdate > lastSaved) && ((now - lastConfigurationUpdate) > 5000))) {
				for (Sone localSone : getLocalSones()) {
					saveSone(localSone);
				}
				saveConfiguration();
				lastSaved = now;
			}
		}
	}

	/**
	 * Stops the core.
	 */
	@Override
	public void serviceStop() {
		localElementTicker.shutdownNow();
		synchronized (soneInserters) {
			for (Entry<Sone, SoneInserter> soneInserter : soneInserters.entrySet()) {
				soneInserter.getValue().stop();
				Sone latestSone = getLocalSone(soneInserter.getKey().getId());
				saveSone(latestSone);
			}
		}
		synchronized (soneRescuers) {
			for (SoneRescuer soneRescuer : soneRescuers.values()) {
				soneRescuer.stop();
			}
		}
		saveConfiguration();
		database.stopAsync();
		webOfTrustUpdater.stop();
		updateChecker.stop();
		soneDownloader.stop();
		soneDownloaders.shutdown();
		identityManager.stop();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Saves the given Sone. This will persist all local settings for the given
	 * Sone, such as the friends list and similar, private options.
	 *
	 * @param sone
	 *            The Sone to save
	 */
	private synchronized void saveSone(Sone sone) {
		if (!sone.isLocal()) {
			logger.log(Level.FINE, String.format("Tried to save non-local Sone: %s", sone));
			return;
		}
		if (!(sone.getIdentity() instanceof OwnIdentity)) {
			logger.log(Level.WARNING, String.format("Local Sone without OwnIdentity found, refusing to save: %s", sone));
			return;
		}

		logger.log(Level.INFO, String.format("Saving Sone: %s", sone));
		try {
			/* save Sone into configuration. */
			String sonePrefix = "Sone/" + sone.getId();
			configuration.getLongValue(sonePrefix + "/Time").setValue(sone.getTime());
			configuration.getStringValue(sonePrefix + "/LastInsertFingerprint").setValue(soneInserters.get(sone).getLastInsertFingerprint());

			/* save profile. */
			Profile profile = sone.getProfile();
			configuration.getStringValue(sonePrefix + "/Profile/FirstName").setValue(profile.getFirstName());
			configuration.getStringValue(sonePrefix + "/Profile/MiddleName").setValue(profile.getMiddleName());
			configuration.getStringValue(sonePrefix + "/Profile/LastName").setValue(profile.getLastName());
			configuration.getIntValue(sonePrefix + "/Profile/BirthDay").setValue(profile.getBirthDay());
			configuration.getIntValue(sonePrefix + "/Profile/BirthMonth").setValue(profile.getBirthMonth());
			configuration.getIntValue(sonePrefix + "/Profile/BirthYear").setValue(profile.getBirthYear());
			configuration.getStringValue(sonePrefix + "/Profile/Avatar").setValue(profile.getAvatar());

			/* save profile fields. */
			int fieldCounter = 0;
			for (Field profileField : profile.getFields()) {
				String fieldPrefix = sonePrefix + "/Profile/Fields/" + fieldCounter++;
				configuration.getStringValue(fieldPrefix + "/Name").setValue(profileField.getName());
				configuration.getStringValue(fieldPrefix + "/Value").setValue(profileField.getValue());
			}
			configuration.getStringValue(sonePrefix + "/Profile/Fields/" + fieldCounter + "/Name").setValue(null);

			/* save posts. */
			int postCounter = 0;
			for (Post post : sone.getPosts()) {
				String postPrefix = sonePrefix + "/Posts/" + postCounter++;
				configuration.getStringValue(postPrefix + "/ID").setValue(post.getId());
				configuration.getStringValue(postPrefix + "/Recipient").setValue(post.getRecipientId().orNull());
				configuration.getLongValue(postPrefix + "/Time").setValue(post.getTime());
				configuration.getStringValue(postPrefix + "/Text").setValue(post.getText());
			}
			configuration.getStringValue(sonePrefix + "/Posts/" + postCounter + "/ID").setValue(null);

			/* save replies. */
			int replyCounter = 0;
			for (PostReply reply : sone.getReplies()) {
				String replyPrefix = sonePrefix + "/Replies/" + replyCounter++;
				configuration.getStringValue(replyPrefix + "/ID").setValue(reply.getId());
				configuration.getStringValue(replyPrefix + "/Post/ID").setValue(reply.getPostId());
				configuration.getLongValue(replyPrefix + "/Time").setValue(reply.getTime());
				configuration.getStringValue(replyPrefix + "/Text").setValue(reply.getText());
			}
			configuration.getStringValue(sonePrefix + "/Replies/" + replyCounter + "/ID").setValue(null);

			/* save post likes. */
			int postLikeCounter = 0;
			for (String postId : sone.getLikedPostIds()) {
				configuration.getStringValue(sonePrefix + "/Likes/Post/" + postLikeCounter++ + "/ID").setValue(postId);
			}
			configuration.getStringValue(sonePrefix + "/Likes/Post/" + postLikeCounter + "/ID").setValue(null);

			/* save reply likes. */
			int replyLikeCounter = 0;
			for (String replyId : sone.getLikedReplyIds()) {
				configuration.getStringValue(sonePrefix + "/Likes/Reply/" + replyLikeCounter++ + "/ID").setValue(replyId);
			}
			configuration.getStringValue(sonePrefix + "/Likes/Reply/" + replyLikeCounter + "/ID").setValue(null);

			/* save albums. first, collect in a flat structure, top-level first. */
			List<Album> albums = SoneKt.getAllAlbums(sone);

			int albumCounter = 0;
			for (Album album : albums) {
				String albumPrefix = sonePrefix + "/Albums/" + albumCounter++;
				configuration.getStringValue(albumPrefix + "/ID").setValue(album.getId());
				configuration.getStringValue(albumPrefix + "/Title").setValue(album.getTitle());
				configuration.getStringValue(albumPrefix + "/Description").setValue(album.getDescription());
				configuration.getStringValue(albumPrefix + "/Parent").setValue(album.getParent().equals(sone.getRootAlbum()) ? null : album.getParent().getId());
			}
			configuration.getStringValue(sonePrefix + "/Albums/" + albumCounter + "/ID").setValue(null);

			/* save images. */
			int imageCounter = 0;
			for (Album album : albums) {
				for (Image image : album.getImages()) {
					if (!image.isInserted()) {
						continue;
					}
					String imagePrefix = sonePrefix + "/Images/" + imageCounter++;
					configuration.getStringValue(imagePrefix + "/ID").setValue(image.getId());
					configuration.getStringValue(imagePrefix + "/Album").setValue(album.getId());
					configuration.getStringValue(imagePrefix + "/Key").setValue(image.getKey());
					configuration.getStringValue(imagePrefix + "/Title").setValue(image.getTitle());
					configuration.getStringValue(imagePrefix + "/Description").setValue(image.getDescription());
					configuration.getLongValue(imagePrefix + "/CreationTime").setValue(image.getCreationTime());
					configuration.getIntValue(imagePrefix + "/Width").setValue(image.getWidth());
					configuration.getIntValue(imagePrefix + "/Height").setValue(image.getHeight());
				}
			}
			configuration.getStringValue(sonePrefix + "/Images/" + imageCounter + "/ID").setValue(null);

			/* save options. */
			configuration.getBooleanValue(sonePrefix + "/Options/AutoFollow").setValue(sone.getOptions().isAutoFollow());
			configuration.getBooleanValue(sonePrefix + "/Options/EnableSoneInsertNotifications").setValue(sone.getOptions().isSoneInsertNotificationEnabled());
			configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewSones").setValue(sone.getOptions().isShowNewSoneNotifications());
			configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewPosts").setValue(sone.getOptions().isShowNewPostNotifications());
			configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewReplies").setValue(sone.getOptions().isShowNewReplyNotifications());
			configuration.getStringValue(sonePrefix + "/Options/ShowCustomAvatars").setValue(sone.getOptions().getShowCustomAvatars().name());
			configuration.getStringValue(sonePrefix + "/Options/LoadLinkedImages").setValue(sone.getOptions().getLoadLinkedImages().name());

			webOfTrustUpdater.setProperty((OwnIdentity) sone.getIdentity(), "Sone.LatestEdition", String.valueOf(sone.getLatestEdition()));

			logger.log(Level.INFO, String.format("Sone %s saved.", sone));
		} catch (ConfigurationException ce1) {
			logger.log(Level.WARNING, String.format("Could not save Sone: %s", sone), ce1);
		}
	}

	/**
	 * Saves the current options.
	 */
	private void saveConfiguration() {
		synchronized (configuration) {
			if (storingConfiguration) {
				logger.log(Level.FINE, "Already storing configuration…");
				return;
			}
			storingConfiguration = true;
		}

		/* store the options first. */
		try {
			preferences.saveTo(configuration);

			/* save known Sones. */
			int soneCounter = 0;
			synchronized (knownSones) {
				for (String knownSoneId : knownSones) {
					configuration.getStringValue("KnownSone/" + soneCounter++ + "/ID").setValue(knownSoneId);
				}
				configuration.getStringValue("KnownSone/" + soneCounter + "/ID").setValue(null);
			}

			/* save known posts. */
			database.save();

			/* now save it. */
			Stopwatch stopwatch = Stopwatch.createStarted();
			configuration.save();
			configurationSaveTimeHistogram.update(stopwatch.elapsed(TimeUnit.MICROSECONDS));

		} catch (ConfigurationException ce1) {
			logger.log(Level.SEVERE, "Could not store configuration!", ce1);
		} catch (DatabaseException de1) {
			logger.log(Level.SEVERE, "Could not save database!", de1);
		} finally {
			synchronized (configuration) {
				storingConfiguration = false;
			}
		}
	}

	/**
	 * Loads the configuration.
	 */
	private void loadConfiguration() {
		new PreferencesLoader(preferences).loadFrom(configuration);

		/* load known Sones. */
		int soneCounter = 0;
		while (true) {
			String knownSoneId = configuration.getStringValue("KnownSone/" + soneCounter++ + "/ID").getValue(null);
			if (knownSoneId == null) {
				break;
			}
			synchronized (knownSones) {
				knownSones.add(knownSoneId);
			}
		}
	}

	/**
	 * Notifies the core that a new {@link OwnIdentity} was added.
	 *
	 * @param ownIdentityAddedEvent
	 *            The event
	 */
	@Subscribe
	public void ownIdentityAdded(OwnIdentityAddedEvent ownIdentityAddedEvent) {
		OwnIdentity ownIdentity = ownIdentityAddedEvent.getOwnIdentity();
		logger.log(Level.FINEST, String.format("Adding OwnIdentity: %s", ownIdentity));
		if (ownIdentity.hasContext("Sone")) {
			addLocalSone(ownIdentity);
		}
	}

	/**
	 * Notifies the core that an {@link OwnIdentity} was removed.
	 *
	 * @param ownIdentityRemovedEvent
	 *            The event
	 */
	@Subscribe
	public void ownIdentityRemoved(OwnIdentityRemovedEvent ownIdentityRemovedEvent) {
		OwnIdentity ownIdentity = ownIdentityRemovedEvent.getOwnIdentity();
		logger.log(Level.FINEST, String.format("Removing OwnIdentity: %s", ownIdentity));
		trustedIdentities.removeAll(ownIdentity);
	}

	/**
	 * Notifies the core that a new {@link Identity} was added.
	 *
	 * @param identityAddedEvent
	 *            The event
	 */
	@Subscribe
	public void identityAdded(IdentityAddedEvent identityAddedEvent) {
		Identity identity = identityAddedEvent.getIdentity();
		logger.log(Level.FINEST, String.format("Adding Identity: %s", identity));
		trustedIdentities.put(identityAddedEvent.getOwnIdentity(), identity);
		addRemoteSone(identity);
	}

	/**
	 * Notifies the core that an {@link Identity} was updated.
	 *
	 * @param identityUpdatedEvent
	 *            The event
	 */
	@Subscribe
	public void identityUpdated(IdentityUpdatedEvent identityUpdatedEvent) {
		Identity identity = identityUpdatedEvent.getIdentity();
		final Sone sone = getRemoteSone(identity.getId());
		if (sone.isLocal()) {
			return;
		}
		String newLatestEdition = identity.getProperty("Sone.LatestEdition");
		if (newLatestEdition != null) {
			Long parsedNewLatestEdition = tryParse(newLatestEdition);
			if (parsedNewLatestEdition != null) {
				sone.setLatestEdition(parsedNewLatestEdition);
			}
		}
		soneDownloader.addSone(sone);
		soneDownloaders.execute(soneDownloader.fetchSoneAsSskAction(sone));
	}

	/**
	 * Notifies the core that an {@link Identity} was removed.
	 *
	 * @param identityRemovedEvent
	 *            The event
	 */
	@Subscribe
	public void identityRemoved(IdentityRemovedEvent identityRemovedEvent) {
		OwnIdentity ownIdentity = identityRemovedEvent.getOwnIdentity();
		Identity identity = identityRemovedEvent.getIdentity();
		trustedIdentities.remove(ownIdentity, identity);
		for (Entry<OwnIdentity, Collection<Identity>> trustedIdentity : trustedIdentities.asMap().entrySet()) {
			if (trustedIdentity.getKey().equals(ownIdentity)) {
				continue;
			}
			if (trustedIdentity.getValue().contains(identity)) {
				return;
			}
		}
		Sone sone = getSone(identity.getId());
		if (sone == null) {
			/* TODO - we don’t have the Sone anymore. should this happen? */
			return;
		}
		for (PostReply postReply : sone.getReplies()) {
			eventBus.post(new PostReplyRemovedEvent(postReply));
		}
		for (Post post : sone.getPosts()) {
			eventBus.post(new PostRemovedEvent(post));
		}
		eventBus.post(new SoneRemovedEvent(sone));
		database.removeSone(sone);
	}

	/**
	 * Deletes the temporary image.
	 *
	 * @param imageInsertFinishedEvent
	 *            The event
	 */
	@Subscribe
	public void imageInsertFinished(ImageInsertFinishedEvent imageInsertFinishedEvent) {
		logger.log(Level.WARNING, String.format("Image insert finished for %s: %s", imageInsertFinishedEvent.getImage(), imageInsertFinishedEvent.getResultingUri()));
		imageInsertFinishedEvent.getImage().modify().setKey(imageInsertFinishedEvent.getResultingUri().toString()).update();
		deleteTemporaryImage(imageInsertFinishedEvent.getImage().getId());
		touchConfiguration();
	}

	@VisibleForTesting
	class MarkPostKnown implements Runnable {

		private final Post post;

		public MarkPostKnown(Post post) {
			this.post = post;
		}

		@Override
		public void run() {
			markPostKnown(post);
		}

	}

	@VisibleForTesting
	class MarkReplyKnown implements Runnable {

		private final PostReply postReply;

		public MarkReplyKnown(PostReply postReply) {
			this.postReply = postReply;
		}

		@Override
		public void run() {
			markReplyKnown(postReply);
		}

	}

}
