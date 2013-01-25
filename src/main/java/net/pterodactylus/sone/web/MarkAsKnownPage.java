/*
 * Sone - MarkAsKnownPage.java - Copyright © 2011–2013 David Roden
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

import java.util.StringTokenizer;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

import com.google.common.base.Optional;

/**
 * Page that lets the user mark a number of {@link Sone}s, {@link Post}s, or
 * {@link Reply Replie}s as known.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MarkAsKnownPage extends SoneTemplatePage {

	/**
	 * Creates a new “mark as known” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public MarkAsKnownPage(Template template, WebInterface webInterface) {
		super("markAsKnown.html", template, "Page.MarkAsKnown.Title", webInterface);
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
		String type = request.getHttpRequest().getPartAsStringFailsafe("type", 5);
		if (!type.equals("sone") && !type.equals("post") && !type.equals("reply")) {
			throw new RedirectException("invalid.html");
		}
		String ids = request.getHttpRequest().getPartAsStringFailsafe("id", 65536);
		for (StringTokenizer idTokenizer = new StringTokenizer(ids); idTokenizer.hasMoreTokens();) {
			String id = idTokenizer.nextToken();
			if (type.equals("post")) {
				Optional<Post> post = webInterface.getCore().getPost(id);
				if (!post.isPresent()) {
					continue;
				}
				webInterface.getCore().markPostKnown(post.get());
			} else if (type.equals("reply")) {
				Optional<PostReply> reply = webInterface.getCore().getPostReply(id);
				if (!reply.isPresent()) {
					continue;
				}
				webInterface.getCore().markReplyKnown(reply.get());
			} else if (type.equals("sone")) {
				Sone sone = webInterface.getCore().getSone(id, false);
				if (sone == null) {
					continue;
				}
				webInterface.getCore().markSoneKnown(sone);
			}
		}
		String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
		throw new RedirectException(returnPage);
	}

}
