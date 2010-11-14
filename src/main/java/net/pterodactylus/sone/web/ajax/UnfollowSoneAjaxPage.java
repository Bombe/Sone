/*
 * Sone - UnfollowSoneAjaxPage.java - Copyright © 2010 David Roden
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
 * AJAX page that lets a Sone unfollow another Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UnfollowSoneAjaxPage extends JsonPage {

	/**
	 * Creates a new “unfollow Sone” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public UnfollowSoneAjaxPage(WebInterface webInterface) {
		super("ajax/unfollowSone.ajax", webInterface);
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
		currentSone.removeFriend(soneId);
		webInterface.getCore().saveSone(currentSone);
		return createSuccessJsonObject();
	}

}
