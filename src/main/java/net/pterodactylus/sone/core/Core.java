/*
 * FreenetSone - Core.java - Copyright © 2010 David Roden
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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Options.DefaultOption;
import net.pterodactylus.sone.core.Options.Option;
import net.pterodactylus.sone.core.Options.OptionWatcher;
import net.pterodactylus.sone.core.SoneException.Type;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.WebOfTrustConnector;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.filter.Filter;
import net.pterodactylus.util.filter.Filters;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.service.AbstractService;
import freenet.client.FetchResult;
import freenet.keys.FreenetURI;

/**
 * The Sone core.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Core extends AbstractService {

	/** The default Sones. */
	private static final Set<String> defaultSones = new HashSet<String>();

	static {
		/* Sone of Sone. */
		defaultSones.add("USK@eRHt0ceFsHjRZ11j6dd68RSdIvfd8f9YjJLZ9lnhEyo,iJWjIWh6TkMZm1NY8qBranKTIuwsCPkVPG6T6c6ft-I,AQACAAE/Sone/4");
		/* Sone of Bombe. */
		defaultSones.add("USK@RuW~uAO35Ipne896-1OmaVJNPuYE4ZIB5oZ5ziaU57A,7rV3uiyztXBDt03DCoRiNwiGjgFCJuznM9Okc1opURU,AQACAAE/Sone/29");
	}

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
	private Configuration configuration;

	/** Interface to freenet. */
	private FreenetInterface freenetInterface;

	/** The WoT connector. */
	private WebOfTrustConnector webOfTrustConnector;

	/** The Sone downloader. */
	private SoneDownloader soneDownloader;

	/** The Sone blacklist. */
	private final Set<Sone> blacklistedSones = Collections.synchronizedSet(new HashSet<Sone>());

	/** The local Sones. */
	private final Set<Sone> localSones = Collections.synchronizedSet(new HashSet<Sone>());

	/** Sone inserters. */
	private final Map<Sone, SoneInserter> soneInserters = Collections.synchronizedMap(new HashMap<Sone, SoneInserter>());

	/** The Sones’ statuses. */
	private final Map<Sone, SoneStatus> soneStatuses = Collections.synchronizedMap(new HashMap<Sone, SoneStatus>());

	/* various caches follow here. */

	/** Cache for all known Sones. */
	private final Map<String, Sone> soneCache = Collections.synchronizedMap(new HashMap<String, Sone>());

	/** Cache for all known posts. */
	private final Map<String, Post> postCache = Collections.synchronizedMap(new HashMap<String, Post>());

	/** Cache for all known replies. */
	private final Map<String, Reply> replyCache = Collections.synchronizedMap(new HashMap<String, Reply>());

	/**
	 * Creates a new core.
	 */
	public Core() {
		super("Sone Core", false);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the options of the Sone plugin.
	 *
	 * @return The options of the Sone plugin
	 */
	public Options getOptions() {
		return options;
	}

	/**
	 * Sets the configuration of the core.
	 *
	 * @param configuration
	 *            The configuration of the core
	 * @return This core (for method chaining)
	 */
	public Core configuration(Configuration configuration) {
		this.configuration = configuration;
		return this;
	}

	/**
	 * Sets the Freenet interface to use.
	 *
	 * @param freenetInterface
	 *            The Freenet interface to use
	 * @return This core (for method chaining)
	 */
	public Core freenetInterface(FreenetInterface freenetInterface) {
		this.freenetInterface = freenetInterface;
		soneDownloader = new SoneDownloader(this, freenetInterface);
		soneDownloader.start();
		return this;
	}

	/**
	 * Returns the Web of Trust connector.
	 *
	 * @return The Web of Trust connector
	 */
	public WebOfTrustConnector getWebOfTrustConnector() {
		return webOfTrustConnector;
	}

	/**
	 * Sets the Web of Trust connector.
	 *
	 * @param webOfTrustConnector
	 *            The Web of Trust connector
	 * @return This core (for method chaining)
	 */
	public Core setWebOfTrustConnector(WebOfTrustConnector webOfTrustConnector) {
		this.webOfTrustConnector = webOfTrustConnector;
		return this;
	}

	/**
	 * Returns the local Sones.
	 *
	 * @return The local Sones
	 */
	public Set<Sone> getSones() {
		return Filters.filteredSet(localSones, new Filter<Sone>() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public boolean filterObject(Sone sone) {
				return !blacklistedSones.contains(sone);
			}
		});
	}

	/**
	 * Returns the Sone with the given ID, or an empty Sone that has been
	 * initialized with the given ID.
	 *
	 * @param soneId
	 *            The ID of the Sone
	 * @return The Sone
	 */
	public Sone getSone(String soneId) {
		if (!soneCache.containsKey(soneId)) {
			Sone sone = new Sone(soneId);
			soneCache.put(soneId, sone);
			setSoneStatus(sone, SoneStatus.unknown);
		}
		return soneCache.get(soneId);
	}

	/**
	 * Returns all known sones.
	 *
	 * @return All known sones
	 */
	public Collection<Sone> getKnownSones() {
		return Filters.filteredCollection(soneCache.values(), new Filter<Sone>() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public boolean filterObject(Sone sone) {
				return !blacklistedSones.contains(sone);
			}
		});
	}

	/**
	 * Gets all known Sones that are not local Sones.
	 *
	 * @return All remote Sones
	 */
	public Collection<Sone> getRemoteSones() {
		return Filters.filteredCollection(getKnownSones(), new Filter<Sone>() {

			@Override
			@SuppressWarnings("synthetic-access")
			public boolean filterObject(Sone sone) {
				return !blacklistedSones.contains(sone) && !localSones.contains(sone);
			}
		});
	}

	/**
	 * Returns all blacklisted Sones.
	 *
	 * @return All blacklisted Sones
	 */
	public Collection<Sone> getBlacklistedSones() {
		return Collections.unmodifiableCollection(blacklistedSones);
	}

	/**
	 * Checks whether the given Sone is blacklisted.
	 *
	 * @param sone
	 *            The Sone to check
	 * @return {@code true} if this Sone is blacklisted, {@code false} otherwise
	 */
	public boolean isBlacklistedSone(Sone sone) {
		return blacklistedSones.contains(sone);
	}

	/**
	 * Returns the status of the given Sone.
	 *
	 * @param sone
	 *            The Sone to get the status for
	 * @return The status of the Sone
	 */
	public SoneStatus getSoneStatus(Sone sone) {
		return soneStatuses.get(sone);
	}

	/**
	 * Sets the status of the Sone.
	 *
	 * @param sone
	 *            The Sone to set the status for
	 * @param soneStatus
	 *            The status of the Sone
	 */
	public void setSoneStatus(Sone sone, SoneStatus soneStatus) {
		soneStatuses.put(sone, soneStatus);
	}

	/**
	 * Creates a new post and adds it to the given Sone.
	 *
	 * @param sone
	 *            The sone that creates the post
	 * @param text
	 *            The text of the post
	 * @return The created post
	 */
	public Post createPost(Sone sone, String text) {
		return createPost(sone, System.currentTimeMillis(), text);
	}

	/**
	 * Creates a new post and adds it to the given Sone.
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
		Post post = getPost(UUID.randomUUID().toString()).setSone(sone).setTime(time).setText(text);
		sone.addPost(post);
		return post;
	}

	/**
	 * Creates a reply.
	 *
	 * @param sone
	 *            The Sone that posts the reply
	 * @param post
	 *            The post the reply refers to
	 * @param text
	 *            The text of the reply
	 * @return The created reply
	 */
	public Reply createReply(Sone sone, Post post, String text) {
		return createReply(sone, post, System.currentTimeMillis(), text);
	}

	/**
	 * Creates a reply.
	 *
	 * @param sone
	 *            The Sone that posts the reply
	 * @param post
	 *            The post the reply refers to
	 * @param time
	 *            The time of the post
	 * @param text
	 *            The text of the reply
	 * @return The created reply
	 */
	public Reply createReply(Sone sone, Post post, long time, String text) {
		Reply reply = getReply(UUID.randomUUID().toString()).setSone(sone).setPost(post).setTime(time).setText(text);
		sone.addReply(reply);
		return reply;
	}

	//
	// ACTIONS
	//

	/**
	 * Adds a Sone to watch for updates. The Sone needs to be completely
	 * initialized.
	 *
	 * @param sone
	 *            The Sone to watch for updates
	 */
	public void addSone(Sone sone) {
		soneCache.put(sone.getId(), sone);
		if (!localSones.contains(sone)) {
			soneDownloader.addSone(sone);
		}
	}

	/**
	 * Adds the given Sone.
	 *
	 * @param sone
	 *            The Sone to add
	 */
	public void addLocalSone(Sone sone) {
		if (localSones.add(sone)) {
			setSoneStatus(sone, SoneStatus.idle);
			SoneInserter soneInserter = new SoneInserter(this, freenetInterface, sone);
			soneInserter.start();
			soneInserters.put(sone, soneInserter);
		}
	}

	/**
	 * Blackslists the given Sone.
	 *
	 * @param sone
	 *            The Sone to blacklist
	 */
	public void blacklistSone(Sone sone) {
		if (blacklistedSones.add(sone)) {
			soneDownloader.removeSone(sone);
			if (localSones.remove(sone)) {
				SoneInserter soneInserter = soneInserters.remove(sone);
				soneInserter.stop();
			}
		}
	}

	/**
	 * Unblacklists the given Sone.
	 *
	 * @param sone
	 *            The Sone to unblacklist
	 */
	public void unblacklistSone(Sone sone) {
		if (blacklistedSones.remove(sone)) {
			if (sone.getInsertUri() != null) {
				addLocalSone(sone);
			} else {
				addSone(sone);
			}
		}
	}

	/**
	 * Creates a new Sone at a random location.
	 *
	 * @param name
	 *            The name of the Sone
	 * @return The created Sone
	 * @throws SoneException
	 *             if a Sone error occurs
	 */
	public Sone createSone(String name) throws SoneException {
		return createSone(name, "Sone", null, null);
	}

	/**
	 * Creates a new Sone at the given location. If one of {@code requestUri} or
	 * {@code insertUrI} is {@code null}, the Sone is created at a random
	 * location.
	 *
	 * @param name
	 *            The name of the Sone
	 * @param documentName
	 *            The document name in the SSK
	 * @param requestUri
	 *            The request URI of the Sone, or {@link NullPointerException}
	 *            to create a Sone at a random location
	 * @param insertUri
	 *            The insert URI of the Sone, or {@code null} to create a Sone
	 *            at a random location
	 * @return The created Sone
	 * @throws SoneException
	 *             if a Sone error occurs
	 */
	public Sone createSone(String name, String documentName, String requestUri, String insertUri) throws SoneException {
		if ((name == null) || (name.trim().length() == 0)) {
			throw new SoneException(Type.INVALID_SONE_NAME);
		}
		String finalRequestUri;
		String finalInsertUri;
		if ((requestUri == null) || (insertUri == null)) {
			String[] keyPair = freenetInterface.generateKeyPair();
			finalRequestUri = keyPair[0];
			finalInsertUri = keyPair[1];
		} else {
			finalRequestUri = requestUri;
			finalInsertUri = insertUri;
		}
		Sone sone;
		try {
			logger.log(Level.FINEST, "Creating new Sone “%s” at %s (%s)…", new Object[] { name, finalRequestUri, finalInsertUri });
			sone = getSone(UUID.randomUUID().toString()).setName(name).setRequestUri(new FreenetURI(finalRequestUri).setKeyType("USK").setDocName(documentName)).setInsertUri(new FreenetURI(finalInsertUri).setKeyType("USK").setDocName(documentName));
			sone.setProfile(new Profile());
			/* set modification counter to 1 so it is inserted immediately. */
			sone.setModificationCounter(1);
			addLocalSone(sone);
		} catch (MalformedURLException mue1) {
			throw new SoneException(Type.INVALID_URI);
		}
		return sone;
	}

	/**
	 * Loads the Sone from the given request URI. The fetching of the data is
	 * performed in a new thread so this method returns immediately.
	 *
	 * @param requestUri
	 *            The request URI to load the Sone from
	 */
	public void loadSone(final String requestUri) {
		loadSone(requestUri, null);
	}

	/**
	 * Loads the Sone from the given request URI. The fetching of the data is
	 * performed in a new thread so this method returns immediately. If
	 * {@code insertUri} is not {@code null} the loaded Sone is converted into a
	 * local Sone and available using as any other local Sone.
	 *
	 * @param requestUri
	 *            The request URI to load the Sone from
	 * @param insertUri
	 *            The insert URI of the Sone
	 */
	public void loadSone(final String requestUri, final String insertUri) {
		new Thread(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				try {
					FreenetURI realRequestUri = new FreenetURI(requestUri).setMetaString(new String[] { "sone.xml" });
					FetchResult fetchResult = freenetInterface.fetchUri(realRequestUri);
					if (fetchResult == null) {
						return;
					}
					Sone parsedSone = soneDownloader.parseSone(null, fetchResult, realRequestUri);
					if (parsedSone != null) {
						if (insertUri != null) {
							parsedSone.setInsertUri(new FreenetURI(insertUri));
							addLocalSone(parsedSone);
						} else {
							addSone(parsedSone);
						}
						setSoneStatus(parsedSone, SoneStatus.idle);
					}
				} catch (MalformedURLException mue1) {
					logger.log(Level.INFO, "Could not create URI from “" + requestUri + "”.", mue1);
				}
			}
		}, "Sone Downloader").start();
	}

	/**
	 * Loads a Sone from an input stream.
	 *
	 * @param soneInputStream
	 *            The input stream to load the Sone from
	 * @return The parsed Sone, or {@code null} if the Sone could not be parsed
	 */
	public Sone loadSone(InputStream soneInputStream) {
		Sone parsedSone = soneDownloader.parseSone(soneInputStream);
		if (parsedSone == null) {
			return null;
		}
		if (parsedSone.getInsertUri() != null) {
			addLocalSone(parsedSone);
		} else {
			addSone(parsedSone);
		}
		return parsedSone;
	}

	/**
	 * Loads and updates the given Sone.
	 *
	 * @param sone
	 *            The Sone to load
	 */
	public void loadSone(final Sone sone) {
		new Thread(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				FreenetURI realRequestUri = sone.getRequestUri().setMetaString(new String[] { "sone.xml" });
				setSoneStatus(sone, SoneStatus.downloading);
				try {
					FetchResult fetchResult = freenetInterface.fetchUri(realRequestUri);
					if (fetchResult == null) {
						/* TODO - mark Sone as bad. */
						return;
					}
					Sone parsedSone = soneDownloader.parseSone(sone, fetchResult, realRequestUri);
					if (parsedSone != null) {
						addSone(parsedSone);
					}
				} finally {
					setSoneStatus(sone, (sone.getTime() == 0) ? SoneStatus.unknown : SoneStatus.idle);
				}
			}
		}, "Sone Downloader").start();
	}

	/**
	 * Deletes the given Sone from this plugin instance.
	 *
	 * @param sone
	 *            The sone to delete
	 */
	public void deleteSone(Sone sone) {
		SoneInserter soneInserter = soneInserters.remove(sone);
		soneInserter.stop();
		localSones.remove(sone);
		soneStatuses.remove(sone);
		soneCache.remove(sone.getId());
	}

	/**
	 * Returns the post with the given ID. If no post exists yet with the given
	 * ID, a new post is returned.
	 *
	 * @param postId
	 *            The ID of the post
	 * @return The post
	 */
	public Post getPost(String postId) {
		if (!postCache.containsKey(postId)) {
			postCache.put(postId, new Post(postId));
		}
		return postCache.get(postId);
	}

	/**
	 * Returns the reply with the given ID. If no reply exists yet with the
	 * given ID, a new reply is returned.
	 *
	 * @param replyId
	 *            The ID of the reply
	 * @return The reply
	 */
	public Reply getReply(String replyId) {
		if (!replyCache.containsKey(replyId)) {
			replyCache.put(replyId, new Reply(replyId));
		}
		return replyCache.get(replyId);
	}

	/**
	 * Gets all replies to the given post, sorted by date, oldest first.
	 *
	 * @param post
	 *            The post the replies refer to
	 * @return The sorted list of replies for the post
	 */
	public List<Reply> getReplies(Post post) {
		List<Reply> replies = new ArrayList<Reply>();
		for (Reply reply : replyCache.values()) {
			if (reply.getPost().equals(post)) {
				replies.add(reply);
			}
		}
		Collections.sort(replies, new Comparator<Reply>() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public int compare(Reply leftReply, Reply rightReply) {
				return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, leftReply.getTime() - rightReply.getTime()));
			}
		});
		return replies;
	}

	/**
	 * Gets all Sones that like the given post.
	 *
	 * @param post
	 *            The post to check for
	 * @return All Sones that like the post
	 */
	public Collection<Sone> getLikes(final Post post) {
		return Filters.filteredCollection(getKnownSones(), new Filter<Sone>() {

			@Override
			public boolean filterObject(Sone sone) {
				return sone.isLikedPostId(post.getId());
			}
		});
	}

	/**
	 * Gets all Sones that like the given reply.
	 *
	 * @param reply
	 *            The reply to check for
	 * @return All Sones that like the reply
	 */
	public Collection<Sone> getLikes(final Reply reply) {
		return Filters.filteredCollection(getKnownSones(), new Filter<Sone>() {

			@Override
			public boolean filterObject(Sone sone) {
				return sone.isLikedReplyId(reply.getId());
			}
		});
	}

	/**
	 * Deletes the given reply. It is removed from its Sone and from the reply
	 * cache.
	 *
	 * @param reply
	 *            The reply to remove
	 */
	public void deleteReply(Reply reply) {
		reply.getSone().removeReply(reply);
		replyCache.remove(reply.getId());
	}

	//
	// SERVICE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void serviceStart() {
		loadConfiguration();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void serviceStop() {
		soneDownloader.stop();
		/* stop all Sone inserters. */
		for (SoneInserter soneInserter : soneInserters.values()) {
			soneInserter.stop();
		}
		saveConfiguration();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Adds some default Sones.
	 */
	private void addDefaultSones() {
		for (String soneUri : defaultSones) {
			loadSone(soneUri);
		}
	}

	/**
	 * Loads the configuration.
	 */
	@SuppressWarnings("unchecked")
	private void loadConfiguration() {
		logger.entering(Core.class.getName(), "loadConfiguration()");

		boolean firstStart = configuration.getBooleanValue("FirstStart").getValue(true);
		if (firstStart) {
			logger.log(Level.INFO, "First start of Sone, adding a couple of default Sones…");
			addDefaultSones();
			try {
				configuration.getBooleanValue("FirstStart").setValue(false);
			} catch (ConfigurationException ce1) {
				logger.log(Level.WARNING, "Could not clear “first start” flag!");
			}
		}

		options.addIntegerOption("InsertionDelay", new DefaultOption<Integer>(60, new OptionWatcher<Integer>() {

			@Override
			public void optionChanged(Option<Integer> option, Integer oldValue, Integer newValue) {
				SoneInserter.setInsertionDelay(newValue);
			}

		}));

		options.addBooleanOption("ClearOnNextRestart", new DefaultOption<Boolean>(false));
		options.addBooleanOption("ReallyClearOnNextRestart", new DefaultOption<Boolean>(false));

		if (firstStart) {
			return;
		}

		options.getBooleanOption("ClearOnNextRestart").set(configuration.getBooleanValue("Option/ClearOnNextRestart").getValue(null));
		options.getBooleanOption("ReallyClearOnNextRestart").set(configuration.getBooleanValue("Option/ReallyClearOnNextRestart").getValue(null));
		boolean clearConfiguration = options.getBooleanOption("ClearOnNextRestart").get() && options.getBooleanOption("ReallyClearOnNextRestart").get();
		options.getBooleanOption("ClearOnNextRestart").set(null);
		options.getBooleanOption("ReallyClearOnNextRestart").set(null);
		if (clearConfiguration) {
			/* stop loading the configuration. */
			addDefaultSones();
			return;
		}

		options.getIntegerOption("InsertionDelay").set(configuration.getIntValue("Option/InsertionDelay").getValue(null));

		/* parse local Sones. */
		logger.log(Level.INFO, "Loading Sones…");
		int soneId = 0;
		do {
			String sonePrefix = "Sone/Sone." + soneId++;
			String id = configuration.getStringValue(sonePrefix + "/ID").getValue(null);
			if (id == null) {
				break;
			}
			String name = configuration.getStringValue(sonePrefix + "/Name").getValue(null);
			long time = configuration.getLongValue(sonePrefix + "/Time").getValue((long) 0);
			String insertUri = configuration.getStringValue(sonePrefix + "/InsertURI").getValue(null);
			String requestUri = configuration.getStringValue(sonePrefix + "/RequestURI").getValue(null);
			long modificationCounter = configuration.getLongValue(sonePrefix + "/ModificationCounter").getValue((long) 0);
			String firstName = configuration.getStringValue(sonePrefix + "/Profile/FirstName").getValue(null);
			String middleName = configuration.getStringValue(sonePrefix + "/Profile/MiddleName").getValue(null);
			String lastName = configuration.getStringValue(sonePrefix + "/Profile/LastName").getValue(null);
			Integer birthDay = configuration.getIntValue(sonePrefix + "/Profile/BirthDay").getValue(null);
			Integer birthMonth = configuration.getIntValue(sonePrefix + "/Profile/BirthMonth").getValue(null);
			Integer birthYear = configuration.getIntValue(sonePrefix + "/Profile/BirthYear").getValue(null);
			try {
				Profile profile = new Profile();
				profile.setFirstName(firstName);
				profile.setMiddleName(middleName);
				profile.setLastName(lastName);
				profile.setBirthDay(birthDay).setBirthMonth(birthMonth).setBirthYear(birthYear);
				Sone sone = getSone(id).setName(name).setTime(time).setRequestUri(new FreenetURI(requestUri)).setInsertUri(new FreenetURI(insertUri));
				sone.setProfile(profile);
				int postId = 0;
				do {
					String postPrefix = sonePrefix + "/Post." + postId++;
					id = configuration.getStringValue(postPrefix + "/ID").getValue(null);
					if (id == null) {
						break;
					}
					time = configuration.getLongValue(postPrefix + "/Time").getValue((long) 0);
					String text = configuration.getStringValue(postPrefix + "/Text").getValue(null);
					Post post = getPost(id).setSone(sone).setTime(time).setText(text);
					sone.addPost(post);
				} while (true);
				int replyCounter = 0;
				do {
					String replyPrefix = sonePrefix + "/Reply." + replyCounter++;
					String replyId = configuration.getStringValue(replyPrefix + "/ID").getValue(null);
					if (replyId == null) {
						break;
					}
					Post replyPost = getPost(configuration.getStringValue(replyPrefix + "/Post").getValue(null));
					long replyTime = configuration.getLongValue(replyPrefix + "/Time").getValue(null);
					String replyText = configuration.getStringValue(replyPrefix + "/Text").getValue(null);
					Reply reply = getReply(replyId).setSone(sone).setPost(replyPost).setTime(replyTime).setText(replyText);
					sone.addReply(reply);
				} while (true);

				/* load friends. */
				int friendCounter = 0;
				while (true) {
					String friendPrefix = sonePrefix + "/Friend." + friendCounter++;
					String friendId = configuration.getStringValue(friendPrefix + "/ID").getValue(null);
					if (friendId == null) {
						break;
					}
					Sone friendSone = getSone(friendId);
					String friendKey = configuration.getStringValue(friendPrefix + "/Key").getValue(null);
					String friendName = configuration.getStringValue(friendPrefix + "/Name").getValue(null);
					friendSone.setRequestUri(new FreenetURI(friendKey)).setName(friendName);
					sone.addFriend(friendSone);
				}

				/* load blocked Sone IDs. */
				int blockedSoneCounter = 0;
				while (true) {
					String blockedSonePrefix = sonePrefix + "/BlockedSone." + blockedSoneCounter++;
					String blockedSoneId = configuration.getStringValue(blockedSonePrefix + "/ID").getValue(null);
					if (blockedSoneId == null) {
						break;
					}
					sone.addBlockedSoneId(blockedSoneId);
				}

				/* load liked post IDs. */
				int likedPostIdCounter = 0;
				while (true) {
					String likedPostIdPrefix = sonePrefix + "/LikedPostId." + likedPostIdCounter++;
					String likedPostId = configuration.getStringValue(likedPostIdPrefix + "/ID").getValue(null);
					if (likedPostId == null) {
						break;
					}
					sone.addLikedPostId(likedPostId);
				}

				/* load liked reply IDs. */
				int likedReplyIdCounter = 0;
				while (true) {
					String likedReplyIdPrefix = sonePrefix + "/LikedReplyId." + likedReplyIdCounter++;
					String likedReplyId = configuration.getStringValue(likedReplyIdPrefix + "/ID").getValue(null);
					if (likedReplyId == null) {
						break;
					}
					sone.addLikedReplyId(likedReplyId);
				}

				sone.setModificationCounter(modificationCounter);
				addLocalSone(sone);
			} catch (MalformedURLException mue1) {
				logger.log(Level.WARNING, "Could not create Sone from requestUri (“" + requestUri + "”) and insertUri (“" + insertUri + "”)!", mue1);
			}
		} while (true);
		logger.log(Level.INFO, "Loaded %d Sones.", getSones().size());

		/* load all known Sones. */
		int knownSonesCounter = 0;
		while (true) {
			String knownSonePrefix = "KnownSone." + knownSonesCounter++;
			String knownSoneId = configuration.getStringValue(knownSonePrefix + "/ID").getValue(null);
			if (knownSoneId == null) {
				break;
			}
			String knownSoneName = configuration.getStringValue(knownSonePrefix + "/Name").getValue(null);
			String knownSoneKey = configuration.getStringValue(knownSonePrefix + "/Key").getValue(null);
			try {
				getSone(knownSoneId).setName(knownSoneName).setRequestUri(new FreenetURI(knownSoneKey));
			} catch (MalformedURLException mue1) {
				logger.log(Level.WARNING, "Could not create Sone from requestUri (“" + knownSoneKey + "”)!", mue1);
			}
		}

		/* load all blacklisted Sones. */
		int blacklistedSonesCounter = 0;
		while (true) {
			String blacklistedSonePrefix = "BlacklistedSone." + blacklistedSonesCounter++;
			String blacklistedSoneId = configuration.getStringValue(blacklistedSonePrefix + "/ID").getValue(null);
			if (blacklistedSoneId == null) {
				break;
			}
			String blacklistedSoneName = configuration.getStringValue(blacklistedSonePrefix + "/Name").getValue(null);
			String blacklistedSoneKey = configuration.getStringValue(blacklistedSonePrefix + "/Key").getValue(null);
			String blacklistedSoneInsertKey = configuration.getStringValue(blacklistedSonePrefix + "/InsertKey").getValue(null);
			try {
				blacklistSone(getSone(blacklistedSoneId).setName(blacklistedSoneName).setRequestUri(new FreenetURI(blacklistedSoneKey)).setInsertUri((blacklistedSoneInsertKey != null) ? new FreenetURI(blacklistedSoneInsertKey) : null));
			} catch (MalformedURLException mue1) {
				logger.log(Level.WARNING, "Could not create blacklisted Sone from requestUri (“" + blacklistedSoneKey + "”)!", mue1);
			}
		}

		/* load all remote Sones. */
		for (Sone remoteSone : getRemoteSones()) {
			loadSone(remoteSone);
		}

		logger.exiting(Core.class.getName(), "loadConfiguration()");
	}

	/**
	 * Saves the configuraiton.
	 */
	private void saveConfiguration() {
		Set<Sone> sones = getSones();
		logger.log(Level.INFO, "Storing %d Sones…", sones.size());

		try {
			/* store the options first. */
			configuration.getIntValue("Option/InsertionDelay").setValue(options.getIntegerOption("InsertionDelay").getReal());
			configuration.getBooleanValue("Option/ClearOnNextRestart").setValue(options.getBooleanOption("ClearOnNextRestart").getReal());
			configuration.getBooleanValue("Option/ReallyClearOnNextRestart").setValue(options.getBooleanOption("ReallyClearOnNextRestart").getReal());

			/* store all Sones. */
			int soneId = 0;
			for (Sone sone : localSones) {
				String sonePrefix = "Sone/Sone." + soneId++;
				configuration.getStringValue(sonePrefix + "/ID").setValue(sone.getId());
				configuration.getStringValue(sonePrefix + "/Name").setValue(sone.getName());
				configuration.getLongValue(sonePrefix + "/Time").setValue(sone.getTime());
				configuration.getStringValue(sonePrefix + "/RequestURI").setValue(sone.getRequestUri().toString());
				configuration.getStringValue(sonePrefix + "/InsertURI").setValue(sone.getInsertUri().toString());
				configuration.getLongValue(sonePrefix + "/ModificationCounter").setValue(sone.getModificationCounter());
				Profile profile = sone.getProfile();
				configuration.getStringValue(sonePrefix + "/Profile/FirstName").setValue(profile.getFirstName());
				configuration.getStringValue(sonePrefix + "/Profile/MiddleName").setValue(profile.getMiddleName());
				configuration.getStringValue(sonePrefix + "/Profile/LastName").setValue(profile.getLastName());
				configuration.getIntValue(sonePrefix + "/Profile/BirthDay").setValue(profile.getBirthDay());
				configuration.getIntValue(sonePrefix + "/Profile/BirthMonth").setValue(profile.getBirthMonth());
				configuration.getIntValue(sonePrefix + "/Profile/BirthYear").setValue(profile.getBirthYear());
				int postId = 0;
				for (Post post : sone.getPosts()) {
					String postPrefix = sonePrefix + "/Post." + postId++;
					configuration.getStringValue(postPrefix + "/ID").setValue(post.getId());
					configuration.getLongValue(postPrefix + "/Time").setValue(post.getTime());
					configuration.getStringValue(postPrefix + "/Text").setValue(post.getText());
				}
				/* write null ID as terminator. */
				configuration.getStringValue(sonePrefix + "/Post." + postId + "/ID").setValue(null);

				int replyId = 0;
				for (Reply reply : sone.getReplies()) {
					String replyPrefix = sonePrefix + "/Reply." + replyId++;
					configuration.getStringValue(replyPrefix + "/ID").setValue(reply.getId());
					configuration.getStringValue(replyPrefix + "/Post").setValue(reply.getPost().getId());
					configuration.getLongValue(replyPrefix + "/Time").setValue(reply.getTime());
					configuration.getStringValue(replyPrefix + "/Text").setValue(reply.getText());
				}
				/* write null ID as terminator. */
				configuration.getStringValue(sonePrefix + "/Reply." + replyId + "/ID").setValue(null);

				int friendId = 0;
				for (Sone friend : sone.getFriends()) {
					String friendPrefix = sonePrefix + "/Friend." + friendId++;
					configuration.getStringValue(friendPrefix + "/ID").setValue(friend.getId());
					configuration.getStringValue(friendPrefix + "/Key").setValue(friend.getRequestUri().toString());
					configuration.getStringValue(friendPrefix + "/Name").setValue(friend.getName());
				}
				/* write null ID as terminator. */
				configuration.getStringValue(sonePrefix + "/Friend." + friendId + "/ID").setValue(null);

				/* write all blocked Sones. */
				int blockedSoneCounter = 0;
				for (String blockedSoneId : sone.getBlockedSoneIds()) {
					String blockedSonePrefix = sonePrefix + "/BlockedSone." + blockedSoneCounter++;
					configuration.getStringValue(blockedSonePrefix + "/ID").setValue(blockedSoneId);
				}
				configuration.getStringValue(sonePrefix + "/BlockedSone." + blockedSoneCounter + "/ID").setValue(null);

				/* write all liked posts. */
				int likedPostIdCounter = 0;
				for (String soneLikedPostId : sone.getLikedPostIds()) {
					String likedPostIdPrefix = sonePrefix + "/LikedPostId." + likedPostIdCounter++;
					configuration.getStringValue(likedPostIdPrefix + "/ID").setValue(soneLikedPostId);
				}
				configuration.getStringValue(sonePrefix + "/LikedPostId." + likedPostIdCounter + "/ID").setValue(null);

				/* write all liked replies. */
				int likedReplyIdCounter = 0;
				for (String soneLikedReplyId : sone.getLikedReplyIds()) {
					String likedReplyIdPrefix = sonePrefix + "/LikedReplyId." + likedReplyIdCounter++;
					configuration.getStringValue(likedReplyIdPrefix + "/ID").setValue(soneLikedReplyId);
				}
				configuration.getStringValue(sonePrefix + "/LikedReplyId." + likedReplyIdCounter + "/ID").setValue(null);

			}
			/* write null ID as terminator. */
			configuration.getStringValue("Sone/Sone." + soneId + "/ID").setValue(null);

			/* write all known Sones. */
			int knownSonesCounter = 0;
			for (Sone knownSone : getRemoteSones()) {
				String knownSonePrefix = "KnownSone." + knownSonesCounter++;
				configuration.getStringValue(knownSonePrefix + "/ID").setValue(knownSone.getId());
				configuration.getStringValue(knownSonePrefix + "/Name").setValue(knownSone.getName());
				configuration.getStringValue(knownSonePrefix + "/Key").setValue(knownSone.getRequestUri().toString());
				/* TODO - store all known stuff? */
			}
			configuration.getStringValue("KnownSone." + knownSonesCounter + "/ID").setValue(null);

			/* write all blacklisted Sones. */
			int blacklistedSonesCounter = 0;
			for (Sone blacklistedSone : getBlacklistedSones()) {
				String blacklistedSonePrefix = "BlacklistedSone." + blacklistedSonesCounter++;
				configuration.getStringValue(blacklistedSonePrefix + "/ID").setValue(blacklistedSone.getId());
				configuration.getStringValue(blacklistedSonePrefix + "/Name").setValue(blacklistedSone.getName());
				configuration.getStringValue(blacklistedSonePrefix + "/Key").setValue(blacklistedSone.getRequestUri().toString());
				configuration.getStringValue(blacklistedSonePrefix + "/InsertKey").setValue((blacklistedSone.getInsertUri() != null) ? blacklistedSone.getInsertUri().toString() : null);
				/* TODO - store all known stuff? */
			}
			configuration.getStringValue("BlacklistedSone." + blacklistedSonesCounter + "/ID").setValue(null);

		} catch (ConfigurationException ce1) {
			logger.log(Level.WARNING, "Could not store configuration!", ce1);
		}
	}

}
