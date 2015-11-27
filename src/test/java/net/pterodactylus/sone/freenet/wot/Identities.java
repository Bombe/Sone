/*
 * Sone - Identities.java - Copyright © 2013–2015 David Roden
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

import java.util.Collection;
import java.util.Map;

/**
 * Creates {@link Identity}s and {@link OwnIdentity}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Identities {

	public static OwnIdentity createOwnIdentity(String id, Collection<String> contexts, Map<String, String> properties) {
		DefaultOwnIdentity ownIdentity = new DefaultOwnIdentity(id, "Nickname" + id, "Request" + id, "Insert" + id);
		setContextsAndPropertiesOnIdentity(ownIdentity, contexts, properties);
		return ownIdentity;
	}

	public static Identity createIdentity(String id, Collection<String> contexts, Map<String, String> properties) {
		DefaultIdentity identity = new DefaultIdentity(id, "Nickname" + id, "Request" + id);
		setContextsAndPropertiesOnIdentity(identity, contexts, properties);
		return identity;
	}

	private static void setContextsAndPropertiesOnIdentity(Identity identity, Collection<String> contexts, Map<String, String> properties) {
		identity.setContexts(contexts);
		identity.setProperties(properties);
	}

}
