/*
 * Sone - SoneAccessor.java - Copyright © 2010–2016 David Roden
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

import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static java.util.logging.Logger.getLogger;
import static net.pterodactylus.sone.data.Album.FLATTENER;
import static net.pterodactylus.sone.data.Album.IMAGES;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.SoneStatus;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.freenet.wot.Trust;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.ajax.GetTimesAjaxPage;
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
	private static final Logger logger = getLogger(SoneAccessor.class.getName());

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
		} else if (member.equals("friend")) {
			Sone currentSone = (Sone) templateContext.get("currentSone");
			return (currentSone != null) && currentSone.hasFriend(sone.getId());
		} else if (member.equals("current")) {
			Sone currentSone = (Sone) templateContext.get("currentSone");
			return (currentSone != null) && currentSone.equals(sone);
		} else if (member.equals("modified")) {
			return core.isModifiedSone(sone);
		} else if (member.equals("status")) {
			return sone.getStatus();
		} else if (member.equals("unknown")) {
			return sone.getStatus() == SoneStatus.unknown;
		} else if (member.equals("idle")) {
			return sone.getStatus() == SoneStatus.idle;
		} else if (member.equals("inserting")) {
			return sone.getStatus() == SoneStatus.inserting;
		} else if (member.equals("downloading")) {
			return sone.getStatus() == SoneStatus.downloading;
		} else if (member.equals("new")) {
			return !sone.isKnown();
		} else if (member.equals("locked")) {
			return core.isLocked(sone);
		} else if (member.equals("lastUpdatedText")) {
			return GetTimesAjaxPage.getTime((WebInterface) templateContext.get("webInterface"), sone.getTime());
		} else if (member.equals("trust")) {
			Sone currentSone = (Sone) templateContext.get("currentSone");
			if (currentSone == null) {
				return null;
			}
			Trust trust = sone.getIdentity().getTrust((OwnIdentity) currentSone.getIdentity());
			logger.log(Level.FINEST, String.format("Trust for %s by %s: %s", sone, currentSone, trust));
			if (trust == null) {
				return new Trust(null, null, null);
			}
			return trust;
		} else if (member.equals("allImages")) {
			return from(asList(sone.getRootAlbum())).transformAndConcat(FLATTENER).transformAndConcat(IMAGES);
		} else if (member.equals("albums")) {
			return sone.getRootAlbum().getAlbums();
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
