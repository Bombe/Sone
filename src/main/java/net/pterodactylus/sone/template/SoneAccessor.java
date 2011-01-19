/*
 * Sone - SoneAccessor.java - Copyright © 2010 David Roden
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

import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.core.Core.SoneStatus;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.Trust;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.template.Accessor;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.TemplateContext;

/**
 * {@link Accessor} for {@link Sone}s that adds a couple of properties to Sones.
 * <dl>
 * <dt>niceName</dt>
 * <dd>Will show a combination of first name, middle name, and last name, if
 * available, otherwise the username of the Sone is returned.</dd>
 * <dt>friend</dt>
 * <dd>Will return {@code true} if the sone in question is a friend of the
 * currently logged in Sone (as determined by accessing the “currentSone”
 * variable of the given {@link TemplateContext}).</dd>
 * <dt>current</dt>
 * <dd>Will return {@code true} if the sone in question is the currently logged
 * in Sone.</dd>
 * </dl>
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneAccessor extends ReflectionAccessor {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(SoneAccessor.class);

	/** The core. */
	private final Core core;

	/**
	 * Creates a new Sone accessor.
	 *
	 * @param core
	 *            The Sone core
	 */
	public SoneAccessor(Core core) {
		this.core = core;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(TemplateContext templateContext, Object object, String member) {
		Sone sone = (Sone) object;
		if (member.equals("niceName")) {
			return getNiceName(sone);
		} else if (member.equals("local")) {
			return core.isLocalSone(sone);
		} else if (member.equals("friend")) {
			Sone currentSone = (Sone) templateContext.get("currentSone");
			return (currentSone != null) && currentSone.hasFriend(sone.getId());
		} else if (member.equals("current")) {
			Sone currentSone = (Sone) templateContext.get("currentSone");
			return (currentSone != null) && currentSone.equals(sone);
		} else if (member.equals("modified")) {
			return core.isModifiedSone(sone);
		} else if (member.equals("status")) {
			return core.getSoneStatus(sone);
		} else if (member.equals("unknown")) {
			return core.getSoneStatus(sone) == SoneStatus.unknown;
		} else if (member.equals("idle")) {
			return core.getSoneStatus(sone) == SoneStatus.idle;
		} else if (member.equals("inserting")) {
			return core.getSoneStatus(sone) == SoneStatus.inserting;
		} else if (member.equals("downloading")) {
			return core.getSoneStatus(sone) == SoneStatus.downloading;
		} else if (member.equals("new")) {
			return core.isNewSone(sone.getId(), false);
		} else if (member.equals("locked")) {
			return core.isLocked(sone);
		} else if (member.equals("trust")) {
			Sone currentSone = (Sone) templateContext.get("currentSone");
			if (currentSone == null) {
				return null;
			}
			Trust trust = core.getTrust(currentSone, sone);
			logger.log(Level.FINEST, "Trust for %s by %s: %s", new Object[] { sone, currentSone, trust });
			if (trust == null) {
				return new Trust(null, null, null);
			}
			return trust;
		}
		return super.get(templateContext, object, member);
	}

	//
	// STATIC METHODS
	//

	/**
	 * Returns the nice name of the given Sone.
	 *
	 * @param sone
	 *            The Sone to get the nice name for
	 * @return The nice name of the Sone
	 */
	public static String getNiceName(Sone sone) {
		Profile profile = sone.getProfile();
		String firstName = profile.getFirstName();
		String middleName = profile.getMiddleName();
		String lastName = profile.getLastName();
		if (firstName == null) {
			if (lastName == null) {
				return String.valueOf(sone.getName());
			}
			return lastName;
		}
		return firstName + ((middleName != null) ? " " + middleName : "") + ((lastName != null) ? " " + lastName : "");
	}

}
