/*
 * Sone - IdentityAccessor.java - Copyright © 2010–2012 David Roden
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

package net.pterodactylus.sone.template;

import java.util.Set;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.util.template.Accessor;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.TemplateContext;

/**
 * {@link Accessor} implementation that adds a “uniqueNickname” member to an
 * {@link Identity}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityAccessor extends ReflectionAccessor {

	/** The core. */
	private final Core core;

	/**
	 * Creates a new identity accessor.
	 *
	 * @param core
	 *            The core
	 */
	public IdentityAccessor(Core core) {
		this.core = core;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(TemplateContext templateContext, Object object, String member) {
		Identity identity = (Identity) object;
		if ("uniqueNickname".equals(member)) {
			int minLength = -1;
			boolean found = false;
			Set<OwnIdentity> ownIdentities = null;
			ownIdentities = core.getIdentityManager().getAllOwnIdentities();
			do {
				boolean unique = true;
				String abbreviatedWantedNickname = getAbbreviatedNickname(identity, ++minLength);
				for (Identity ownIdentity : ownIdentities) {
					if (ownIdentity.equals(identity)) {
						continue;
					}
					String abbreviatedNickname = getAbbreviatedNickname(ownIdentity, minLength);
					if (abbreviatedNickname.equals(abbreviatedWantedNickname)) {
						unique = false;
						break;
					}
				}
				found = unique;
			} while (!found && (minLength < 43));
			return getAbbreviatedNickname(identity, minLength);
		}
		return super.get(templateContext, object, member);
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Returns the nickname of the given identity, optionally appending the
	 * first characters of the ID to it.
	 *
	 * @param identity
	 *            The identity
	 * @param length
	 *            The number of characters from the beginning of the ID to
	 *            append to the nickname
	 * @return The nickname with optional ID appendage
	 */
	private String getAbbreviatedNickname(Identity identity, int length) {
		return identity.getNickname() + ((length > 0) ? "@" + identity.getId().substring(0, length) : "");
	}

}
