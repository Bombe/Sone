/*
 * Sone - Client.java - Copyright © 2010–2013 David Roden
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

package net.pterodactylus.sone.data;

/**
 * Container for the client information of a {@link Sone}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Client {

	/** The name of the client. */
	private final String name;

	/** The version of the client. */
	private final String version;

	/**
	 * Creates a new client information container.
	 *
	 * @param name
	 *            The name of the client
	 * @param version
	 *            The version of the client
	 */
	public Client(String name, String version) {
		this.name = name;
		this.version = version;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the name of the client.
	 *
	 * @return The name of the client
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the version of the client.
	 *
	 * @return The version of the client
	 */
	public String getVersion() {
		return version;
	}

}
