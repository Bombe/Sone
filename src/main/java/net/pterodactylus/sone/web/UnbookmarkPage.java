/*
 * Sone - UnbookmarkPage.java - Copyright © 2011 David Roden
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

import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

/**
 * Page that lets the user unbookmark a post.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UnbookmarkPage extends SoneTemplatePage {

	/**
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public UnbookmarkPage(Template template, WebInterface webInterface) {
		super("unbookmark.html", template, "Page.Unbookmark.Title", webInterface);
	}

	//
	// SONETEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		if (request.getMethod() == Method.POST) {
			String id = request.getHttpRequest().getPartAsStringFailsafe("post", 36);
			String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
			webInterface.getCore().unbookmarkPost(id);
			throw new RedirectException(returnPage);
		}
		String id = request.getHttpRequest().getParam("post");
		if (id.equals("allNotLoaded")) {
			Set<Post> posts = webInterface.getCore().getBookmarkedPosts();
			for (Post post : posts) {
				if (post.getSone() == null) {
					webInterface.getCore().unbookmark(post);
				}
			}
			throw new RedirectException("bookmarks.html");
		}
	}

}
