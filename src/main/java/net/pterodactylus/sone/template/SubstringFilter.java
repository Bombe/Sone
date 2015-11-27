/*
 * Sone - SubstringFilter.java - Copyright © 2010–2015 David Roden
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
 * {@link Filter} implementation that executes
 * {@link String#substring(int, int)} on the given data. It has two parameters:
 * “start” and “length.” “length” is optional and defaults to “the rest of the
 * string.” “start” starts at {@code 0} and can be negative to denote starting
 * at the end of the string.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SubstringFilter implements Filter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, Object> parameters) {
		String startString = String.valueOf(parameters.get("start"));
		String lengthString = String.valueOf(parameters.get("length"));
		int start = 0;
		try {
			start = Integer.parseInt(startString);
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		String dataString = String.valueOf(data);
		int dataLength = dataString.length();
		int length = Integer.MAX_VALUE;
		try {
			length = Integer.parseInt(lengthString);
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		if (start < 0) {
			return dataString.substring(dataLength + start, Math.min(dataLength, dataLength + start + length));
		}
		return dataString.substring(start, Math.min(dataLength, start + length));
	}

}
