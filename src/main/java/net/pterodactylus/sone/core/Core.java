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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.SoneException.Type;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.service.AbstractService;
import net.pterodactylus.util.text.StringEscaper;
import net.pterodactylus.util.text.TextException;
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

	/** The local Sones. */
	private final Set<Sone> localSones = new HashSet<Sone>();

	/** Sone inserters. */
	private final Map<Sone, SoneInserter> soneInserters = new HashMap<Sone, SoneInserter>();

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
			SoneInserter soneInserter = new SoneInserter(sone);
			soneInserter.start();
			soneInserters.put(sone, soneInserter);
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
			sone = new Sone(UUID.randomUUID(), name, new FreenetURI(finalRequestUri), new FreenetURI(finalInsertUri));
			addSone(sone);
		} catch (MalformedURLException mue1) {
			throw new SoneException(Type.INVALID_URI);
		}
		localSones.add(sone);
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

		/* get names of all local Sones. */
		String allSoneNamesString = configuration.getStringValue("Sone/Names").getValue(null);
		if (allSoneNamesString == null) {
			allSoneNamesString = "";
		}
		List<String> allSoneNames;
		try {
			allSoneNames = StringEscaper.parseLine(allSoneNamesString);
		} catch (TextException te1) {
			logger.log(Level.WARNING, "Could not parse Sone names: “" + allSoneNamesString + "”", te1);
			allSoneNames = Collections.emptyList();
		}

		/* parse local Sones. */
		logger.log(Level.INFO, "Loading %d Sones…", allSoneNames.size());
		for (String soneName : allSoneNames) {
			String id = configuration.getStringValue("Sone/Name." + soneName + "/ID").getValue(null);
			String insertUri = configuration.getStringValue("Sone/Name." + soneName + "/InsertURI").getValue(null);
			String requestUri = configuration.getStringValue("Sone/Name." + soneName + "/RequestURI").getValue(null);
			long modificationCounter = configuration.getLongValue("Sone/Name." + soneName + "/ModificationCounter").getValue((long) 0);
			try {
				Sone sone = new Sone(UUID.fromString(id), soneName, new FreenetURI(requestUri), new FreenetURI(insertUri));
				sone.setModificationCounter(modificationCounter);
				addSone(sone);
			} catch (MalformedURLException mue1) {
				logger.log(Level.WARNING, "Could not create Sone from requestUri (“" + requestUri + "”) and insertUri (“" + insertUri + "”)!", mue1);
			}
		}

		logger.exiting(Core.class.getName(), "loadConfiguration()");
	}

	/**
	 * Saves the configuraiton.
	 */
	private void saveConfiguration() {

		/* get the names of all Sones. */
		Set<String> soneNames = new HashSet<String>();
		for (Sone sone : localSones) {
			soneNames.add(sone.getName());
		}
		String soneNamesString = StringEscaper.escapeWords(soneNames);

		logger.log(Level.INFO, "Storing %d Sones…", soneNames.size());
		try {
			/* store names of all Sones. */
			configuration.getStringValue("Sone/Names").setValue(soneNamesString);

			/* store all Sones. */
			for (Sone sone : localSones) {
				configuration.getStringValue("Sone/Name." + sone.getName() + "/ID").setValue(sone.getId());
				configuration.getStringValue("Sone/Name." + sone.getName() + "/RequestURI").setValue(sone.getRequestUri().toString());
				configuration.getStringValue("Sone/Name." + sone.getName() + "/InsertURI").setValue(sone.getInsertUri().toString());
				configuration.getLongValue("Sone/Name." + sone.getName() + "/ModificationCounter").setValue(sone.getModificationCounter());
			}
		} catch (ConfigurationException ce1) {
			logger.log(Level.WARNING, "Could not store configuration!", ce1);
		}
	}

}
