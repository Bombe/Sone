/*
 * Sone - DeletePostPage.java - Copyright © 2010–2012 David Roden
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
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

/**
 * Lets the user delete a post they made.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DeletePostPage extends SoneTemplatePage {

	/**
	 * Creates a new “delete post” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public DeletePostPage(Template template, WebInterface webInterface) {
		super("deletePost.html", template, "Page.DeletePost.Title", webInterface, true);
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
		if (request.getMethod() == Method.GET) {
			String postId = request.getHttpRequest().getParam("post");
			String returnPage = request.getHttpRequest().getParam("returnPage");
			Post post = webInterface.getCore().getPost(postId);
			templateContext.set("post", post);
			templateContext.set("returnPage", returnPage);
			return;
		} else if (request.getMethod() == Method.POST) {
			String postId = request.getHttpRequest().getPartAsStringFailsafe("post", 36);
			String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
			Post post = webInterface.getCore().getPost(postId);
			if (!post.getSone().isLocal()) {
				throw new RedirectException("noPermission.html");
			}
			if (request.getHttpRequest().isPartSet("confirmDelete")) {
				webInterface.getCore().deletePost(post);
				throw new RedirectException(returnPage);
			} else if (request.getHttpRequest().isPartSet("abortDelete")) {
				throw new RedirectException(returnPage);
			}
			templateContext.set("post", post);
			templateContext.set("returnPage", returnPage);
		}
	}

}
