/*
 * Sone - LikePage.java - Copyright © 2010–2016 David Roden
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
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

/**
 * Page that lets the user like a {@link Post}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LikePage extends SoneTemplatePage {

	/**
	 * Creates a new “like post” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public LikePage(Template template, WebInterface webInterface) {
		super("like.html", template, "Page.Like.Title", webInterface, true);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void handleRequest(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		if (request.getMethod() == Method.POST) {
			String type = request.getHttpRequest().getPartAsStringFailsafe("type", 16);
			String id = request.getHttpRequest().getPartAsStringFailsafe(type, 36);
			String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
			Sone currentSone = getCurrentSone(request.getToadletContext());
			if ("post".equals(type)) {
				currentSone.addLikedPostId(id);
			} else if ("reply".equals(type)) {
				currentSone.addLikedReplyId(id);
			}
			throw new RedirectException(returnPage);
		}
	}

}
