/*
 * Sone - TrustAccessor.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.freenet.wot.Trust;
import net.pterodactylus.util.template.Accessor;
import net.pterodactylus.util.template.DataProvider;
import net.pterodactylus.util.template.ReflectionAccessor;

/**
 * {@link Accessor} implementation for {@link Trust} values, adding the
 * following properties:
 * <dl>
 * <dt>assigned</dt>
 * <dd>{@link Boolean} that indicates whether this trust relationship has an
 * explicit value assigned to it.</dd>
 * </dl>
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TrustAccessor extends ReflectionAccessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(DataProvider dataProvider, Object object, String member) {
		Trust trust = (Trust) object;
		if ("assigned".equals(member)) {
			return trust.getExplicit() != null;
		} else if ("maximum".equals(member)) {
			return ((trust.getExplicit() != null) && (trust.getExplicit() >= 100)) || ((trust.getImplicit() != null) && (trust.getImplicit() >= 100));
		} else if ("hasDistance".equals(member)) {
			return (trust.getDistance() != null) && (trust.getDistance() != Integer.MAX_VALUE);
		}
		return super.get(dataProvider, object, member);
	}

}
