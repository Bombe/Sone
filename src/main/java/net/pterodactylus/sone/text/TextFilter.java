/*
 * Sone - TextFilter.java - Copyright © 2011 David Roden
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

package net.pterodactylus.sone.text;

import java.util.logging.Logger;

import net.pterodactylus.util.logging.Logging;

/**
 * Filter for newly inserted text. This filter strips HTTP links to the local
 * node of identifying marks, e.g. a link to “http://localhost:8888/KSK@gpl.txt”
 * will be converted to “KSK@gpl.txt”. This will only work for links that point
 * to the same address Sone is accessed by, so if you access Sone using
 * localhost:8888, links to 127.0.0.1:8888 will not be removed.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TextFilter {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(TextFilter.class);

	/**
	 * Filters the given text, stripping the host header part for links to the
	 * local node.
	 *
	 * @param hostHeader
	 *            The host header from the request
	 * @param text
	 *            The text to filter
	 * @return The filtered text
	 */
	public static String filter(String hostHeader, String text) {

		/* filter http(s) links to own node. */
		if (hostHeader != null) {
			String line = text;
			for (String toRemove : new String[] { "http://" + hostHeader + "/", "https://" + hostHeader + "/", "http://" + hostHeader, "https://" + hostHeader }) {
				while (line.indexOf(toRemove) != -1) {
					line = line.replace(toRemove, "");
				}
			}
			return line;
		}

		/* not modified. */
		return text;
	}

}
