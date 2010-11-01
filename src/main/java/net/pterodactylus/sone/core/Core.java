/*
 * Sone - Core.java - Copyright © 2010 David Roden
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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Options.DefaultOption;
import net.pterodactylus.sone.core.Options.Option;
import net.pterodactylus.sone.core.Options.OptionWatcher;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.IdentityListener;
import net.pterodactylus.sone.freenet.wot.IdentityManager;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.number.Numbers;
import freenet.keys.FreenetURI;

/**
 * The Sone core.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Core implements IdentityListener {

	/**
	 * Enumeration for the possible states of a {@link Sone}.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public enum SoneStatus {

		/** The Sone is unknown, i.e. not yet downloaded. */
		unknown,

		/** The Sone is idle, i.e. not being downloaded or inserted. */
		idle,

		/** The Sone is currently being inserted. */
		inserting,

		/** The Sone is currently being downloaded. */
		downloading,
	}

	/** The logger. */
	private static final Logger logger = Logging.getLogger(Core.class);

	/** The options. */
	private final Options options = new Options();

	/** The configuration. */
	private final Configuration configuration;

	/** The identity manager. */
	private final IdentityManager identityManager;

	/** Interface to freenet. */
	private final FreenetInterface freenetInterface;

	/** The Sone downloader. */
	private final SoneDownloader soneDownloader;

	/** The Sones’ statuses. */
	/* synchronize access on itself. */
	private final Map<Sone, SoneStatus> soneStatuses = new HashMap<Sone, SoneStatus>();

	/** Sone inserters. */
	/* synchronize access on this on localSones. */
	private final Map<Sone, SoneInserter> soneInserters = new HashMap<Sone, SoneInserter>();

	/** All local Sones. */
	/* synchronize access on this on itself. */
	private Map<String, Sone> localSones = new HashMap<String, Sone>();

	/** All remote Sones. */
	/* synchronize access on this on itself. */
	private Map<String, Sone> remoteSones = new HashMap<String, Sone>();

	/** All posts. */
	private Map<String, Post> posts = new HashMap<String, Post>();

	/** All replies. */
	private Map<String, Reply> replies = new HashMap<String, Reply>();

	/**
	 * Creates a new core.
	 *
	 * @param configuration
	 *            The configuration of the core
	 * @param freenetInterface
	 *            The freenet interface
	 * @param identityManager
	 *            The identity manager
	 */
	public Core(Configuration configuration, FreenetInterface freenetInterface, IdentityManager identityManager) {
		this.configuration = configuration;
		this.freenetInterface = freenetInterface;
		this.identityManager = identityManager;
		this.soneDownloader = new SoneDownloader(this, freenetInterface);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the options used by the core.
	 *
	 * @return The options of the core
	 */
	public Options getOptions() {
		return options;
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
	 * Returns the status of the given Sone.
	 *
	 * @param sone
	 *            The Sone to get the status for
	 * @return The status of the Sone
	 */
	public SoneStatus getSoneStatus(Sone sone) {
		synchronized (soneStatuses) {
			return soneStatuses.get(sone);
		}
	}

	/**
	 * Sets the status of the given Sone.
	 *
	 * @param sone
	 *            The Sone to set the status of
	 * @param soneStatus
	 *            The status to set
	 */
	public void setSoneStatus(Sone sone, SoneStatus soneStatus) {
		synchronized (soneStatuses) {
			soneStatuses.put(sone, soneStatus);
		}
	}

	/**
	 * Returns all Sones, remote and local.
	 *
	 * @return All Sones
	 */
	public Set<Sone> getSones() {
		Set<Sone> allSones = new HashSet<Sone>();
		allSones.addAll(getLocalSones());
		allSones.addAll(getRemoteSones());
		return allSones;
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
	public Sone getSone(String id) {
		Sone sone = getRemoteSone(id);
		if (sone != null) {
			return sone;
		}
		sone = getLocalSone(id);
		return sone;
	}

	/**
	 * Returns whether the given Sone is a local Sone.
	 *
	 * @param sone
	 *            The Sone to check for its locality
	 * @return {@code true} if the given Sone is local, {@code false} otherwise
	 */
	public boolean isLocalSone(Sone sone) {
		synchronized (localSones) {
			return localSones.containsKey(sone.getId());
		}
	}

	/**
	 * Returns all local Sones.
	 *
	 * @return All local Sones
	 */
	public Set<Sone> getLocalSones() {
		synchronized (localSones) {
			return new HashSet<Sone>(localSones.values());
		}
	}

	/**
	 * Returns the local Sone with the given ID.
	 *
	 * @param id
	 *            The ID of the Sone to get
	 * @return The Sone, or {@code null} if there is no Sone with the given ID
	 */
	public Sone getLocalSone(String id) {
		synchronized (localSones) {
			return localSones.get(id);
		}
	}

	/**
	 * Returns all remote Sones.
	 *
	 * @return All remote Sones
	 */
	public Set<Sone> getRemoteSones() {
		synchronized (remoteSones) {
			return new HashSet<Sone>(remoteSones.values());
		}
	}

	/**
	 * Returns the remote Sone with the given ID.
	 *
	 * @param id
	 *            The ID of the remote Sone to get
	 * @return The Sone, or {@code null} if there is no such Sone
	 */
	public Sone getRemoteSone(String id) {
		synchronized (remoteSones) {
			return remoteSones.get(id);
		}
	}

	/**
	 * Returns whether the given Sone is a remote Sone.
	 *
	 * @param sone
	 *            The Sone to check
	 * @return {@code true} if the given Sone is a remote Sone, {@code false}
	 *         otherwise
	 */
	public boolean isRemoteSone(Sone sone) {
		synchronized (remoteSones) {
			return remoteSones.containsKey(sone.getId());
		}
	}

	/**
	 * Returns the post with the given ID.
	 *
	 * @param postId
	 *            The ID of the post to get
	 * @return The post, or {@code null} if there is no such post
	 */
	public Post getPost(String postId) {
		synchronized (posts) {
			return posts.get(postId);
		}
	}

	/**
	 * Returns the reply with the given ID.
	 *
	 * @param replyId
	 *            The ID of the reply to get
	 * @return The reply, or {@code null} if there is no such reply
	 */
	public Reply getReply(String replyId) {
		synchronized (replies) {
			return replies.get(replyId);
		}
	}

	/**
	 * Returns all replies for the given post, order ascending by time.
	 *
	 * @param post
	 *            The post to get all replies for
	 * @return All replies for the given post
	 */
	public List<Reply> getReplies(Post post) {
		Set<Sone> sones = getSones();
		List<Reply> replies = new ArrayList<Reply>();
		for (Sone sone : sones) {
			for (Reply reply : sone.getReplies()) {
				if (reply.getPost().equals(post)) {
					replies.add(reply);
				}
			}
		}
		Collections.sort(replies, Reply.TIME_COMPARATOR);
		return replies;
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
	public Set<Sone> getLikes(Reply reply) {
		Set<Sone> sones = new HashSet<Sone>();
		for (Sone sone : getSones()) {
			if (sone.getLikedPostIds().contains(reply.getId())) {
				sones.add(sone);
			}
		}
		return sones;
	}

	//
	// ACTIONS
	//

	/**
	 * Adds a local Sone from the given ID which has to be the ID of an own
	 * identity.
	 *
	 * @param id
	 *            The ID of an own identity to add a Sone for
	 * @return The added (or already existing) Sone
	 */
	public Sone addLocalSone(String id) {
		synchronized (localSones) {
			if (localSones.containsKey(id)) {
				logger.log(Level.FINE, "Tried to add known local Sone: %s", id);
				return localSones.get(id);
			}
			OwnIdentity ownIdentity = identityManager.getOwnIdentity(id);
			if (ownIdentity == null) {
				logger.log(Level.INFO, "Invalid Sone ID: %s", id);
				return null;
			}
			return addLocalSone(ownIdentity);
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
		synchronized (localSones) {
			if (localSones.containsKey(ownIdentity.getId())) {
				logger.log(Level.FINE, "Tried to add known local Sone: %s", ownIdentity);
				return localSones.get(ownIdentity.getId());
			}
			String latestEdition = ownIdentity.getProperty("Sone.LatestEdition");
			Sone sone = new Sone(ownIdentity).setInsertUri(getSoneUri(ownIdentity.getInsertUri(), latestEdition)).setRequestUri(getSoneUri(ownIdentity.getRequestUri(), latestEdition));
			/* TODO - load posts ’n stuff */
			localSones.put(ownIdentity.getId(), sone);
			SoneInserter soneInserter = new SoneInserter(this, freenetInterface, sone);
			soneInserters.put(sone, soneInserter);
			soneInserter.start();
			setSoneStatus(sone, SoneStatus.idle);
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
		identityManager.addContext(ownIdentity, "Sone");
		Sone sone = addLocalSone(ownIdentity);
		synchronized (sone) {
			/* mark as modified so that it gets inserted immediately. */
			sone.setModificationCounter(sone.getModificationCounter() + 1);
		}
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
		synchronized (remoteSones) {
			if (remoteSones.containsKey(identity.getId())) {
				logger.log(Level.FINE, "Identity already exists: %s", identity);
				return remoteSones.get(identity.getId());
			}
			Sone sone = new Sone(identity);
			sone.setRequestUri(getSoneUri(identity.getRequestUri(), identity.getProperty("Sone.LatestEdition")));
			remoteSones.put(identity.getId(), sone);
			soneDownloader.addSone(sone);
			setSoneStatus(sone, SoneStatus.idle);
			return sone;
		}
	}

	/**
	 * Updates the stores Sone with the given Sone.
	 *
	 * @param sone
	 *            The updated Sone
	 */
	public void updateSone(Sone sone) {
		if (isRemoteSone(sone)) {
			Sone storedSone = getRemoteSone(sone.getId());
			if (!(sone.getTime() > storedSone.getTime())) {
				logger.log(Level.FINE, "Downloaded Sone %s is not newer than stored Sone %s.", new Object[] { sone, storedSone });
				return;
			}
			synchronized (posts) {
				for (Post post : storedSone.getPosts()) {
					posts.remove(post.getId());
				}
				for (Post post : sone.getPosts()) {
					posts.put(post.getId(), post);
				}
			}
			synchronized (replies) {
				for (Reply reply : storedSone.getReplies()) {
					replies.remove(reply.getId());
				}
				for (Reply reply : sone.getReplies()) {
					replies.put(reply.getId(), reply);
				}
			}
			synchronized (storedSone) {
				storedSone.setTime(sone.getTime());
				storedSone.setProfile(sone.getProfile());
				storedSone.setPosts(sone.getPosts());
				storedSone.setReplies(sone.getReplies());
				storedSone.setLikePostIds(sone.getLikedPostIds());
				storedSone.setLikeReplyIds(sone.getLikedReplyIds());
				storedSone.updateUris(sone.getRequestUri().getEdition());
			}
			saveSone(storedSone);
		}
	}

	/**
	 * Deletes the given Sone. This will remove the Sone from the
	 * {@link #getLocalSone(String) local Sones}, stops its {@link SoneInserter}
	 * and remove the context from its identity.
	 *
	 * @param sone
	 *            The Sone to delete
	 */
	public void deleteSone(Sone sone) {
		if (!(sone.getIdentity() instanceof OwnIdentity)) {
			logger.log(Level.WARNING, "Tried to delete Sone of non-own identity: %s", sone);
			return;
		}
		synchronized (localSones) {
			if (!localSones.containsKey(sone.getId())) {
				logger.log(Level.WARNING, "Tried to delete non-local Sone: %s", sone);
				return;
			}
			localSones.remove(sone.getId());
			soneInserters.remove(sone.getId()).stop();
		}
		identityManager.removeContext((OwnIdentity) sone.getIdentity(), "Sone");
	}

	/**
	 * Saves the given Sone. This will persist all local settings for the given
	 * Sone, such as the friends list and similar, private options.
	 *
	 * @param sone
	 *            The Sone to save
	 */
	public void saveSone(Sone sone) {
		if (!isLocalSone(sone)) {
			logger.log(Level.FINE, "Tried to save non-local Sone: %s", sone);
		}
		/* TODO - implement saving. */
	}

	/**
	 * Creates a new post.
	 *
	 * @param sone
	 *            The Sone that creates the post
	 * @param text
	 *            The text of the post
	 */
	public void createPost(Sone sone, String text) {
		createPost(sone, System.currentTimeMillis(), text);
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
	 */
	public void createPost(Sone sone, long time, String text) {
		if (!isLocalSone(sone)) {
			logger.log(Level.FINE, "Tried to create post for non-local Sone: %s", sone);
			return;
		}
		Post post = new Post(sone, time, text);
		synchronized (posts) {
			posts.put(post.getId(), post);
		}
		sone.addPost(post);
		saveSone(sone);
	}

	/**
	 * Deletes the given post.
	 *
	 * @param post
	 *            The post to delete
	 */
	public void deletePost(Post post) {
		if (!isLocalSone(post.getSone())) {
			logger.log(Level.WARNING, "Tried to delete post of non-local Sone: %s", post.getSone());
			return;
		}
		post.getSone().removePost(post);
		synchronized (posts) {
			posts.remove(post.getId());
		}
		saveSone(post.getSone());
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
	 */
	public void createReply(Sone sone, Post post, String text) {
		createReply(sone, post, System.currentTimeMillis(), text);
	}

	/**
	 * Creates a new reply.
	 *
	 * @param sone
	 *            The Sone that creates the reply
	 * @param post
	 *            The post that this reply refers to
	 * @param time
	 *            The time of the reply
	 * @param text
	 *            The text of the reply
	 */
	public void createReply(Sone sone, Post post, long time, String text) {
		if (!isLocalSone(sone)) {
			logger.log(Level.FINE, "Tried to create reply for non-local Sone: %s", sone);
			return;
		}
		Reply reply = new Reply(sone, post, System.currentTimeMillis(), text);
		synchronized (replies) {
			replies.put(reply.getId(), reply);
		}
		sone.addReply(reply);
		saveSone(sone);
	}

	/**
	 * Deletes the given reply.
	 *
	 * @param reply
	 *            The reply to delete
	 */
	public void deleteReply(Reply reply) {
		Sone sone = reply.getSone();
		if (!isLocalSone(sone)) {
			logger.log(Level.FINE, "Tried to delete non-local reply: %s", reply);
			return;
		}
		synchronized (replies) {
			replies.remove(reply.getId());
		}
		sone.removeReply(reply);
		saveSone(sone);
	}

	/**
	 * Starts the core.
	 */
	public void start() {
		loadConfiguration();
	}

	/**
	 * Stops the core.
	 */
	public void stop() {
		synchronized (localSones) {
			for (SoneInserter soneInserter : soneInserters.values()) {
				soneInserter.stop();
			}
		}
		saveConfiguration();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Loads the configuration.
	 */
	@SuppressWarnings("unchecked")
	private void loadConfiguration() {
		/* create options. */
		options.addIntegerOption("InsertionDelay", new DefaultOption<Integer>(60, new OptionWatcher<Integer>() {

			@Override
			public void optionChanged(Option<Integer> option, Integer oldValue, Integer newValue) {
				SoneInserter.setInsertionDelay(newValue);
			}

		}));
		options.addBooleanOption("ClearOnNextRestart", new DefaultOption<Boolean>(false));
		options.addBooleanOption("ReallyClearOnNextRestart", new DefaultOption<Boolean>(false));

		/* read options from configuration. */
		options.getBooleanOption("ClearOnNextRestart").set(configuration.getBooleanValue("Option/ClearOnNextRestart").getValue(null));
		options.getBooleanOption("ReallyClearOnNextRestart").set(configuration.getBooleanValue("Option/ReallyClearOnNextRestart").getValue(null));
		boolean clearConfiguration = options.getBooleanOption("ClearOnNextRestart").get() && options.getBooleanOption("ReallyClearOnNextRestart").get();
		options.getBooleanOption("ClearOnNextRestart").set(null);
		options.getBooleanOption("ReallyClearOnNextRestart").set(null);
		if (clearConfiguration) {
			/* stop loading the configuration. */
			return;
		}

		options.getIntegerOption("InsertionDelay").set(configuration.getIntValue("Option/InsertionDelay").getValue(null));

	}

	/**
	 * Saves the current options.
	 */
	private void saveConfiguration() {
		/* store the options first. */
		try {
			configuration.getIntValue("Option/InsertionDelay").setValue(options.getIntegerOption("InsertionDelay").getReal());
			configuration.getBooleanValue("Option/ClearOnNextRestart").setValue(options.getBooleanOption("ClearOnNextRestart").getReal());
			configuration.getBooleanValue("Option/ReallyClearOnNextRestart").setValue(options.getBooleanOption("ReallyClearOnNextRestart").getReal());
		} catch (ConfigurationException ce1) {
			logger.log(Level.SEVERE, "Could not store configuration!", ce1);
		}
	}

	/**
	 * Generate a Sone URI from the given URI and latest edition.
	 *
	 * @param uriString
	 *            The URI to derive the Sone URI from
	 * @param latestEditionString
	 *            The latest edition as a {@link String}, or {@code null}
	 * @return The derived URI
	 */
	private FreenetURI getSoneUri(String uriString, String latestEditionString) {
		try {
			FreenetURI uri = new FreenetURI(uriString).setDocName("Sone").setMetaString(new String[0]).setSuggestedEdition(Numbers.safeParseLong(latestEditionString, (long) 0));
			return uri;
		} catch (MalformedURLException mue1) {
			logger.log(Level.WARNING, "Could not create Sone URI from URI: " + uriString, mue1);
			return null;
		}
	}

	//
	// INTERFACE IdentityListener
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ownIdentityAdded(OwnIdentity ownIdentity) {
		logger.log(Level.FINEST, "Adding OwnIdentity: " + ownIdentity);
		if (ownIdentity.hasContext("Sone")) {
			addLocalSone(ownIdentity);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ownIdentityRemoved(OwnIdentity ownIdentity) {
		/* TODO */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void identityAdded(Identity identity) {
		logger.log(Level.FINEST, "Adding Identity: " + identity);
		addRemoteSone(identity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void identityUpdated(Identity identity) {
		/* TODO */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void identityRemoved(Identity identity) {
		/* TODO */
	}

}
