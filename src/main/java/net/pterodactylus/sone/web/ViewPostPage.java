/*
 * Sone - ViewPostPage.java - Copyright © 2010 David Roden
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
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

/**
 * This page lets the user view a post and all its replies.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ViewPostPage extends SoneTemplatePage {

	/**
	 * Creates a new “view post” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public ViewPostPage(Template template, WebInterface webInterface) {
		super("viewPost.html", template, "Page.ViewPost.Title", webInterface, false);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		String postId = request.getHttpRequest().getParam("post");
		Post post = webInterface.getCore().getPost(postId);
		templateContext.set("post", post);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void postProcess(Request request, TemplateContext templateContext) {
		Post post = (Post) templateContext.get("post");
		if (post == null) {
			return;
		}
		webInterface.getCore().markPostKnown(post);
		for (Reply reply : webInterface.getCore().getReplies(post)) {
			webInterface.getCore().markReplyKnown(reply);
		}
	}

}
