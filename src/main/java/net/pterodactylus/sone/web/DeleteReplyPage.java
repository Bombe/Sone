/*
 * Sone - DeleteReplyPage.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.template.DataProvider;
import net.pterodactylus.util.template.Template;

/**
 * This page lets the user delete a reply.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DeleteReplyPage extends SoneTemplatePage {

	/**
	 * Creates a new “delete reply” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public DeleteReplyPage(Template template, WebInterface webInterface) {
		super("deleteReply.html", template, "Page.DeleteReply.Title", webInterface, true);
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
		String replyId = request.getHttpRequest().getPartAsStringFailsafe("reply", 36);
		Reply reply = webInterface.getCore().getReply(replyId);
		String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
		if (request.getMethod() == Method.POST) {
			if (!webInterface.getCore().isLocalSone(reply.getSone())) {
				throw new RedirectException("noPermission.html");
			}
			if (request.getHttpRequest().isPartSet("confirmDelete")) {
				webInterface.getCore().deleteReply(reply);
				throw new RedirectException(returnPage);
			} else if (request.getHttpRequest().isPartSet("abortDelete")) {
				throw new RedirectException(returnPage);
			}
		}
		dataProvider.set("reply", reply);
		dataProvider.set("returnPage", returnPage);
	}

}
