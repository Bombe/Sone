/*
 * Sone - GetLikesAjaxPage.java - Copyright © 2010–2012 David Roden
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.json.JsonArray;
import net.pterodactylus.util.json.JsonObject;

/**
 * AJAX page that retrieves the number of “likes” a {@link Post} has.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetLikesAjaxPage extends JsonPage {

	/**
	 * Creates a new “get post likes” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public GetLikesAjaxPage(WebInterface webInterface) {
		super("getLikes.ajax", webInterface);
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(FreenetRequest request) {
		String type = request.getHttpRequest().getParam("type", null);
		String id = request.getHttpRequest().getParam(type, null);
		if ((id == null) || (id.length() == 0)) {
			return createErrorJsonObject("invalid-" + type + "-id");
		}
		if ("post".equals(type)) {
			Post post = webInterface.getCore().getPost(id);
			Set<Sone> sones = webInterface.getCore().getLikes(post);
			return createSuccessJsonObject().put("likes", sones.size()).put("sones", getSones(sones));
		} else if ("reply".equals(type)) {
			PostReply reply = webInterface.getCore().getReply(id);
			Set<Sone> sones = webInterface.getCore().getLikes(reply);
			return createSuccessJsonObject().put("likes", sones.size()).put("sones", getSones(sones));
		}
		return createErrorJsonObject("invalid-type");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean needsFormPassword() {
		return false;
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates a JSON array (containing the IDs and the nice names) from the
	 * given Sones, after sorting them by name.
	 *
	 * @param sones
	 *            The Sones to convert to an array
	 * @return The Sones, sorted by name
	 */
	private JsonArray getSones(Set<Sone> sones) {
		JsonArray soneArray = new JsonArray();
		List<Sone> sortedSones = new ArrayList<Sone>(sones);
		Collections.sort(sortedSones, Sone.NICE_NAME_COMPARATOR);
		for (Sone sone : sortedSones) {
			soneArray.add(new JsonObject().put("id", sone.getId()).put("name", SoneAccessor.getNiceName(sone)));
		}
		return soneArray;
	}

}
