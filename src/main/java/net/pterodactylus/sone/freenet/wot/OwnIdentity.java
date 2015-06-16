/*
 * Sone - OwnIdentity.java - Copyright © 2010–2013 David Roden
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

package net.pterodactylus.sone.freenet.wot;


/**
 * Defines a local identity, an own identity.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface OwnIdentity extends Identity {

	/**
	 * Returns the insert URI of the identity.
	 *
	 * @return The insert URI of the identity
	 */
	public String getInsertUri();

	public OwnIdentity addContext(String context);
	public OwnIdentity removeContext(String context);
	public OwnIdentity setProperty(String name, String value);
	public OwnIdentity removeProperty(String name);

}
