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

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.SoneException.Type;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.service.AbstractService;
import freenet.keys.FreenetURI;

/**
 * The Sone core.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Core extends AbstractService {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(Core.class);

	/** The configuration. */
	private Configuration configuration;

	/** Interface to freenet. */
	private FreenetInterface freenetInterface;

	/** The Sone downloader. */
	private SoneDownloader soneDownloader;

	/** The local Sones. */
	private final Set<Sone> localSones = new HashSet<Sone>();

	/** Sone inserters. */
	private final Map<Sone, SoneInserter> soneInserters = new HashMap<Sone, SoneInserter>();

	/* various caches follow here. */

	/** Cache for all known Sones. */
	private final Map<String, Sone> soneCache = new HashMap<String, Sone>();

	/** Cache for all known posts. */
	private final Map<String, Post> postCache = new HashMap<String, Post>();

	/** Cache for all known replies. */
	private final Map<String, Reply> replyCache = new HashMap<String, Reply>();

	/**
	 * Creates a new core.
	 */
	public Core() {
		super("Sone Core");
	}

	//
	// ACCESSORS
	//

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
		soneDownloader = new SoneDownloader(freenetInterface);
		soneDownloader.start();
		return this;
	}

	/**
	 * Returns the local Sones.
	 *
	 * @return The local Sones
	 */
	public Set<Sone> getSones() {
		return Collections.unmodifiableSet(localSones);
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
		}
		return soneCache.get(soneId);
	}

	//
	// ACTIONS
	//

	/**
	 * Adds the given Sone.
	 *
	 * @param sone
	 *            The Sone to add
	 */
	public void addSone(Sone sone) {
		if (localSones.add(sone)) {
			soneCache.put(sone.getId(), sone);
			SoneInserter soneInserter = new SoneInserter(freenetInterface, sone);
			soneInserter.start();
			soneDownloader.addSone(sone);
			soneInserters.put(sone, soneInserter);
		}
	}

	/**
	 * Adds a remote Sone so that it is watched for updates.
	 *
	 * @param sone
	 *            The sone to watch
	 */
	public void addRemoteSone(Sone sone) {
		if (!soneCache.containsKey(sone.getId())) {
			soneCache.put(sone.getId(), sone);
		}
		soneDownloader.addSone(sone);
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
		return createSone(name, null, null);
	}

	/**
	 * Creates a new Sone at the given location. If one of {@code requestUri} or
	 * {@code insertUrI} is {@code null}, the Sone is created at a random
	 * location.
	 *
	 * @param name
	 *            The name of the Sone
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
	public Sone createSone(String name, String requestUri, String insertUri) throws SoneException {
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
			sone = new Sone(UUID.randomUUID().toString()).setName(name).setRequestUri(new FreenetURI(finalRequestUri).setKeyType("USK").setDocName("Sone-" + name)).setInsertUri(new FreenetURI(finalInsertUri).setKeyType("USK").setDocName("Sone-" + name));
			sone.setProfile(new Profile());
			/* set modification counter to 1 so it is inserted immediately. */
			sone.setModificationCounter(1);
			addSone(sone);
		} catch (MalformedURLException mue1) {
			throw new SoneException(Type.INVALID_URI);
		}
		return sone;
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
	 * Loads the configuration.
	 */
	private void loadConfiguration() {
		logger.entering(Core.class.getName(), "loadConfiguration()");

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
			String insertUri = configuration.getStringValue(sonePrefix + "/InsertURI").getValue(null);
			String requestUri = configuration.getStringValue(sonePrefix + "/RequestURI").getValue(null);
			long modificationCounter = configuration.getLongValue(sonePrefix + "/ModificationCounter").getValue((long) 0);
			String firstName = configuration.getStringValue(sonePrefix + "/Profile/FirstName").getValue(null);
			String middleName = configuration.getStringValue(sonePrefix + "/Profile/MiddleName").getValue(null);
			String lastName = configuration.getStringValue(sonePrefix + "/Profile/LastName").getValue(null);
			try {
				Profile profile = new Profile();
				profile.setFirstName(firstName);
				profile.setMiddleName(middleName);
				profile.setLastName(lastName);
				Sone sone = new Sone(id).setName(name).setRequestUri(new FreenetURI(requestUri)).setInsertUri(new FreenetURI(insertUri));
				soneCache.put(id, sone);
				sone.setProfile(profile);
				int postId = 0;
				do {
					String postPrefix = sonePrefix + "/Post." + postId++;
					id = configuration.getStringValue(postPrefix + "/ID").getValue(null);
					if (id == null) {
						break;
					}
					long time = configuration.getLongValue(postPrefix + "/Time").getValue(null);
					String text = configuration.getStringValue(postPrefix + "/Text").getValue(null);
					Post post = new Post(id, sone, time, text);
					postCache.put(id, post);
					sone.addPost(post);
				} while (true);
				int replyCounter = 0;
				do {
					String replyPrefix = sonePrefix + "/Reply." + replyCounter++;
					String replyId = configuration.getStringValue(replyPrefix + "/ID").getValue(null);
					if (replyId == null) {
						break;
					}
					Sone replySone = getSone(configuration.getStringValue(replyPrefix + "/Sone/ID").getValue(null));
					String replySoneKey = configuration.getStringValue(replyPrefix + "/Sone/Key").getValue(null);
					String replySoneName = configuration.getStringValue(replyPrefix + "/Sone/Name").getValue(null);
					replySone.setRequestUri(new FreenetURI(replySoneKey)).setName(replySoneName);
					Post replyPost = postCache.get(configuration.getStringValue(replyPrefix + "/Post").getValue(null));
					long replyTime = configuration.getLongValue(replyPrefix + "/Time").getValue(null);
					String replyText = configuration.getStringValue(replyPrefix + "/Text").getValue(null);
					Reply reply = new Reply(replyId, replySone, replyPost, replyTime, replyText);
					replyCache.put(replyId, reply);
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
					addRemoteSone(friendSone);
					sone.addFriend(sone);
				}

				sone.setModificationCounter(modificationCounter);
				addSone(sone);
			} catch (MalformedURLException mue1) {
				logger.log(Level.WARNING, "Could not create Sone from requestUri (“" + requestUri + "”) and insertUri (“" + insertUri + "”)!", mue1);
			}
		} while (true);
		logger.log(Level.INFO, "Loaded %d Sones.", getSones().size());

		logger.exiting(Core.class.getName(), "loadConfiguration()");
	}

	/**
	 * Saves the configuraiton.
	 */
	private void saveConfiguration() {
		Set<Sone> sones = getSones();
		logger.log(Level.INFO, "Storing %d Sones…", sones.size());
		try {
			/* store all Sones. */
			int soneId = 0;
			for (Sone sone : localSones) {
				String sonePrefix = "Sone/Sone." + soneId++;
				configuration.getStringValue(sonePrefix + "/ID").setValue(sone.getId());
				configuration.getStringValue(sonePrefix + "/Name").setValue(sone.getName());
				configuration.getStringValue(sonePrefix + "/RequestURI").setValue(sone.getRequestUri().toString());
				configuration.getStringValue(sonePrefix + "/InsertURI").setValue(sone.getInsertUri().toString());
				configuration.getLongValue(sonePrefix + "/ModificationCounter").setValue(sone.getModificationCounter());
				Profile profile = sone.getProfile();
				configuration.getStringValue(sonePrefix + "/Profile/FirstName").setValue(profile.getFirstName());
				configuration.getStringValue(sonePrefix + "/Profile/MiddleName").setValue(profile.getMiddleName());
				configuration.getStringValue(sonePrefix + "/Profile/LastName").setValue(profile.getLastName());
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
					configuration.getStringValue(replyPrefix + "/Sone/ID").setValue(reply.getSone().getId());
					configuration.getStringValue(replyPrefix + "/Sone/Key").setValue(reply.getSone().getRequestUri().toString());
					configuration.getStringValue(replyPrefix + "/Sone/Name").setValue(reply.getSone().getName());
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

			}
			/* write null ID as terminator. */
			configuration.getStringValue("Sone/Sone." + soneId + "/ID").setValue(null);

		} catch (ConfigurationException ce1) {
			logger.log(Level.WARNING, "Could not store configuration!", ce1);
		}
	}

}
