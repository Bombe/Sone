/*
 * Sone - GetPagePlugin.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.web.page.Page.Request;
import net.pterodactylus.util.template.DataProvider;
import net.pterodactylus.util.template.Plugin;

/**
 * Extracts a page number from a {@link Request}’s parameters and stores it in a
 * {@link DataProvider}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetPagePlugin implements Plugin {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(DataProvider dataProvider, Map<String, String> parameters) {
		String requestKey = parameters.get("request");
		String parameter = parameters.get("parameter");
		String pageKey = parameters.get("key");

		if (requestKey == null) {
			requestKey = "request";
		}
		if (parameter == null) {
			parameter = "page";
		}
		if (pageKey == null) {
			pageKey = "page";
		}

		Request request = (Request) dataProvider.getData(requestKey);
		String pageString = request.getHttpRequest().getParam(parameter);
		int page = 0;
		try {
			page = Integer.parseInt(pageString);
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		dataProvider.setData(pageKey, page);
	}

}
