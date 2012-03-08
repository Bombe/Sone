/*
 * Sone - CssClassNameFilter.java - Copyright © 2010–2012 David Roden
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

import java.util.Map;

import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.TemplateContext;

/**
 * Converts the {@link String} {@link String#valueOf(Object) representation} of
 * an object to a valid CSS class name by converting all characters that are not
 * US-ASCII letters or numbers to an underscore.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CssClassNameFilter implements Filter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, String> parameters) {
		return String.valueOf(data).replaceAll("[^a-zA-Z0-9-]", "_");
	}

}
