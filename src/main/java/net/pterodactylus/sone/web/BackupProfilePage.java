/*
 * Sone - BackupProfilePage.java - Copyright © 2010 David Roden
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

import java.io.StringWriter;

import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.template.Template;

/**
 * This page lets the user store a backup file containing the settings of the
 * logged in Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class BackupProfilePage extends SoneTemplatePage {

	/**
	 * Creates a new “backup profile” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public BackupProfilePage(Template template, WebInterface webInterface) {
		super("backupProfile.html", template, "Page.BackupProfile.Title", webInterface);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response handleRequest(Request request) {
		StringWriter stringWriter = new StringWriter();
		template.set("currentSone", getCurrentSone(request.getToadletContext()));
		try {
			template.render(stringWriter);
		} finally {
			Closer.close(stringWriter);
		}

		Response response = new Response(200, "OK", "text/xml; charset=utf-8", stringWriter.toString());
		response.setHeader("Content-Disposition", "attachment; filename=Sone_" + getCurrentSone(request.getToadletContext()).getName() + ".xml");
		return response;
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
