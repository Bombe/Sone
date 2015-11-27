/*
 * Sone - GetLikesAjaxPage.java - Copyright © 2010–2015 David Roden
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

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static net.pterodactylus.sone.data.Sone.NICE_NAME_COMPARATOR;

import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

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
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
		String type = request.getHttpRequest().getParam("type", null);
		String id = request.getHttpRequest().getParam(type, null);
		if ((id == null) || (id.length() == 0)) {
			return createErrorJsonObject("invalid-" + type + "-id");
		}
		if ("post".equals(type)) {
			Optional<Post> post = webInterface.getCore().getPost(id);
			if (!post.isPresent()) {
				return createErrorJsonObject("invalid-post-id");
			}
			Set<Sone> sones = webInterface.getCore().getLikes(post.get());
			return createSuccessJsonObject().put("likes", sones.size()).put("sones", getSones(sones));
		} else if ("reply".equals(type)) {
			Optional<PostReply> reply = webInterface.getCore().getPostReply(id);
			if (!reply.isPresent()) {
				return createErrorJsonObject("invalid-reply-id");
			}
			Set<Sone> sones = webInterface.getCore().getLikes(reply.get());
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
	private static JsonNode getSones(Set<Sone> sones) {
		ArrayNode soneArray = new ArrayNode(instance);
		for (Sone sone : FluentIterable.from(sones).toSortedList(NICE_NAME_COMPARATOR)) {
			soneArray.add(new ObjectNode(instance).put("id", sone.getId()).put("name", SoneAccessor.getNiceName(sone)));
		}
		return soneArray;
	}

}
