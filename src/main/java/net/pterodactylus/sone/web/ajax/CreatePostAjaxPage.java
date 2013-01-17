/*
 * Sone - CreatePostAjaxPage.java - Copyright © 2010–2013 David Roden
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
import net.pterodactylus.sone.text.TextFilter;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.json.JsonObject;

/**
 * AJAX handler that creates a new post.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreatePostAjaxPage extends JsonPage {

	/**
	 * Creates a new “create post” AJAX handler.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public CreatePostAjaxPage(WebInterface webInterface) {
		super("createPost.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(FreenetRequest request) {
		Sone sone = getCurrentSone(request.getToadletContext());
		if (sone == null) {
			return createErrorJsonObject("auth-required");
		}
		String recipientId = request.getHttpRequest().getParam("recipient");
		Sone recipient = webInterface.getCore().getSone(recipientId, false);
		String senderId = request.getHttpRequest().getParam("sender");
		Sone sender = webInterface.getCore().getLocalSone(senderId, false);
		if (sender == null) {
			sender = sone;
		}
		String text = request.getHttpRequest().getParam("text");
		if ((text == null) || (text.trim().length() == 0)) {
			return createErrorJsonObject("text-required");
		}
		text = TextFilter.filter(request.getHttpRequest().getHeader("host"), text);
		Post newPost = webInterface.getCore().createPost(sender, recipient, text);
		return createSuccessJsonObject().put("postId", newPost.getId()).put("sone", sender.getId()).put("recipient", (newPost.getRecipient() != null) ? newPost.getRecipient().getId() : null);
	}

}
