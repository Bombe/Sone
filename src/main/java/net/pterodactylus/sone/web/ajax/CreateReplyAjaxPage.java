/*
 * Sone - CreateReplyAjaxPage.java - Copyright © 2010 David Roden
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
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.text.TextFilter;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.json.JsonObject;

/**
 * This AJAX page create a reply.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreateReplyAjaxPage extends JsonPage {

	/**
	 * Creates a new “create reply” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public CreateReplyAjaxPage(WebInterface webInterface) {
		super("createReply.ajax", webInterface);
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
		String text = request.getHttpRequest().getParam("text").trim();
		String senderId = request.getHttpRequest().getParam("sender");
		Sone sender = webInterface.getCore().getLocalSone(senderId, false);
		if (sender == null) {
			sender = getCurrentSone(request.getToadletContext());
		}
		Post post = webInterface.getCore().getPost(postId);
		if ((post == null) || (post.getSone() == null)) {
			return createErrorJsonObject("invalid-post-id");
		}
		text = TextFilter.filter(request.getHttpRequest().getHeader("host"), text);
		PostReply reply = webInterface.getCore().createReply(sender, post, text);
		return createSuccessJsonObject().put("reply", reply.getId()).put("sone", sender.getId());
	}

}
