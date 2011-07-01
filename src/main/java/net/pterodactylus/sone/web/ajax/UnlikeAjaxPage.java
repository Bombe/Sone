/*
 * Sone - UnlikeAjaxPage.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.json.JsonObject;

/**
 * AJAX page that lets the user unlike a {@link Post}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UnlikeAjaxPage extends JsonPage {

	/**
	 * Creates a new “unlike post” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public UnlikeAjaxPage(WebInterface webInterface) {
		super("unlike.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(Request request) {
		String type = request.getHttpRequest().getParam("type", null);
		String id = request.getHttpRequest().getParam(type, null);
		if ((id == null) || (id.length() == 0)) {
			return createErrorJsonObject("invalid-" + type + "-id");
		}
		Sone currentSone = getCurrentSone(request.getToadletContext());
		if (currentSone == null) {
			return createErrorJsonObject("auth-required");
		}
		if ("post".equals(type)) {
			currentSone.removeLikedPostId(id);
			webInterface.getCore().touchConfiguration();
		} else if ("reply".equals(type)) {
			currentSone.removeLikedReplyId(id);
			webInterface.getCore().touchConfiguration();
		} else {
			return createErrorJsonObject("invalid-type");
		}
		return createSuccessJsonObject();
	}

}
