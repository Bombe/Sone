/*
 * Sone - LockSoneAjaxPage.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.json.JsonObject;

/**
 * Lets the user {@link Core#lockSone(Sone) lock} a {@link Sone}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LockSoneAjaxPage extends JsonPage {

	/**
	 * Creates a new “lock Sone” AJAX handler.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public LockSoneAjaxPage(WebInterface webInterface) {
		super("lockSone.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(FreenetRequest request) {
		String soneId = request.getHttpRequest().getParam("sone");
		Sone sone = webInterface.getCore().getLocalSone(soneId, false);
		if (sone == null) {
			return createErrorJsonObject("invalid-sone-id");
		}
		webInterface.getCore().lockSone(sone);
		return createSuccessJsonObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean requiresLogin() {
		return false;
	}

}
