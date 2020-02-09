/*
 * Sone - RequestChangeFilter.java - Copyright © 2010–2020 David Roden
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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.TemplateContext;

/**
 * This filter expects a {@link FreenetRequest} as input and outputs a
 * {@link URI} that is modified by the parameters. The name of the parameter is
 * handed in as “name”, the new value is stored in “value”.
 */
public class RequestChangeFilter implements Filter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, Object> parameters) {
		FreenetRequest request = (FreenetRequest) data;
		String name = String.valueOf(parameters.get("name"));
		String value = String.valueOf(parameters.get("value"));

		Map<String, String> values = new HashMap<>();
		Collection<String> parameterNames = request.getHttpRequest().getParameterNames();
		for (String parameterName : parameterNames) {
			values.put(parameterName, request.getHttpRequest().getParam(parameterName));
		}
		values.put(name, value);

		StringBuilder query = new StringBuilder();
		try {
			for (Entry<String, String> parameterEntry : values.entrySet()) {
				query.append((query.length() == 0) ? '?' : '&');
				query.append(URLEncoder.encode(parameterEntry.getKey(), "UTF-8"));
				query.append('=');
				query.append(URLEncoder.encode(parameterEntry.getValue(), "UTF-8"));
			}
			String oldUri = request.getUri().toString();
			int questionMark = oldUri.indexOf('?');
			if (questionMark == -1) {
				questionMark = oldUri.length();
			}
			return new URI(oldUri.substring(0, questionMark) + query.toString());
		} catch (UnsupportedEncodingException uee1) {
			/* UTF-8 not supported? I don’t think so. */
		} catch (URISyntaxException use1) {
			use1.printStackTrace();
		}
		return null;
	}

}
