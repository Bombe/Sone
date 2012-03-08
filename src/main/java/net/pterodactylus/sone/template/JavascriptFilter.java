/*
 * Sone - JavascriptFilter.java - Copyright © 2011–2012 David Roden
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

import net.pterodactylus.util.number.Hex;
import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.TemplateContext;

/**
 * Escapes double quotes, backslashes, carriage returns and line feeds, and
 * additionally encloses a given string with double quotes to make it possible
 * to use a string in Javascript.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class JavascriptFilter implements Filter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, String> parameters) {
		StringBuilder javascriptString = new StringBuilder();
		javascriptString.append('"');
		for (char c : String.valueOf(data).toCharArray()) {
			if (c == '\r') {
				javascriptString.append("\\r");
				continue;
			}
			if (c == '\n') {
				javascriptString.append("\\n");
				continue;
			}
			if (c == '\t') {
				javascriptString.append("\\t");
				continue;
			}
			if ((c == '"') || (c == '\\')) {
				javascriptString.append('\\');
				javascriptString.append(c);
			} else if (c < 32) {
				javascriptString.append("\\x").append(Hex.toHex((byte) c));
			} else {
				javascriptString.append(c);
			}
		}
		javascriptString.append('"');
		return javascriptString.toString();
	}

}
