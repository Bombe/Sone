/*
 * Sone - DeletePostAjaxPage.java - Copyright © 2010–2013 David Roden
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
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.json.JsonObject;

/**
 * This AJAX page deletes a post.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DeletePostAjaxPage extends JsonPage {

	/**
	 * Creates a new AJAX page that deletes a post.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public DeletePostAjaxPage(WebInterface webInterface) {
		super("deletePost.ajax", webInterface);
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(FreenetRequest request) {
		String postId = request.getHttpRequest().getParam("post");
		Post post = webInterface.getCore().getPost(postId);
		if ((post == null) || (post.getSone() == null)) {
			return createErrorJsonObject("invalid-post-id");
		}
		if (!post.getSone().isLocal()) {
			return createErrorJsonObject("not-authorized");
		}
		webInterface.getCore().deletePost(post);
		return createSuccessJsonObject();
	}

}
