/*
 * Sone - UniqueElementFilter.java - Copyright © 2011–2012 David Roden
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.TemplateContext;

/**
 * Filter that reduces a collection to a {@link Set}, removing duplicates.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UniqueElementFilter implements Filter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, Object> parameters) {
		if (!(data instanceof Collection<?>)) {
			return data;
		}
		return new HashSet<Object>((Collection<?>) data);
	}

}
