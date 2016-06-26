/*
 * Sone - TrustPage.java - Copyright © 2011–2016 David Roden
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

import com.google.common.base.Optional;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

/**
 * Page that lets the user trust another Sone. This will assign a configurable
 * amount of trust to an identity.
 *
 * @see Core#trustSone(Sone, Sone)
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TrustPage extends SoneTemplatePage {

	/**
	 * Creates a new “trust Sone” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public TrustPage(Template template, WebInterface webInterface) {
		super("trust.html", template, "Page.Trust.Title", webInterface, true);
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
		if (request.getMethod() == Method.POST) {
			String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
			String identity = request.getHttpRequest().getPartAsStringFailsafe("sone", 44);
			Sone currentSone = getCurrentSone(request.getToadletContext());
			Optional<Sone> sone = webInterface.getCore().getSone(identity);
			if (sone.isPresent()) {
				webInterface.getCore().trustSone(currentSone, sone.get());
			}
			throw new RedirectException(returnPage);
		}
	}

}
