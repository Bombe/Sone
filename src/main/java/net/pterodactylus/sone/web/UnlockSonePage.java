/*
 * Sone - UnlockSonePage.java - Copyright © 2010–2012 David Roden
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
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

/**
 * This page lets the user unlock a {@link Sone} to allow its insertion.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UnlockSonePage extends SoneTemplatePage {

	/**
	 * Creates a new “unlock Sone” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public UnlockSonePage(Template template, WebInterface webInterface) {
		super("unlockSone.html", template, "Page.UnlockSone.Title", webInterface);
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
		String soneId = request.getHttpRequest().getPartAsStringFailsafe("sone", 44);
		Sone sone = webInterface.getCore().getLocalSone(soneId, false);
		if (sone != null) {
			webInterface.getCore().unlockSone(sone);
		}
		String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
		throw new RedirectException(returnPage);
	}

}
