/*
 * Sone - RescuePage.java - Copyright © 2011–2013 David Roden
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

import static net.pterodactylus.sone.utils.NumberParsers.parseLong;

import net.pterodactylus.sone.core.SoneRescuer;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

/**
 * Page that lets the user control the rescue mode for a Sone.
 *
 * @see SoneRescuer
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class RescuePage extends SoneTemplatePage {

	/**
	 * Creates a new rescue page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public RescuePage(Template template, WebInterface webInterface) {
		super("rescue.html", template, "Page.Rescue.Title", webInterface, true);
	}

	//
	// SONETEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		Sone currentSone = getCurrentSone(request.getToadletContext(), false);
		SoneRescuer soneRescuer = webInterface.getCore().getSoneRescuer(currentSone);
		if (request.getMethod() == Method.POST) {
			if ("true".equals(request.getHttpRequest().getPartAsStringFailsafe("fetch", 4))) {
				long edition = parseLong(request.getHttpRequest().getPartAsStringFailsafe("edition", 8), -1L);
				if (edition > -1) {
					soneRescuer.setEdition(edition);
				}
				soneRescuer.startNextFetch();
			}
			throw new RedirectException("rescue.html");
		}
		templateContext.set("soneRescuer", soneRescuer);
	}

}
