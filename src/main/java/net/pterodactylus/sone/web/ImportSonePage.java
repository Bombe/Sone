/*
 * Sone - ImportSonePage.java - Copyright © 2010 David Roden
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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.template.Template;
import freenet.support.api.Bucket;
import freenet.support.io.Closer;

/**
 * The “import Sone” page lets the user import a Sone from a previous backup.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ImportSonePage extends SoneTemplatePage {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(ImportSonePage.class);

	/**
	 * Creates a new “import Sone” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public ImportSonePage(Template template, WebInterface webInterface) {
		super("importSone.html", template, "Page.ImportSone.Title", webInterface, false);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(net.pterodactylus.sone.web.page.Page.Request request, Template template) throws RedirectException {
		super.processTemplate(request, template);
		template.set("errorParsingSone", false);
		if (request.getMethod() == Method.POST) {
			Bucket soneBucket = request.getHttpRequest().getPart("sone-file");
			InputStream soneInputStream = null;
			try {
				soneInputStream = soneBucket.getInputStream();
				Sone sone = webInterface.core().loadSone(soneInputStream);
				if (sone != null) {
					throw new RedirectException("viewSone.html?sone=" + sone.getId());
				}
			} catch (IOException ioe1) {
				logger.log(Level.INFO, "Could not load sone from posted XML file.", ioe1);
			} finally {
				Closer.close(soneInputStream);
				soneBucket.free();
			}
			template.set("errorParsingSone", true);
		}
	}

}
