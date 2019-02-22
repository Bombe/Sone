/*
 * Sone - CollectionAccessor.java - Copyright © 2010–2016 David Roden
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.template.Accessor;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.TemplateContext;

/**
 * {@link Accessor} for {@link Collection}s that adds a couple of specialized
 * properties that only work for collections that contain the right types.
 * <dl>
 * <dd>soneNames</dd>
 * <dt>Returns the nice names of all {@link Sone}s in the collection, sorted
 * ascending by their nice names.</dt>
 * </dl>
 */
public class CollectionAccessor extends ReflectionAccessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(TemplateContext templateContext, Object object, String member) {
		Collection<?> collection = (Collection<?>) object;
		if (member.equals("soneNames")) {
			List<Sone> sones = new ArrayList<>();
			for (Object sone : collection) {
				if (!(sone instanceof Sone)) {
					continue;
				}
				sones.add((Sone) sone);
			}
			Collections.sort(sones, Sone.NICE_NAME_COMPARATOR);
			StringBuilder soneNames = new StringBuilder();
			for (Sone sone : sones) {
				if (soneNames.length() > 0) {
					soneNames.append(", ");
				}
				soneNames.append(SoneAccessor.getNiceName(sone));
			}
			return soneNames.toString();
		}
		return super.get(templateContext, object, member);
	}

}
