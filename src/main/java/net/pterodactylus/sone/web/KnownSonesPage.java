/*
 * Sone - KnownSonesPage.java - Copyright © 2010 David Roden
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.template.DataProvider;
import net.pterodactylus.util.template.Template;

/**
 * This page shows all known Sones.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class KnownSonesPage extends SoneTemplatePage {

	/**
	 * Creates a “known Sones” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public KnownSonesPage(Template template, WebInterface webInterface) {
		super("knownSones.html", template, "Page.KnownSones.Title", webInterface, false);
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
		List<Sone> knownSones = new ArrayList<Sone>(webInterface.getCore().getSones());
		Collections.sort(knownSones, Sone.NICE_NAME_COMPARATOR);
		dataProvider.set("knownSones", knownSones);
	}

}
