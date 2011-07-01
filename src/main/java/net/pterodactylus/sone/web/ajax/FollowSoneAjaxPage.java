/*
 * Sone - FollowSoneAjaxPage.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.json.JsonObject;

/**
 * AJAX page that lets a Sone follow another Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FollowSoneAjaxPage extends JsonPage {

	/**
	 * Creates a new “follow Sone” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public FollowSoneAjaxPage(WebInterface webInterface) {
		super("followSone.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(Request request) {
		String soneId = request.getHttpRequest().getParam("sone");
		if (!webInterface.getCore().hasSone(soneId)) {
			return createErrorJsonObject("invalid-sone-id");
		}
		Sone currentSone = getCurrentSone(request.getToadletContext());
		if (currentSone == null) {
			return createErrorJsonObject("auth-required");
		}
		currentSone.addFriend(soneId);
		webInterface.getCore().touchConfiguration();
		return createSuccessJsonObject();
	}

}
