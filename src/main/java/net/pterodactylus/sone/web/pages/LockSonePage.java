/*
 * Sone - LockSonePage.java - Copyright © 2010–2016 David Roden
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

package net.pterodactylus.sone.web.pages;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

/**
 * This page lets the user lock a {@link Sone} to prevent it from being
 * inserted.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LockSonePage extends SoneTemplatePage {

	/**
	 * Creates a new “lock Sone” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public LockSonePage(Template template, WebInterface webInterface) {
		super("lockSone.html", template, "Page.LockSone.Title", webInterface);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void handleRequest(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		String soneId = request.getHttpRequest().getPartAsStringFailsafe("sone", 44);
		Sone sone = webInterface.getCore().getLocalSone(soneId);
		if (sone != null) {
			webInterface.getCore().lockSone(sone);
		}
		String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
		throw new RedirectException(returnPage);
	}

}
