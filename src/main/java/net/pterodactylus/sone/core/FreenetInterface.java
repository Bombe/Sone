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

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.service.AbstractService;

import com.db4o.ObjectContainer;

import freenet.client.FetchException;
import freenet.client.FetchResult;
import freenet.client.HighLevelSimpleClient;
import freenet.client.HighLevelSimpleClientImpl;
import freenet.client.InsertException;
import freenet.client.async.ClientContext;
import freenet.client.async.USKCallback;
import freenet.keys.FreenetURI;
import freenet.keys.USK;
import freenet.node.Node;
import freenet.node.RequestStarter;

/**
 * Contains all necessary functionality for interacting with the Freenet node.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreenetInterface extends AbstractService {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(FreenetInterface.class);

	/** The node to interact with. */
	private final Node node;

	/** The high-level client to use for requests. */
	private final HighLevelSimpleClient client;

	/** The USK callbacks. */
	private final Map<String, USKCallback> soneUskCallbacks = new HashMap<String, USKCallback>();

	/**
	 * Creates a new Freenet interface.
	 *
	 * @param node
	 *            The node to interact with
	 * @param client
	 *            The high-level client
	 */
	public FreenetInterface(Node node, HighLevelSimpleClient client) {
		super("Sone Freenet Interface");
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
		FetchResult fetchResult = null;
		FreenetURI currentUri = new FreenetURI(uri);
		while (true) {
			try {
				fetchResult = client.fetch(currentUri);
				return fetchResult;
			} catch (FetchException fe1) {
				if (fe1.getMode() == FetchException.PERMANENT_REDIRECT) {
					currentUri = fe1.newURI;
					continue;
				}
				logger.log(Level.WARNING, "Could not fetch “" + uri + "”!", fe1);
				return null;
			}
		}
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

	/**
	 * Registers the USK for the given Sone and notifies the given
	 * {@link SoneDownloader} if an update was found.
	 *
	 * @param sone
	 *            The Sone to watch
	 * @param soneDownloader
	 *            The Sone download to notify on updates
	 */
	public void registerUsk(final Sone sone, final SoneDownloader soneDownloader) {
		try {
			logger.log(Level.FINE, "Registering Sone “%s” for USK updates at %s…", new Object[] { sone, sone.getRequestUri().setMetaString(new String[] { "sone.xml" }) });
			USKCallback uskCallback = new USKCallback() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void onFoundEdition(long edition, USK key, ObjectContainer objectContainer, ClientContext clientContext, boolean metadata, short codec, byte[] data, boolean newKnownGood, boolean newSlotToo) {
					logger.log(Level.FINE, "Found USK update for Sone “%s” at %s, new known good: %s, new slot too: %s.", new Object[] { sone, key, newKnownGood, newSlotToo });
					if (newKnownGood) {
						sone.updateUris(key.getURI());
						soneDownloader.fetchSone(sone);
					}
				}

				@Override
				public short getPollingPriorityProgress() {
					return RequestStarter.INTERACTIVE_PRIORITY_CLASS;
				}

				@Override
				public short getPollingPriorityNormal() {
					return RequestStarter.INTERACTIVE_PRIORITY_CLASS;
				}
			};
			soneUskCallbacks.put(sone.getId(), uskCallback);
			node.clientCore.uskManager.subscribe(USK.create(sone.getRequestUri()), uskCallback, true, (HighLevelSimpleClientImpl) client);
		} catch (MalformedURLException mue1) {
			logger.log(Level.WARNING, "Could not subscribe USK “" + sone.getRequestUri() + "”!", mue1);
		}
	}

	/**
	 * Unsubscribes the request URI of the given Sone.
	 *
	 * @param sone
	 *            The Sone to unregister
	 */
	public void unregisterUsk(Sone sone) {
		USKCallback uskCallback = soneUskCallbacks.remove(sone.getId());
		if (uskCallback == null) {
			return;
		}
		try {
			logger.log(Level.FINEST, "Unsubscribing from USK for %s…", new Object[] { sone });
			node.clientCore.uskManager.unsubscribe(USK.create(sone.getRequestUri()), uskCallback);
		} catch (MalformedURLException mue1) {
			logger.log(Level.FINE, "Could not unsubscribe USK “" + sone.getRequestUri() + "”!", mue1);
		}
	}

}
