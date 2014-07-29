/*
 * Sone - Core.java - Copyright © 2010–2013 David Roden
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static net.pterodactylus.sone.data.Sone.LOCAL_SONE_FILTER;

import java.net.MalformedURLException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Options.DefaultOption;
import net.pterodactylus.sone.core.Options.Option;
import net.pterodactylus.sone.core.Options.OptionWatcher;
import net.pterodactylus.sone.core.SoneInserter.SetInsertionDelay;
import net.pterodactylus.sone.core.event.ImageInsertFinishedEvent;
import net.pterodactylus.sone.core.event.MarkPostKnownEvent;
import net.pterodactylus.sone.core.event.MarkPostReplyKnownEvent;
import net.pterodactylus.sone.core.event.MarkSoneKnownEvent;
import net.pterodactylus.sone.core.event.NewPostFoundEvent;
import net.pterodactylus.sone.core.event.NewPostReplyFoundEvent;
import net.pterodactylus.sone.core.event.NewSoneFoundEvent;
import net.pterodactylus.sone.core.event.PostRemovedEvent;
import net.pterodactylus.sone.core.event.PostReplyRemovedEvent;
import net.pterodactylus.sone.core.event.SoneLockedEvent;
import net.pterodactylus.sone.core.event.SoneRemovedEvent;
import net.pterodactylus.sone.core.event.SoneUnlockedEvent;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Client;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Profile.Field;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.ShowCustomAvatars;
import net.pterodactylus.sone.data.Sone.SoneStatus;
import net.pterodactylus.sone.data.SoneImpl;
import net.pterodactylus.sone.data.TemporaryImage;
import net.pterodactylus.sone.database.Database;
import net.pterodactylus.sone.database.DatabaseException;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostProvider;
import net.pterodactylus.sone.database.PostReplyBuilder;
import net.pterodactylus.sone.database.PostReplyProvider;
import net.pterodactylus.sone.database.SoneProvider;
import net.pterodactylus.sone.fcp.FcpInterface;
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired;
import net.pterodactylus.sone.fcp.FcpInterface.SetActive;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.IdentityManager;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.freenet.wot.event.IdentityAddedEvent;
import net.pterodactylus.sone.freenet.wot.event.IdentityRemovedEvent;
import net.pterodactylus.sone.freenet.wot.event.IdentityUpdatedEvent;
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityAddedEvent;
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityRemovedEvent;
import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.sone.utils.IntegerRangePredicate;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.service.AbstractService;
import net.pterodactylus.util.thread.NamedThreadFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import freenet.keys.FreenetURI;

/**
 * The Sone core.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Core extends AbstractService implements SoneProvider, PostProvider, PostReplyProvider {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(Core.class);

	/** The start time. */
	private final long startupTime = System.currentTimeMillis();

	/** The options. */
	private final Options options = new Options();

	/** The preferences. */
	private final Preferences preferences = new Preferences(options);

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

	/** The FCP interface. */
	private volatile FcpInterface fcpInterface;

	/** The times Sones were followed. */
	private final Map<String, Long> soneFollowingTimes = new HashMap<String, Long>();

	/** Locked local Sones. */
	/* synchronize on itself. */
	private final Set<Sone> lockedSones = new HashSet<Sone>();

	/** Sone inserters. */
	/* synchronize access on this on sones. */
	private final Map<Sone, SoneInserter> soneInserters = new HashMap<Sone, SoneInserter>();

	/** Sone rescuers. */
	/* synchronize access on this on sones. */
	private final Map<Sone, SoneRescuer> soneRescuers = new HashMap<Sone, SoneRescuer>();

	/** All Sones. */
	/* synchronize access on this on itself. */
	private final Map<String, Sone> sones = new HashMap<String, Sone>();

	/** All known Sones. */
	private final Set<String> knownSones = new HashSet<String>();

	/** The post database. */
	private final Database database;

	/** All bookmarked posts. */
	/* synchronize access on itself. */
	private final Set<String> bookmarkedPosts = new HashSet<String>();

	/** Trusted identities, sorted by own identities. */
	private final Multimap<OwnIdentity, Identity> trustedIdentities = Multimaps.synchronizedSetMultimap(HashMultimap.<OwnIdentity, Identity>create());

	/** All temporary images. */
	private final Map<String, TemporaryImage> temporaryImages = new HashMap<String, TemporaryImage>();

	/** Ticker for threads that mark own elements as known. */
	private final ScheduledExecutorService localElementTicker = Executors.newScheduledThreadPool(1);

	/** The time the configuration was last touched. */
	private volatile long lastConfigurationUpdate;

	/**
	 * Creates a new core.
	 *
	 * @param configuration
	 *            The configuration of the core
	 * @param freenetInterface
	 *            The freenet interface
	 * @param identityManager
	 *            The identity manager
	 * @param webOfTrustUpdater
	 *            The WebOfTrust updater
	 * @param eventBus
	 *            The event bus
	 * @param database
	 *            The database
	 */
	@Inject
	public Core(Configuration configuration, FreenetInterface freenetInterface, IdentityManager identityManager, WebOfTrustUpdater webOfTrustUpdater, EventBus eventBus, Database database) {
		super("Sone Core");
		this.configuration = configuration;
		this.freenetInterface = freenetInterface;
		this.identityManager = identityManager;
		this.soneDownloader = new SoneDownloader(this, freenetInterface);
		this.imageInserter = new ImageInserter(freenetInterface);
		this.updateChecker = new UpdateChecker(eventBus, freenetInterface);
		this.webOfTrustUpdater = webOfTrustUpdater;
		this.eventBus = eventBus;
		this.database = database;
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
	 * Sets the FCP interface to use.
	 *
	 * @param fcpInterface
	 *            The FCP interface to use
	 */
	public void setFcpInterface(FcpInterface fcpInterface) {
		this.fcpInterface = fcpInterface;
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
		synchronized (sones) {
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

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Collection<Sone> getSones() {
		synchronized (sones) {
			return ImmutableSet.copyOf(sones.values());
		}
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
	public Optional<Sone> getSone(String id) {
		synchronized (sones) {
			return Optional.fromNullable(sones.get(id));
		}
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Collection<Sone> getLocalSones() {
		synchronized (sones) {
			return FluentIterable.from(sones.values()).filter(LOCAL_SONE_FILTER).toSet();
		}
	}

	/**
	 * Returns the local Sone with the given ID, optionally creating a new Sone.
	 *
	 * @param id
	 *            The ID of the Sone
	 * @param create
	 *            {@code true} to create a new Sone if none exists,
	 *            {@code false} to return null if none exists
	 * @return The Sone with the given ID, or {@code null}
	 */
	public Sone getLocalSone(String id, boolean create) {
		synchronized (sones) {
			Sone sone = sones.get(id);
			if ((sone == null) && create) {
				sone = new SoneImpl(id, true);
				sones.put(id, sone);
			}
			if ((sone != null) && !sone.isLocal()) {
				sone = new SoneImpl(id, true);
				sones.put(id, sone);
			}
			return sone;
		}
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Collection<Sone> getRemoteSones() {
		synchronized (sones) {
			return FluentIterable.from(sones.values()).filter(not(LOCAL_SONE_FILTER)).toSet();
		}
	}

	/**
	 * Returns the remote Sone with the given ID.
	 *
	 * @param id
	 *            The ID of the remote Sone to get
	 * @param create
	 *            {@code true} to always create a Sone, {@code false} to return
	 *            {@code null} if no Sone with the given ID exists
	 * @return The Sone with the given ID
	 */
	public Sone getRemoteSone(String id, boolean create) {
		synchronized (sones) {
			Sone sone = sones.get(id);
			if ((sone == null) && create && (id != null) && (id.length() == 43)) {
				sone = new SoneImpl(id, false);
				sones.put(id, sone);
			}
			return sone;
		}
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
	 * Returns the time when the given was first followed by any local Sone.
	 *
	 * @param sone
	 *            The Sone to get the time for
	 * @return The time (in milliseconds since Jan 1, 1970) the Sone has first
	 *         been followed, or {@link Long#MAX_VALUE}
	 */
	public long getSoneFollowingTime(Sone sone) {
		synchronized (soneFollowingTimes) {
			return Optional.fromNullable(soneFollowingTimes.get(sone.getId())).or(Long.MAX_VALUE);
		}
	}

	/**
	 * Returns whether the target Sone is trusted by the origin Sone.
	 *
	 * @param origin
	 *            The origin Sone
	 * @param target
	 *            The target Sone
	 * @return {@code true} if the target Sone is trusted by the origin Sone
	 */
	public boolean isSoneTrusted(Sone origin, Sone target) {
		checkNotNull(origin, "origin must not be null");
		checkNotNull(target, "target must not be null");
		checkArgument(origin.getIdentity() instanceof OwnIdentity, "origin’s identity must be an OwnIdentity");
		return trustedIdentities.containsEntry(origin.getIdentity(), target.getIdentity());
	}

	/**
	 * Returns a post builder.
	 *
	 * @return A new post builder
	 */
	public PostBuilder postBuilder() {
		return database.newPostBuilder();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Post> getPost(String postId) {
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
	@Override
	public Optional<PostReply> getPostReply(String replyId) {
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
		Set<Sone> sones = new HashSet<Sone>();
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
		Set<Sone> sones = new HashSet<Sone>();
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
		return isPostBookmarked(post.getId());
	}

	/**
	 * Returns whether the post with the given ID is bookmarked.
	 *
	 * @param id
	 *            The ID of the post to check
	 * @return {@code true} if the post with the given ID is bookmarked,
	 *         {@code false} otherwise
	 */
	public boolean isPostBookmarked(String id) {
		synchronized (bookmarkedPosts) {
			return bookmarkedPosts.contains(id);
		}
	}

	/**
	 * Returns all currently known bookmarked posts.
	 *
	 * @return All bookmarked posts
	 */
	public Set<Post> getBookmarkedPosts() {
		Set<Post> posts = new HashSet<Post>();
		synchronized (bookmarkedPosts) {
			for (String bookmarkedPostId : bookmarkedPosts) {
				Optional<Post> post = getPost(bookmarkedPostId);
				if (post.isPresent()) {
					posts.add(post.get());
				}
			}
		}
		return posts;
	}

	/**
	 * Returns the album with the given ID, creating a new album if no album
	 * with the given ID can be found.
	 *
	 * @param albumId
	 *            The ID of the album
	 * @return The album with the given ID
	 */
	public Album getAlbum(String albumId) {
		return getAlbum(albumId, true);
	}

	/**
	 * Returns the album with the given ID, optionally creating a new album if
	 * an album with the given ID can not be found.
	 *
	 * @param albumId
	 *            The ID of the album
	 * @param create
	 *            {@code true} to create a new album if none exists for the
	 *            given ID
	 * @return The album with the given ID, or {@code null} if no album with the
	 *         given ID exists and {@code create} is {@code false}
	 */
	public Album getAlbum(String albumId, boolean create) {
		Optional<Album> album = database.getAlbum(albumId);
		if (album.isPresent()) {
			return album.get();
		}
		if (!create) {
			return null;
		}
		Album newAlbum = database.newAlbumBuilder().withId(albumId).build();
		database.storeAlbum(newAlbum);
		return newAlbum;
	}

	/**
	 * Returns the image with the given ID, creating it if necessary.
	 *
	 * @param imageId
	 *            The ID of the image
	 * @return The image with the given ID
	 */
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
	public Image getImage(String imageId, boolean create) {
		Optional<Image> image = database.getImage(imageId);
		if (image.isPresent()) {
			return image.get();
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
		synchronized (sones) {
			final Sone sone;
			try {
				sone = getLocalSone(ownIdentity.getId(), true).setIdentity(ownIdentity).setInsertUri(new FreenetURI(ownIdentity.getInsertUri())).setRequestUri(new FreenetURI(ownIdentity.getRequestUri()));
			} catch (MalformedURLException mue1) {
				logger.log(Level.SEVERE, String.format("Could not convert the Identity’s URIs to Freenet URIs: %s, %s", ownIdentity.getInsertUri(), ownIdentity.getRequestUri()), mue1);
				return null;
			}
			sone.setLatestEdition(Numbers.safeParseLong(ownIdentity.getProperty("Sone.LatestEdition"), (long) 0));
			sone.setClient(new Client("Sone", SonePlugin.VERSION.toString()));
			sone.setKnown(true);
			/* TODO - load posts ’n stuff */
			sones.put(ownIdentity.getId(), sone);
			final SoneInserter soneInserter = new SoneInserter(this, eventBus, freenetInterface, sone);
			soneInserters.put(sone, soneInserter);
			sone.setStatus(SoneStatus.idle);
			loadSone(sone);
			soneInserter.start();
			return sone;
		}
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
		sone.getOptions().addBooleanOption("AutoFollow", new DefaultOption<Boolean>(false));
		sone.getOptions().addBooleanOption("EnableSoneInsertNotifications", new DefaultOption<Boolean>(false));
		sone.getOptions().addBooleanOption("ShowNotification/NewSones", new DefaultOption<Boolean>(true));
		sone.getOptions().addBooleanOption("ShowNotification/NewPosts", new DefaultOption<Boolean>(true));
		sone.getOptions().addBooleanOption("ShowNotification/NewReplies", new DefaultOption<Boolean>(true));
		sone.getOptions().addEnumOption("ShowCustomAvatars", new DefaultOption<ShowCustomAvatars>(ShowCustomAvatars.NEVER));

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
		synchronized (sones) {
			final Sone sone = getRemoteSone(identity.getId(), true);
			if (sone.isLocal()) {
				return sone;
			}
			sone.setIdentity(identity);
			boolean newSone = sone.getRequestUri() == null;
			sone.setRequestUri(SoneUri.create(identity.getRequestUri()));
			sone.setLatestEdition(Numbers.safeParseLong(identity.getProperty("Sone.LatestEdition"), (long) 0));
			if (newSone) {
				synchronized (knownSones) {
					newSone = !knownSones.contains(sone.getId());
				}
				sone.setKnown(!newSone);
				if (newSone) {
					eventBus.post(new NewSoneFoundEvent(sone));
					for (Sone localSone : getLocalSones()) {
						if (localSone.getOptions().getBooleanOption("AutoFollow").get()) {
							followSone(localSone, sone.getId());
						}
					}
				}
			}
			soneDownloader.addSone(sone);
			soneDownloaders.execute(new Runnable() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void run() {
					soneDownloader.fetchSone(sone, sone.getRequestUri());
				}

			});
			return sone;
		}
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
		sone.addFriend(soneId);
		synchronized (soneFollowingTimes) {
			if (!soneFollowingTimes.containsKey(soneId)) {
				long now = System.currentTimeMillis();
				soneFollowingTimes.put(soneId, now);
				Optional<Sone> followedSone = getSone(soneId);
				if (!followedSone.isPresent()) {
					return;
				}
				for (Post post : followedSone.get().getPosts()) {
					if (post.getTime() < now) {
						markPostKnown(post);
					}
				}
				for (PostReply reply : followedSone.get().getReplies()) {
					if (reply.getTime() < now) {
						markReplyKnown(reply);
					}
				}
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
		sone.removeFriend(soneId);
		boolean unfollowedSoneStillFollowed = false;
		for (Sone localSone : getLocalSones()) {
			unfollowedSoneStillFollowed |= localSone.hasFriend(soneId);
		}
		if (!unfollowedSoneStillFollowed) {
			synchronized (soneFollowingTimes) {
				soneFollowingTimes.remove(soneId);
			}
		}
		touchConfiguration();
	}

	/**
	 * Sets the trust value of the given origin Sone for the target Sone.
	 *
	 * @param origin
	 *            The origin Sone
	 * @param target
	 *            The target Sone
	 * @param trustValue
	 *            The trust value (from {@code -100} to {@code 100})
	 */
	public void setTrust(Sone origin, Sone target, int trustValue) {
		checkNotNull(origin, "origin must not be null");
		checkArgument(origin.getIdentity() instanceof OwnIdentity, "origin must be a local Sone");
		checkNotNull(target, "target must not be null");
		checkArgument((trustValue >= -100) && (trustValue <= 100), "trustValue must be within [-100, 100]");
		webOfTrustUpdater.setTrust((OwnIdentity) origin.getIdentity(), target.getIdentity(), trustValue, preferences.getTrustComment());
	}

	/**
	 * Removes any trust assignment for the given target Sone.
	 *
	 * @param origin
	 *            The trust origin
	 * @param target
	 *            The trust target
	 */
	public void removeTrust(Sone origin, Sone target) {
		checkNotNull(origin, "origin must not be null");
		checkNotNull(target, "target must not be null");
		checkArgument(origin.getIdentity() instanceof OwnIdentity, "origin must be a local Sone");
		webOfTrustUpdater.setTrust((OwnIdentity) origin.getIdentity(), target.getIdentity(), null, null);
	}

	/**
	 * Assigns the configured positive trust value for the given target.
	 *
	 * @param origin
	 *            The trust origin
	 * @param target
	 *            The trust target
	 */
	public void trustSone(Sone origin, Sone target) {
		setTrust(origin, target, preferences.getPositiveTrust());
	}

	/**
	 * Assigns the configured negative trust value for the given target.
	 *
	 * @param origin
	 *            The trust origin
	 * @param target
	 *            The trust target
	 */
	public void distrustSone(Sone origin, Sone target) {
		setTrust(origin, target, preferences.getNegativeTrust());
	}

	/**
	 * Removes the trust assignment for the given target.
	 *
	 * @param origin
	 *            The trust origin
	 * @param target
	 *            The trust target
	 */
	public void untrustSone(Sone origin, Sone target) {
		removeTrust(origin, target);
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
	public void updateSone(Sone sone, boolean soneRescueMode) {
		Optional<Sone> storedSone = getSone(sone.getId());
		if (storedSone.isPresent()) {
			if (!soneRescueMode && !(sone.getTime() > storedSone.get().getTime())) {
				logger.log(Level.FINE, String.format("Downloaded Sone %s is not newer than stored Sone %s.", sone, storedSone));
				return;
			}
			/* find removed posts. */
			Collection<Post> removedPosts = new ArrayList<Post>();
			Collection<Post> newPosts = new ArrayList<Post>();
			Collection<Post> existingPosts = database.getPosts(sone.getId());
			for (Post oldPost : existingPosts) {
				if (!sone.getPosts().contains(oldPost)) {
					removedPosts.add(oldPost);
				}
			}
			/* find new posts. */
			for (Post newPost : sone.getPosts()) {
				if (existingPosts.contains(newPost)) {
					continue;
				}
				if (newPost.getTime() < getSoneFollowingTime(sone)) {
					newPost.setKnown(true);
				} else if (!newPost.isKnown()) {
					newPosts.add(newPost);
				}
			}
			/* store posts. */
			database.storePosts(sone, sone.getPosts());
			Collection<PostReply> newPostReplies = new ArrayList<PostReply>();
			Collection<PostReply> removedPostReplies = new ArrayList<PostReply>();
			if (!soneRescueMode) {
				for (PostReply reply : storedSone.get().getReplies()) {
					if (!sone.getReplies().contains(reply)) {
						removedPostReplies.add(reply);
					}
				}
			}
			Set<PostReply> storedReplies = storedSone.get().getReplies();
			for (PostReply reply : sone.getReplies()) {
				if (storedReplies.contains(reply)) {
					continue;
				}
				if (reply.getTime() < getSoneFollowingTime(sone)) {
					reply.setKnown(true);
				} else if (!reply.isKnown()) {
					newPostReplies.add(reply);
				}
			}
			database.storePostReplies(sone, sone.getReplies());
			for (Album album : storedSone.get().getRootAlbum().getAlbums()) {
				database.removeAlbum(album);
				for (Image image : album.getImages()) {
					database.removeImage(image);
				}
			}
			for (Post removedPost : removedPosts) {
				eventBus.post(new PostRemovedEvent(removedPost));
			}
			for (Post newPost : newPosts) {
				eventBus.post(new NewPostFoundEvent(newPost));
			}
			for (PostReply removedPostReply : removedPostReplies) {
				eventBus.post(new PostReplyRemovedEvent(removedPostReply));
			}
			for (PostReply newPostReply : newPostReplies) {
				eventBus.post(new NewPostReplyFoundEvent(newPostReply));
			}
			for (Album album : sone.getRootAlbum().getAlbums()) {
				database.storeAlbum(album);
				for (Image image : album.getImages()) {
					database.storeImage(image);
				}
			}
			synchronized (sones) {
				sone.setOptions(storedSone.get().getOptions());
				sone.setKnown(storedSone.get().isKnown());
				sone.setStatus((sone.getTime() == 0) ? SoneStatus.unknown : SoneStatus.idle);
				if (sone.isLocal()) {
					soneInserters.get(storedSone.get()).setSone(sone);
					touchConfiguration();
				}
				sones.put(sone.getId(), sone);
			}
		}
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
		synchronized (sones) {
			if (!getLocalSones().contains(sone)) {
				logger.log(Level.WARNING, String.format("Tried to delete non-local Sone: %s", sone));
				return;
			}
			sones.remove(sone.getId());
			SoneInserter soneInserter = soneInserters.remove(sone);
			soneInserter.stop();
		}
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

		/* initialize options. */
		sone.getOptions().addBooleanOption("AutoFollow", new DefaultOption<Boolean>(false));
		sone.getOptions().addBooleanOption("EnableSoneInsertNotifications", new DefaultOption<Boolean>(false));
		sone.getOptions().addBooleanOption("ShowNotification/NewSones", new DefaultOption<Boolean>(true));
		sone.getOptions().addBooleanOption("ShowNotification/NewPosts", new DefaultOption<Boolean>(true));
		sone.getOptions().addBooleanOption("ShowNotification/NewReplies", new DefaultOption<Boolean>(true));
		sone.getOptions().addEnumOption("ShowCustomAvatars", new DefaultOption<ShowCustomAvatars>(ShowCustomAvatars.NEVER));

		/* load Sone. */
		String sonePrefix = "Sone/" + sone.getId();
		Long soneTime = configuration.getLongValue(sonePrefix + "/Time").getValue(null);
		if (soneTime == null) {
			logger.log(Level.INFO, "Could not load Sone because no Sone has been saved.");
			return;
		}
		String lastInsertFingerprint = configuration.getStringValue(sonePrefix + "/LastInsertFingerprint").getValue("");

		/* load profile. */
		Profile profile = new Profile(sone);
		profile.setFirstName(configuration.getStringValue(sonePrefix + "/Profile/FirstName").getValue(null));
		profile.setMiddleName(configuration.getStringValue(sonePrefix + "/Profile/MiddleName").getValue(null));
		profile.setLastName(configuration.getStringValue(sonePrefix + "/Profile/LastName").getValue(null));
		profile.setBirthDay(configuration.getIntValue(sonePrefix + "/Profile/BirthDay").getValue(null));
		profile.setBirthMonth(configuration.getIntValue(sonePrefix + "/Profile/BirthMonth").getValue(null));
		profile.setBirthYear(configuration.getIntValue(sonePrefix + "/Profile/BirthYear").getValue(null));

		/* load profile fields. */
		while (true) {
			String fieldPrefix = sonePrefix + "/Profile/Fields/" + profile.getFields().size();
			String fieldName = configuration.getStringValue(fieldPrefix + "/Name").getValue(null);
			if (fieldName == null) {
				break;
			}
			String fieldValue = configuration.getStringValue(fieldPrefix + "/Value").getValue("");
			profile.addField(fieldName).setValue(fieldValue);
		}

		/* load posts. */
		Set<Post> posts = new HashSet<Post>();
		while (true) {
			String postPrefix = sonePrefix + "/Posts/" + posts.size();
			String postId = configuration.getStringValue(postPrefix + "/ID").getValue(null);
			if (postId == null) {
				break;
			}
			String postRecipientId = configuration.getStringValue(postPrefix + "/Recipient").getValue(null);
			long postTime = configuration.getLongValue(postPrefix + "/Time").getValue((long) 0);
			String postText = configuration.getStringValue(postPrefix + "/Text").getValue(null);
			if ((postTime == 0) || (postText == null)) {
				logger.log(Level.WARNING, "Invalid post found, aborting load!");
				return;
			}
			PostBuilder postBuilder = postBuilder().withId(postId).from(sone.getId()).withTime(postTime).withText(postText);
			if ((postRecipientId != null) && (postRecipientId.length() == 43)) {
				postBuilder.to(postRecipientId);
			}
			posts.add(postBuilder.build());
		}

		/* load replies. */
		Set<PostReply> replies = new HashSet<PostReply>();
		while (true) {
			String replyPrefix = sonePrefix + "/Replies/" + replies.size();
			String replyId = configuration.getStringValue(replyPrefix + "/ID").getValue(null);
			if (replyId == null) {
				break;
			}
			String postId = configuration.getStringValue(replyPrefix + "/Post/ID").getValue(null);
			long replyTime = configuration.getLongValue(replyPrefix + "/Time").getValue((long) 0);
			String replyText = configuration.getStringValue(replyPrefix + "/Text").getValue(null);
			if ((postId == null) || (replyTime == 0) || (replyText == null)) {
				logger.log(Level.WARNING, "Invalid reply found, aborting load!");
				return;
			}
			PostReplyBuilder postReplyBuilder = postReplyBuilder().withId(replyId).from(sone.getId()).to(postId).withTime(replyTime).withText(replyText);
			replies.add(postReplyBuilder.build());
		}

		/* load post likes. */
		Set<String> likedPostIds = new HashSet<String>();
		while (true) {
			String likedPostId = configuration.getStringValue(sonePrefix + "/Likes/Post/" + likedPostIds.size() + "/ID").getValue(null);
			if (likedPostId == null) {
				break;
			}
			likedPostIds.add(likedPostId);
		}

		/* load reply likes. */
		Set<String> likedReplyIds = new HashSet<String>();
		while (true) {
			String likedReplyId = configuration.getStringValue(sonePrefix + "/Likes/Reply/" + likedReplyIds.size() + "/ID").getValue(null);
			if (likedReplyId == null) {
				break;
			}
			likedReplyIds.add(likedReplyId);
		}

		/* load friends. */
		Set<String> friends = new HashSet<String>();
		while (true) {
			String friendId = configuration.getStringValue(sonePrefix + "/Friends/" + friends.size() + "/ID").getValue(null);
			if (friendId == null) {
				break;
			}
			friends.add(friendId);
		}

		/* load albums. */
		List<Album> topLevelAlbums = new ArrayList<Album>();
		int albumCounter = 0;
		while (true) {
			String albumPrefix = sonePrefix + "/Albums/" + albumCounter++;
			String albumId = configuration.getStringValue(albumPrefix + "/ID").getValue(null);
			if (albumId == null) {
				break;
			}
			String albumTitle = configuration.getStringValue(albumPrefix + "/Title").getValue(null);
			String albumDescription = configuration.getStringValue(albumPrefix + "/Description").getValue(null);
			String albumParentId = configuration.getStringValue(albumPrefix + "/Parent").getValue(null);
			String albumImageId = configuration.getStringValue(albumPrefix + "/AlbumImage").getValue(null);
			if ((albumTitle == null) || (albumDescription == null)) {
				logger.log(Level.WARNING, "Invalid album found, aborting load!");
				return;
			}
			Album album = getAlbum(albumId).setSone(sone).modify().setTitle(albumTitle).setDescription(albumDescription).setAlbumImage(albumImageId).update();
			if (albumParentId != null) {
				Album parentAlbum = getAlbum(albumParentId, false);
				if (parentAlbum == null) {
					logger.log(Level.WARNING, String.format("Invalid parent album ID: %s", albumParentId));
					return;
				}
				parentAlbum.addAlbum(album);
			} else {
				if (!topLevelAlbums.contains(album)) {
					topLevelAlbums.add(album);
				}
			}
		}

		/* load images. */
		int imageCounter = 0;
		while (true) {
			String imagePrefix = sonePrefix + "/Images/" + imageCounter++;
			String imageId = configuration.getStringValue(imagePrefix + "/ID").getValue(null);
			if (imageId == null) {
				break;
			}
			String albumId = configuration.getStringValue(imagePrefix + "/Album").getValue(null);
			String key = configuration.getStringValue(imagePrefix + "/Key").getValue(null);
			String title = configuration.getStringValue(imagePrefix + "/Title").getValue(null);
			String description = configuration.getStringValue(imagePrefix + "/Description").getValue(null);
			Long creationTime = configuration.getLongValue(imagePrefix + "/CreationTime").getValue(null);
			Integer width = configuration.getIntValue(imagePrefix + "/Width").getValue(null);
			Integer height = configuration.getIntValue(imagePrefix + "/Height").getValue(null);
			if ((albumId == null) || (key == null) || (title == null) || (description == null) || (creationTime == null) || (width == null) || (height == null)) {
				logger.log(Level.WARNING, "Invalid image found, aborting load!");
				return;
			}
			Album album = getAlbum(albumId, false);
			if (album == null) {
				logger.log(Level.WARNING, "Invalid album image encountered, aborting load!");
				return;
			}
			Image image = getImage(imageId).modify().setSone(sone).setCreationTime(creationTime).setKey(key).setTitle(title).setDescription(description).setWidth(width).setHeight(height).update();
			album.addImage(image);
		}

		/* load avatar. */
		String avatarId = configuration.getStringValue(sonePrefix + "/Profile/Avatar").getValue(null);
		if (avatarId != null) {
			profile.setAvatar(getImage(avatarId, false));
		}

		/* load options. */
		sone.getOptions().getBooleanOption("AutoFollow").set(configuration.getBooleanValue(sonePrefix + "/Options/AutoFollow").getValue(null));
		sone.getOptions().getBooleanOption("EnableSoneInsertNotifications").set(configuration.getBooleanValue(sonePrefix + "/Options/EnableSoneInsertNotifications").getValue(null));
		sone.getOptions().getBooleanOption("ShowNotification/NewSones").set(configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewSones").getValue(null));
		sone.getOptions().getBooleanOption("ShowNotification/NewPosts").set(configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewPosts").getValue(null));
		sone.getOptions().getBooleanOption("ShowNotification/NewReplies").set(configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewReplies").getValue(null));
		sone.getOptions().<ShowCustomAvatars> getEnumOption("ShowCustomAvatars").set(ShowCustomAvatars.valueOf(configuration.getStringValue(sonePrefix + "/Options/ShowCustomAvatars").getValue(ShowCustomAvatars.NEVER.name())));

		/* if we’re still here, Sone was loaded successfully. */
		synchronized (sone) {
			sone.setTime(soneTime);
			sone.setProfile(profile);
			sone.setPosts(posts);
			sone.setReplies(replies);
			sone.setLikePostIds(likedPostIds);
			sone.setLikeReplyIds(likedReplyIds);
			for (String friendId : friends) {
				followSone(sone, friendId);
			}
			for (Album album : sone.getRootAlbum().getAlbums()) {
				sone.getRootAlbum().removeAlbum(album);
			}
			for (Album album : topLevelAlbums) {
				sone.getRootAlbum().addAlbum(album);
			}
			soneInserters.get(sone).setLastInsertFingerprint(lastInsertFingerprint);
		}
		synchronized (knownSones) {
			for (String friend : friends) {
				knownSones.add(friend);
			}
		}
		database.storePosts(sone, posts);
		for (Post post : posts) {
			post.setKnown(true);
		}
		database.storePostReplies(sone, replies);
		for (PostReply reply : replies) {
			reply.setKnown(true);
		}

		logger.info(String.format("Sone loaded successfully: %s", sone));
	}

	/**
	 * Creates a new post.
	 *
	 * @param sone
	 *            The Sone that creates the post
	 * @param text
	 *            The text of the post
	 * @return The created post
	 */
	public Post createPost(Sone sone, String text) {
		return createPost(sone, System.currentTimeMillis(), text);
	}

	/**
	 * Creates a new post.
	 *
	 * @param sone
	 *            The Sone that creates the post
	 * @param time
	 *            The time of the post
	 * @param text
	 *            The text of the post
	 * @return The created post
	 */
	public Post createPost(Sone sone, long time, String text) {
		return createPost(sone, null, time, text);
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
	public Post createPost(Sone sone, Optional<Sone> recipient, String text) {
		return createPost(sone, recipient, System.currentTimeMillis(), text);
	}

	/**
	 * Creates a new post.
	 *
	 * @param sone
	 *            The Sone that creates the post
	 * @param recipient
	 *            The recipient Sone, or {@code null} if this post does not have
	 *            a recipient
	 * @param time
	 *            The time of the post
	 * @param text
	 *            The text of the post
	 * @return The created post
	 */
	public Post createPost(Sone sone, Optional<Sone> recipient, long time, String text) {
		checkNotNull(text, "text must not be null");
		checkArgument(text.trim().length() > 0, "text must not be empty");
		if (!sone.isLocal()) {
			logger.log(Level.FINE, String.format("Tried to create post for non-local Sone: %s", sone));
			return null;
		}
		PostBuilder postBuilder = database.newPostBuilder();
		postBuilder.from(sone.getId()).randomId().withTime(time).withText(text.trim());
		if (recipient.isPresent()) {
			postBuilder.to(recipient.get().getId());
		}
		final Post post = postBuilder.build();
		database.storePost(post);
		eventBus.post(new NewPostFoundEvent(post));
		sone.addPost(post);
		touchConfiguration();
		localElementTicker.schedule(new Runnable() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				markPostKnown(post);
			}
		}, 10, TimeUnit.SECONDS);
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

	/**
	 * Bookmarks the given post.
	 *
	 * @param post
	 *            The post to bookmark
	 */
	public void bookmark(Post post) {
		bookmarkPost(post.getId());
	}

	/**
	 * Bookmarks the post with the given ID.
	 *
	 * @param id
	 *            The ID of the post to bookmark
	 */
	public void bookmarkPost(String id) {
		synchronized (bookmarkedPosts) {
			bookmarkedPosts.add(id);
		}
	}

	/**
	 * Removes the given post from the bookmarks.
	 *
	 * @param post
	 *            The post to unbookmark
	 */
	public void unbookmark(Post post) {
		unbookmarkPost(post.getId());
	}

	/**
	 * Removes the post with the given ID from the bookmarks.
	 *
	 * @param id
	 *            The ID of the post to unbookmark
	 */
	public void unbookmarkPost(String id) {
		synchronized (bookmarkedPosts) {
			bookmarkedPosts.remove(id);
		}
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
		localElementTicker.schedule(new Runnable() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				markReplyKnown(reply);
			}
		}, 10, TimeUnit.SECONDS);
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
		reply.setKnown(true);
		eventBus.post(new MarkPostReplyKnownEvent(reply));
		if (!previouslyKnown) {
			touchConfiguration();
		}
	}

	/**
	 * Creates a new top-level album for the given Sone.
	 *
	 * @param sone
	 *            The Sone to create the album for
	 * @return The new album
	 */
	public Album createAlbum(Sone sone) {
		return createAlbum(sone, sone.getRootAlbum());
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
		Album album = database.newAlbumBuilder().randomId().build();
		database.storeAlbum(album);
		album.setSone(sone);
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
	 * @see #deleteTemporaryImage(TemporaryImage)
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
	 * Deletes the given temporary image.
	 *
	 * @param temporaryImage
	 *            The temporary image to delete
	 */
	public void deleteTemporaryImage(TemporaryImage temporaryImage) {
		checkNotNull(temporaryImage, "temporaryImage must not be null");
		deleteTemporaryImage(temporaryImage.getId());
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
		database.start();
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
		synchronized (sones) {
			for (Entry<Sone, SoneInserter> soneInserter : soneInserters.entrySet()) {
				soneInserter.getValue().stop();
				saveSone(getLocalSone(soneInserter.getKey().getId(), false));
			}
		}
		saveConfiguration();
		database.stop();
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

			/* save friends. */
			int friendCounter = 0;
			for (String friendId : sone.getFriends()) {
				configuration.getStringValue(sonePrefix + "/Friends/" + friendCounter++ + "/ID").setValue(friendId);
			}
			configuration.getStringValue(sonePrefix + "/Friends/" + friendCounter + "/ID").setValue(null);

			/* save albums. first, collect in a flat structure, top-level first. */
			List<Album> albums = FluentIterable.from(sone.getRootAlbum().getAlbums()).transformAndConcat(Album.FLATTENER).toList();

			int albumCounter = 0;
			for (Album album : albums) {
				String albumPrefix = sonePrefix + "/Albums/" + albumCounter++;
				configuration.getStringValue(albumPrefix + "/ID").setValue(album.getId());
				configuration.getStringValue(albumPrefix + "/Title").setValue(album.getTitle());
				configuration.getStringValue(albumPrefix + "/Description").setValue(album.getDescription());
				configuration.getStringValue(albumPrefix + "/Parent").setValue(album.getParent().equals(sone.getRootAlbum()) ? null : album.getParent().getId());
				configuration.getStringValue(albumPrefix + "/AlbumImage").setValue(album.getAlbumImage() == null ? null : album.getAlbumImage().getId());
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
			configuration.getBooleanValue(sonePrefix + "/Options/AutoFollow").setValue(sone.getOptions().getBooleanOption("AutoFollow").getReal());
			configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewSones").setValue(sone.getOptions().getBooleanOption("ShowNotification/NewSones").getReal());
			configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewPosts").setValue(sone.getOptions().getBooleanOption("ShowNotification/NewPosts").getReal());
			configuration.getBooleanValue(sonePrefix + "/Options/ShowNotification/NewReplies").setValue(sone.getOptions().getBooleanOption("ShowNotification/NewReplies").getReal());
			configuration.getBooleanValue(sonePrefix + "/Options/EnableSoneInsertNotifications").setValue(sone.getOptions().getBooleanOption("EnableSoneInsertNotifications").getReal());
			configuration.getStringValue(sonePrefix + "/Options/ShowCustomAvatars").setValue(sone.getOptions().<ShowCustomAvatars> getEnumOption("ShowCustomAvatars").get().name());

			configuration.save();

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
			configuration.getIntValue("Option/ConfigurationVersion").setValue(0);
			configuration.getIntValue("Option/InsertionDelay").setValue(options.getIntegerOption("InsertionDelay").getReal());
			configuration.getIntValue("Option/PostsPerPage").setValue(options.getIntegerOption("PostsPerPage").getReal());
			configuration.getIntValue("Option/ImagesPerPage").setValue(options.getIntegerOption("ImagesPerPage").getReal());
			configuration.getIntValue("Option/CharactersPerPost").setValue(options.getIntegerOption("CharactersPerPost").getReal());
			configuration.getIntValue("Option/PostCutOffLength").setValue(options.getIntegerOption("PostCutOffLength").getReal());
			configuration.getBooleanValue("Option/RequireFullAccess").setValue(options.getBooleanOption("RequireFullAccess").getReal());
			configuration.getIntValue("Option/PositiveTrust").setValue(options.getIntegerOption("PositiveTrust").getReal());
			configuration.getIntValue("Option/NegativeTrust").setValue(options.getIntegerOption("NegativeTrust").getReal());
			configuration.getStringValue("Option/TrustComment").setValue(options.getStringOption("TrustComment").getReal());
			configuration.getBooleanValue("Option/ActivateFcpInterface").setValue(options.getBooleanOption("ActivateFcpInterface").getReal());
			configuration.getIntValue("Option/FcpFullAccessRequired").setValue(options.getIntegerOption("FcpFullAccessRequired").getReal());

			/* save known Sones. */
			int soneCounter = 0;
			synchronized (knownSones) {
				for (String knownSoneId : knownSones) {
					configuration.getStringValue("KnownSone/" + soneCounter++ + "/ID").setValue(knownSoneId);
				}
				configuration.getStringValue("KnownSone/" + soneCounter + "/ID").setValue(null);
			}

			/* save Sone following times. */
			soneCounter = 0;
			synchronized (soneFollowingTimes) {
				for (Entry<String, Long> soneFollowingTime : soneFollowingTimes.entrySet()) {
					configuration.getStringValue("SoneFollowingTimes/" + soneCounter + "/Sone").setValue(soneFollowingTime.getKey());
					configuration.getLongValue("SoneFollowingTimes/" + soneCounter + "/Time").setValue(soneFollowingTime.getValue());
					++soneCounter;
				}
				configuration.getStringValue("SoneFollowingTimes/" + soneCounter + "/Sone").setValue(null);
			}

			/* save known posts. */
			database.save();

			/* save bookmarked posts. */
			int bookmarkedPostCounter = 0;
			synchronized (bookmarkedPosts) {
				for (String bookmarkedPostId : bookmarkedPosts) {
					configuration.getStringValue("Bookmarks/Post/" + bookmarkedPostCounter++ + "/ID").setValue(bookmarkedPostId);
				}
			}
			configuration.getStringValue("Bookmarks/Post/" + bookmarkedPostCounter++ + "/ID").setValue(null);

			/* now save it. */
			configuration.save();

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
		/* create options. */
		options.addIntegerOption("InsertionDelay", new DefaultOption<Integer>(60, new IntegerRangePredicate(0, Integer.MAX_VALUE), new SetInsertionDelay()));
		options.addIntegerOption("PostsPerPage", new DefaultOption<Integer>(10, new IntegerRangePredicate(1, Integer.MAX_VALUE)));
		options.addIntegerOption("ImagesPerPage", new DefaultOption<Integer>(9, new IntegerRangePredicate(1, Integer.MAX_VALUE)));
		options.addIntegerOption("CharactersPerPost", new DefaultOption<Integer>(400, Predicates.<Integer> or(new IntegerRangePredicate(50, Integer.MAX_VALUE), Predicates.equalTo(-1))));
		options.addIntegerOption("PostCutOffLength", new DefaultOption<Integer>(200, Predicates.<Integer> or(new IntegerRangePredicate(50, Integer.MAX_VALUE), Predicates.equalTo(-1))));
		options.addBooleanOption("RequireFullAccess", new DefaultOption<Boolean>(false));
		options.addIntegerOption("PositiveTrust", new DefaultOption<Integer>(75, new IntegerRangePredicate(0, 100)));
		options.addIntegerOption("NegativeTrust", new DefaultOption<Integer>(-25, new IntegerRangePredicate(-100, 100)));
		options.addStringOption("TrustComment", new DefaultOption<String>("Set from Sone Web Interface"));
		options.addBooleanOption("ActivateFcpInterface", new DefaultOption<Boolean>(false, fcpInterface.new SetActive()));
		options.addIntegerOption("FcpFullAccessRequired", new DefaultOption<Integer>(2, new OptionWatcher<Integer>() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void optionChanged(Option<Integer> option, Integer oldValue, Integer newValue) {
				fcpInterface.setFullAccessRequired(FullAccessRequired.values()[newValue]);
			}

		}));

		loadConfigurationValue("InsertionDelay");
		loadConfigurationValue("PostsPerPage");
		loadConfigurationValue("ImagesPerPage");
		loadConfigurationValue("CharactersPerPost");
		loadConfigurationValue("PostCutOffLength");
		options.getBooleanOption("RequireFullAccess").set(configuration.getBooleanValue("Option/RequireFullAccess").getValue(null));
		loadConfigurationValue("PositiveTrust");
		loadConfigurationValue("NegativeTrust");
		options.getStringOption("TrustComment").set(configuration.getStringValue("Option/TrustComment").getValue(null));
		options.getBooleanOption("ActivateFcpInterface").set(configuration.getBooleanValue("Option/ActivateFcpInterface").getValue(null));
		options.getIntegerOption("FcpFullAccessRequired").set(configuration.getIntValue("Option/FcpFullAccessRequired").getValue(null));

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

		/* load Sone following times. */
		soneCounter = 0;
		while (true) {
			String soneId = configuration.getStringValue("SoneFollowingTimes/" + soneCounter + "/Sone").getValue(null);
			if (soneId == null) {
				break;
			}
			long time = configuration.getLongValue("SoneFollowingTimes/" + soneCounter + "/Time").getValue(Long.MAX_VALUE);
			synchronized (soneFollowingTimes) {
				soneFollowingTimes.put(soneId, time);
			}
			++soneCounter;
		}

		/* load bookmarked posts. */
		int bookmarkedPostCounter = 0;
		while (true) {
			String bookmarkedPostId = configuration.getStringValue("Bookmarks/Post/" + bookmarkedPostCounter++ + "/ID").getValue(null);
			if (bookmarkedPostId == null) {
				break;
			}
			synchronized (bookmarkedPosts) {
				bookmarkedPosts.add(bookmarkedPostId);
			}
		}

	}

	/**
	 * Loads an {@link Integer} configuration value for the option with the
	 * given name, logging validation failures.
	 *
	 * @param optionName
	 *            The name of the option to load
	 */
	private void loadConfigurationValue(String optionName) {
		try {
			options.getIntegerOption(optionName).set(configuration.getIntValue("Option/" + optionName).getValue(null));
		} catch (IllegalArgumentException iae1) {
			logger.log(Level.WARNING, String.format("Invalid value for %s in configuration, using default.", optionName));
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
		OwnIdentity ownIdentity = ownIdentityAddedEvent.ownIdentity();
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
		OwnIdentity ownIdentity = ownIdentityRemovedEvent.ownIdentity();
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
		Identity identity = identityAddedEvent.identity();
		logger.log(Level.FINEST, String.format("Adding Identity: %s", identity));
		trustedIdentities.put(identityAddedEvent.ownIdentity(), identity);
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
		final Identity identity = identityUpdatedEvent.identity();
		soneDownloaders.execute(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				Sone sone = getRemoteSone(identity.getId(), false);
				if (sone.isLocal()) {
					return;
				}
				sone.setIdentity(identity);
				sone.setLatestEdition(Numbers.safeParseLong(identity.getProperty("Sone.LatestEdition"), sone.getLatestEdition()));
				soneDownloader.addSone(sone);
				soneDownloader.fetchSone(sone);
			}
		});
	}

	/**
	 * Notifies the core that an {@link Identity} was removed.
	 *
	 * @param identityRemovedEvent
	 *            The event
	 */
	@Subscribe
	public void identityRemoved(IdentityRemovedEvent identityRemovedEvent) {
		OwnIdentity ownIdentity = identityRemovedEvent.ownIdentity();
		Identity identity = identityRemovedEvent.identity();
		trustedIdentities.remove(ownIdentity, identity);
		boolean foundIdentity = false;
		for (Entry<OwnIdentity, Collection<Identity>> trustedIdentity : trustedIdentities.asMap().entrySet()) {
			if (trustedIdentity.getKey().equals(ownIdentity)) {
				continue;
			}
			if (trustedIdentity.getValue().contains(identity)) {
				foundIdentity = true;
			}
		}
		if (foundIdentity) {
			/* some local identity still trusts this identity, don’t remove. */
			return;
		}
		Optional<Sone> sone = getSone(identity.getId());
		if (!sone.isPresent()) {
			/* TODO - we don’t have the Sone anymore. should this happen? */
			return;
		}
		database.removePosts(sone.get());
		for (Post post : sone.get().getPosts()) {
			eventBus.post(new PostRemovedEvent(post));
		}
		database.removePostReplies(sone.get());
		for (PostReply reply : sone.get().getReplies()) {
			eventBus.post(new PostReplyRemovedEvent(reply));
		}
		synchronized (sones) {
			sones.remove(identity.getId());
		}
		eventBus.post(new SoneRemovedEvent(sone.get()));
	}

	/**
	 * Deletes the temporary image.
	 *
	 * @param imageInsertFinishedEvent
	 *            The event
	 */
	@Subscribe
	public void imageInsertFinished(ImageInsertFinishedEvent imageInsertFinishedEvent) {
		logger.log(Level.WARNING, String.format("Image insert finished for %s: %s", imageInsertFinishedEvent.image(), imageInsertFinishedEvent.resultingUri()));
		imageInsertFinishedEvent.image().modify().setKey(imageInsertFinishedEvent.resultingUri().toString()).update();
		deleteTemporaryImage(imageInsertFinishedEvent.image().getId());
		touchConfiguration();
	}

}
