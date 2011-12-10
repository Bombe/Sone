/*
 * Sone - ProfileAccessor.java - Copyright © 2011 David Roden
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

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.ShowCustomAvatars;
import net.pterodactylus.sone.freenet.wot.Trust;
import net.pterodactylus.util.template.Accessor;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.TemplateContext;

/**
 * {@link Accessor} for {@link Profile} objects that overwrites the original
 * “avatar” member to include checks for whether the custom avatar should
 * actually be shown.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ProfileAccessor extends ReflectionAccessor {

	/** The core. */
	private final Core core;

	/**
	 * Creates a new profile accessor.
	 *
	 * @param core
	 *            The Sone core
	 */
	public ProfileAccessor(Core core) {
		this.core = core;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(TemplateContext templateContext, Object object, String member) {
		Profile profile = (Profile) object;
		if ("avatar".equals(member)) {
			Sone currentSone = (Sone) templateContext.get("currentSone");
			if (currentSone == null) {
				/* not logged in? don’t show custom avatars, then. */
				return null;
			}
			String avatarId = profile.getAvatar();
			if (avatarId != null) {
				if (core.getImage(avatarId, false) == null) {
					/* avatar ID but no matching image? show nothing. */
					return null;
				}
			}
			Sone remoteSone = profile.getSone();
			if (core.isLocalSone(remoteSone)) {
				/* always show your own avatars. */
				return avatarId;
			}
			ShowCustomAvatars showCustomAvatars = currentSone.getOptions().<ShowCustomAvatars> getEnumOption("ShowCustomAvatars").get();
			if (showCustomAvatars == ShowCustomAvatars.NEVER) {
				return null;
			}
			if ((showCustomAvatars == ShowCustomAvatars.ALWAYS) || (avatarId == null)) {
				return avatarId;
			}
			if ((showCustomAvatars == ShowCustomAvatars.FOLLOWED) && currentSone.hasFriend(remoteSone.getId())) {
				return avatarId;
			}
			Trust trust = core.getTrust(currentSone, remoteSone);
			if (trust == null) {
				return null;
			}
			if ((showCustomAvatars == ShowCustomAvatars.MANUALLY_TRUSTED) && (trust.getExplicit() != null) && (trust.getExplicit() > 0)) {
				return avatarId;
			}
			if ((showCustomAvatars == ShowCustomAvatars.TRUSTED) && (((trust.getExplicit() != null) && (trust.getExplicit() > 0)) || ((trust.getImplicit() != null) && (trust.getImplicit() > 0)))) {
				return avatarId;
			}
			return null;
		}
		return super.get(templateContext, object, member);
	}

}
