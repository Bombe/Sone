/*
 * Sone - FollowSonePage.java - Copyright © 2010 David Roden
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
import net.pterodactylus.sone.data.SoneShell;
import net.pterodactylus.util.template.Template;

/**
 * This page lets the user follow another Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FollowSonePage extends SoneTemplatePage {

	/**
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public FollowSonePage(Template template, WebInterface webInterface) {
		super("followSone.html", template, "Page.FollowSone.Title", webInterface);
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
		String soneId = request.getHttpRequest().getParam("sone");
		Sone currentSone = getCurrentSone(request.getToadletContext());
		Sone sone = webInterface.core().getSone(soneId);
		if (!(sone instanceof SoneShell) && !sone.equals(currentSone)) {
			currentSone.addFriendSone(sone);
		}
		throw new RedirectException("viewSone.html?sone=" + soneId);
	}

	//
	// SONETEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean requiresLogin() {
		return true;
	}

}
