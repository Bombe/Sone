/*
 * FreenetSone - LogoutPage.java - Copyright © 2010 David Roden
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

import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import freenet.clients.http.ToadletContext;

/**
 * Logs a user out.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LogoutPage extends SoneTemplatePage {

	/**
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public LogoutPage(Template template, WebInterface webInterface) {
		super("logout.html", template, "Page.Logout.Title", webInterface, true);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, TemplateContext templateContext) throws RedirectException {
		setCurrentSone(request.getToadletContext(), null);
		super.processTemplate(request, templateContext);
		throw new RedirectException("index.html");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled(ToadletContext toadletContext) {
		return (getCurrentSone(toadletContext, false) != null) && (webInterface.getCore().getLocalSones().size() != 1);
	}

}
