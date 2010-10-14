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

import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.template.Accessor;
import net.pterodactylus.util.template.DataProvider;
import net.pterodactylus.util.template.ReflectionAccessor;

/**
 * {@link Accessor} for {@link Sone}s that adds a couple of properties to Sones.
 * <dl>
 * <dt>niceName</dt>
 * <dd>Will show a combination of first name, middle name, and last name, if
 * available, otherwise the username of the Sone is returned.</dd>
 * </dl>
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneAccessor extends ReflectionAccessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(DataProvider dataProvider, Object object, String member) {
		Sone sone = (Sone) object;
		if (member.equals("niceName")) {
			Profile profile = sone.getProfile();
			String firstName = profile.getFirstName();
			String middleName = profile.getMiddleName();
			String lastName = profile.getLastName();
			if (firstName == null) {
				if (lastName == null) {
					return sone.getName();
				}
				return lastName;
			}
			return firstName + ((middleName != null) ? " " + middleName : "") + ((lastName != null) ? " " + lastName : "");
		}
		return super.get(dataProvider, object, member);
	}

}
