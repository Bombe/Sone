/*
 * FreenetSone - FreenetInterface.java - Copyright © 2010 David Roden
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

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.service.AbstractService;
import freenet.client.FetchException;
import freenet.client.FetchResult;
import freenet.client.HighLevelSimpleClient;
import freenet.client.InsertException;
import freenet.keys.FreenetURI;
import freenet.node.Node;

/**
 * Contains all necessary functionality for interacting with the Freenet node.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreenetInterface extends AbstractService {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(FreenetInterface.class);

	/** The node to interact with. */
	@SuppressWarnings("unused")
	private final Node node;

	/** The high-level client to use for requests. */
	private final HighLevelSimpleClient client;

	/**
	 * Creates a new Freenet interface.
	 *
	 * @param node
	 *            The node to interact with
	 * @param client
	 *            The high-level client
	 */
	public FreenetInterface(Node node, HighLevelSimpleClient client) {
		this.node = node;
		this.client = client;
	}

	//
	// ACTIONS
	//

	/**
	 * Fetches the given URI.
	 *
	 * @param uri
	 *            The URI to fetch
	 * @return The result of the fetch, or {@code null} if an error occured
	 */
	public FetchResult fetchUri(FreenetURI uri) {
		logger.entering(FreenetInterface.class.getName(), "fetchUri(FreenetURI)", uri);
		FetchResult fetchResult = null;
		try {
			fetchResult = client.fetch(uri);
		} catch (FetchException fe1) {
			logger.log(Level.WARNING, "Could not fetch “" + uri + "”!", fe1);
		} finally {
			logger.exiting(FreenetInterface.class.getName(), "fetchUri(FreenetURI)", fetchResult);
		}
		return fetchResult;
	}

	/**
	 * Creates a key pair.
	 *
	 * @return The request key at index 0, the insert key at index 1
	 */
	public String[] generateKeyPair() {
		FreenetURI[] keyPair = client.generateKeyPair("");
		return new String[] { keyPair[1].toString(), keyPair[0].toString() };
	}

	/**
	 * Inserts a directory into Freenet.
	 *
	 * @param insertUri
	 *            The insert URI
	 * @param manifestEntries
	 *            The directory entries
	 * @param defaultFile
	 *            The name of the default file
	 * @return The generated URI
	 * @throws SoneException
	 *             if an insert error occurs
	 */
	public FreenetURI insertDirectory(FreenetURI insertUri, HashMap<String, Object> manifestEntries, String defaultFile) throws SoneException {
		try {
			return client.insertManifest(insertUri, manifestEntries, defaultFile);
		} catch (InsertException ie1) {
			throw new SoneException(null, ie1);
		}
	}

}
