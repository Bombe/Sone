/*
 * Sone - CreateReplyPage.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.web;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.template.Template;

/**
 * This page lets the user post a reply to a post.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreateReplyPage extends SoneTemplatePage {

	/**
	 * Creates a new “create reply” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public CreateReplyPage(Template template, WebInterface webInterface) {
		super("createReply.html", template, "Page.CreateReply.Title", webInterface, true);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, Template template) throws RedirectException {
		super.processTemplate(request, template);
		String postId = request.getHttpRequest().getPartAsStringFailsafe("post", 36);
		String text = request.getHttpRequest().getPartAsStringFailsafe("text", 65536).trim();
		String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 64);
		if (request.getMethod() == Method.POST) {
			Post post = webInterface.core().getPost(postId);
			if (text.length() > 0) {
				Sone currentSone = getCurrentSone(request.getToadletContext());
				webInterface.core().createReply(currentSone, post, text);
				throw new RedirectException(returnPage);
			}
			template.set("errorTextEmpty", true);
		}
		template.set("postId", postId);
		template.set("text", text);
		template.set("returnPage", returnPage);
	}

}
