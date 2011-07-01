/*
 * Sone - UnfollowSonePage.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

/**
 * This page lets the user unfollow another Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UnfollowSonePage extends SoneTemplatePage {

	/**
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public UnfollowSonePage(Template template, WebInterface webInterface) {
		super("unfollowSone.html", template, "Page.UnfollowSone.Title", webInterface, true);
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
		if (request.getMethod() == Method.POST) {
			String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
			Sone currentSone = getCurrentSone(request.getToadletContext());
			String soneIds = request.getHttpRequest().getPartAsStringFailsafe("sone", 2000);
			for (String soneId : soneIds.split("[ ,]+")) {
				currentSone.removeFriend(soneId);
			}
			webInterface.getCore().saveSone(currentSone);
			throw new RedirectException(returnPage);
		}
	}

}
