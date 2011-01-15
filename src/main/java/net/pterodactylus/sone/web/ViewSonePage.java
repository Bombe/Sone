/*
 * Sone - ViewSonePage.java - Copyright © 2010 David Roden
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

import java.util.List;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.template.DataProvider;
import net.pterodactylus.util.template.Template;

/**
 * Lets the user browser another Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ViewSonePage extends SoneTemplatePage {

	/**
	 * Creates a new “view Sone” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public ViewSonePage(Template template, WebInterface webInterface) {
		super("viewSone.html", template, "Page.ViewSone.Title", webInterface, false);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, DataProvider dataProvider) throws RedirectException {
		super.processTemplate(request, dataProvider);
		String soneId = request.getHttpRequest().getParam("sone");
		Sone sone = webInterface.getCore().getSone(soneId, false);
		dataProvider.set("sone", sone);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void postProcess(Request request, DataProvider dataProvider) {
		Sone sone = (Sone) dataProvider.get("sone");
		if (sone == null) {
			return;
		}
		List<Post> posts = sone.getPosts();
		for (Post post : posts) {
			webInterface.getCore().markPostKnown(post);
			for (Reply reply : webInterface.getCore().getReplies(post)) {
				webInterface.getCore().markReplyKnown(reply);
			}
		}
	}

}
