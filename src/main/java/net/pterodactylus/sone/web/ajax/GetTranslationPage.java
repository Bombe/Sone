/*
 * Sone - GetTranslationPage.java - Copyright © 2010–2012 David Roden
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

package net.pterodactylus.sone.web.ajax;

import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.json.JsonObject;

/**
 * Returns the translation for a given key as JSON object.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetTranslationPage extends JsonPage {

	/**
	 * Creates a new translation page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public GetTranslationPage(WebInterface webInterface) {
		super("getTranslation.ajax", webInterface);
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(FreenetRequest request) {
		String key = request.getHttpRequest().getParam("key");
		String translation = webInterface.getL10n().getString(key);
		return createSuccessJsonObject().put("value", translation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean needsFormPassword() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean requiresLogin() {
		return false;
	}

}
