/*
 * Sone - SoneUri.java - Copyright © 2013–2015 David Roden
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

import static java.util.logging.Logger.getLogger;

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import freenet.keys.FreenetURI;

/**
 * Helper class that creates {@link FreenetURI}s for Sone to insert to and
 * request from.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneUri {

	/** The logger. */
	private static final Logger logger = getLogger(SoneUri.class.getName());

	/**
	 * Generate a Sone URI from the given URI.
	 *
	 * @param uri
	 *            The URI to derive the Sone URI from
	 * @return The derived URI
	 */
	public static FreenetURI create(String uri) {
		try {
			return new FreenetURI(uri).setDocName("Sone").setMetaString(new String[0]);
		} catch (MalformedURLException mue1) {
			/* this should never happen. */
			logger.log(Level.WARNING, String.format("Could not create Sone URI from URI: %s", uri), mue1);
			return null;
		}
	}

}
