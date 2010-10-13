/*
 * FreenetSone - CreateSonePage.java - Copyright © 2010 David Roden
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

import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.SoneException;
import net.pterodactylus.sone.core.SoneException.Type;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.template.Template;
import freenet.clients.http.ToadletContext;

/**
 * The “create Sone” page lets the user create a new Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreateSonePage extends SoneTemplatePage {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(CreateSonePage.class);

	/**
	 * Creates a new “create Sone” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public CreateSonePage(Template template, WebInterface webInterface) {
		super("createSone.html", template, "Page.CreateSone.Title", webInterface);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, Template template) throws RedirectException {
		String name = "";
		String requestUri = null;
		String insertUri = null;
		if (request.getMethod() == Method.POST) {
			name = request.getHttpRequest().getPartAsStringFailsafe("name", 100);
			if (request.getHttpRequest().getParam("create-from-uri").length() > 0) {
				requestUri = request.getHttpRequest().getPartAsStringFailsafe("request-uri", 256);
				insertUri = request.getHttpRequest().getPartAsStringFailsafe("insert-uri", 256);
			}
			try {
				/* create Sone. */
				Sone sone = webInterface.core().createSone(name, requestUri, insertUri);

				/* log in the new Sone. */
				setCurrentSone(request.getToadletContext(), sone);
				throw new RedirectException("index.html");
			} catch (SoneException se1) {
				logger.log(Level.FINE, "Could not create Sone “%s” at (“%s”, “%s”), %s!", new Object[] { name, requestUri, insertUri, se1.getType() });
				if (se1.getType() == Type.INVALID_SONE_NAME) {
					template.set("errorName", true);
				} else if (se1.getType() == Type.INVALID_URI) {
					template.set("errorUri", true);
				}
			}
		}
		template.set("name", name);
		template.set("requestUri", requestUri);
		template.set("insertUri", insertUri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled(ToadletContext toadletContext) {
		return true;
	}

}
