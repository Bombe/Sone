/*
 * Sone - L10nFilter.java - Copyright © 2010–2012 David Roden
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

package net.pterodactylus.sone.freenet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.TemplateContext;

/**
 * {@link Filter} implementation replaces {@link String} values with their
 * translated equivalents.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class L10nFilter implements Filter {

	/** The web interface. */
	private final WebInterface webInterface;

	/**
	 * Creates a new L10n filter.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public L10nFilter(WebInterface webInterface) {
		this.webInterface = webInterface;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String format(TemplateContext templateContext, Object data, Map<String, String> parameters) {
		if (parameters.isEmpty()) {
			return webInterface.getL10n().getString(String.valueOf(data));
		}
		List<Object> parameterValues = new ArrayList<Object>();
		int parameterIndex = 0;
		while (parameters.containsKey(String.valueOf(parameterIndex))) {
			Object value = parameters.get(String.valueOf(parameterIndex));
			if (((String) value).startsWith("=")) {
				value = ((String) value).substring(1);
			} else {
				value = templateContext.get((String) value);
			}
			parameterValues.add(value);
			++parameterIndex;
		}
		return new MessageFormat(webInterface.getL10n().getString(String.valueOf(data)), new Locale(webInterface.getL10n().getSelectedLanguage().shortCode)).format(parameterValues.toArray());
	}
}
