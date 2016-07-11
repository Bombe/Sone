/*
 * Sone - MarkAsKnownAjaxPage.java - Copyright © 2011–2016 David Roden
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
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;

import com.google.common.base.Optional;

/**
 * AJAX page that lets the user mark a number of {@link Sone}s, {@link Post}s,
 * or {@link Reply}s as known.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MarkAsKnownAjaxPage extends JsonPage {

	/**
	 * Creates a new “mark as known” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public MarkAsKnownAjaxPage(WebInterface webInterface) {
		super("markAsKnown.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
		String type = request.getHttpRequest().getParam("type");
		if (!type.equals("sone") && !type.equals("post") && !type.equals("reply")) {
			return createErrorJsonObject("invalid-type");
		}
		String[] ids = request.getHttpRequest().getParam("id").split(" ");
		Core core = webInterface.getCore();
		for (String id : ids) {
			if (type.equals("post")) {
				Optional<Post> post = core.getPost(id);
				if (!post.isPresent()) {
					continue;
				}
				core.markPostKnown(post.get());
			} else if (type.equals("reply")) {
				Optional<PostReply> reply = core.getPostReply(id);
				if (!reply.isPresent()) {
					continue;
				}
				core.markReplyKnown(reply.get());
			} else if (type.equals("sone")) {
				Optional<Sone> sone = core.getSone(id);
				if (!sone.isPresent()) {
					continue;
				}
				core.markSoneKnown(sone.get());
			}
		}
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
