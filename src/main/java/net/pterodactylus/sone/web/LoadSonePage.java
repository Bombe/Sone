/*
 * Sone - LoadSonePage.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.template.Template;

/**
 * This page lets the user a new Sone that has been previously managed on a
 * different node or installation. The data of the Sone is loaded from Freenet.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LoadSonePage extends SoneTemplatePage {

	/**
	 * Creates a new “load Sone” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public LoadSonePage(Template template, WebInterface webInterface) {
		super("loadSone.html", template, "Page.LoadSone.Title", webInterface);
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
		if (request.getMethod() == Method.POST) {
			String requestUri = request.getHttpRequest().getPartAsStringFailsafe("request-uri", 256);
			String insertUri = request.getHttpRequest().getPartAsStringFailsafe("insert-uri", 256);
			String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 64);
			webInterface.core().loadSone(requestUri, insertUri);
			throw new RedirectException(returnPage);
		}
	}

}
