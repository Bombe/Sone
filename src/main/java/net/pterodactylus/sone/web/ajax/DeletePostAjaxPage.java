/*
 * Sone - DeletePostAjaxPage.java - Copyright © 2010–2015 David Roden
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

import com.google.common.base.Optional;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;

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
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
		String postId = request.getHttpRequest().getParam("post");
		Optional<Post> post = webInterface.getCore().getPost(postId);
		if (!post.isPresent()) {
			return createErrorJsonObject("invalid-post-id");
		}
		if (!post.get().getSone().isLocal()) {
			return createErrorJsonObject("not-authorized");
		}
		webInterface.getCore().deletePost(post.get());
		return createSuccessJsonObject();
	}

}
