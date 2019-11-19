/*
 * Sone - UnknownDateFilter.java - Copyright © 2011–2019 David Roden
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

import net.pterodactylus.sone.freenet.Translation;
import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.TemplateContext;

/**
 * {@link Filter} implementation that replaces a {@link Long} with a value of
 * {@code 0} by a {@link String} from a {@link Translation translation}.
 */
public class UnknownDateFilter implements Filter {

	/** The translation. */
	private final Translation translation;

	/** The key for the text to show. */
	private final String unknownKey;

	/**
	 * Creates a new unknown date filter.
	 *
	 * @param translation The translation
	 * @param unknownKey  The key of the text to show
	 */
	public UnknownDateFilter(Translation translation, String unknownKey) {
		this.translation = translation;
		this.unknownKey = unknownKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, Object> parameters) {
		if (data instanceof Long) {
			if ((Long) data == 0) {
				return translation.translate(unknownKey);
			}
		}
		return data;
	}

}
