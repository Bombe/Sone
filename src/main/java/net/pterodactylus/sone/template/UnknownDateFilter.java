/*
 * Sone - UnknownDateFilter.java - Copyright © 2011–2013 David Roden
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
import freenet.l10n.BaseL10n;

/**
 * {@link Filter} implementation that replaces a {@link Long} with a value of
 * {@code 0} by a {@link String} from an {@link BaseL10n l10n handler}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UnknownDateFilter implements Filter {

	/** The l10n handler. */
	private BaseL10n l10nHandler;

	/** The key for the text to show. */
	private final String unknownKey;

	/**
	 * Creates a new unknown date filter.
	 *
	 * @param l10nHandler
	 *            The l10n handler
	 * @param unknownKey
	 *            The key of the text to show
	 */
	public UnknownDateFilter(BaseL10n l10nHandler, String unknownKey) {
		this.l10nHandler = l10nHandler;
		this.unknownKey = unknownKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, Object> parameters) {
		if (data instanceof Long) {
			if ((Long) data == 0) {
				return l10nHandler.getString(unknownKey);
			}
		}
		return data;
	}

}
