/*
 * Sone - CreateReplyPage.java - Copyright © 2010–2013 David Roden
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
import net.pterodactylus.sone.text.TextFilter;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

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
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		String postId = request.getHttpRequest().getPartAsStringFailsafe("post", 36);
		String text = request.getHttpRequest().getPartAsStringFailsafe("text", 65536).trim();
		String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
		if (request.getMethod() == Method.POST) {
			Post post = webInterface.getCore().getPost(postId);
			if (text.length() > 0) {
				String senderId = request.getHttpRequest().getPartAsStringFailsafe("sender", 43);
				Sone sender = webInterface.getCore().getLocalSone(senderId, false);
				if (sender == null) {
					sender = getCurrentSone(request.getToadletContext());
				}
				text = TextFilter.filter(request.getHttpRequest().getHeader("host"), text);
				webInterface.getCore().createReply(sender, post, text);
				throw new RedirectException(returnPage);
			}
			templateContext.set("errorTextEmpty", true);
		}
		templateContext.set("postId", postId);
		templateContext.set("text", text);
		templateContext.set("returnPage", returnPage);
	}

}
