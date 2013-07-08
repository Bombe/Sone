/*
 * Sone - DistrustAjaxPage.java - Copyright © 2011–2013 David Roden
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

package net.pterodactylus.sone.web.ajax;

import com.google.common.base.Optional;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;

/**
 * AJAX page that lets the user distrust a Sone.
 *
 * @see Core#distrustSone(Sone, Sone)
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DistrustAjaxPage extends JsonPage {

	/**
	 * Creates a new “distrust Sone” AJAX handler.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public DistrustAjaxPage(WebInterface webInterface) {
		super("distrustSone.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
		Sone currentSone = getCurrentSone(request.getToadletContext(), false);
		if (currentSone == null) {
			return createErrorJsonObject("auth-required");
		}
		String soneId = request.getHttpRequest().getParam("sone");
		Optional<Sone> sone = webInterface.getCore().getSone(soneId);
		if (!sone.isPresent()) {
			return createErrorJsonObject("invalid-sone-id");
		}
		webInterface.getCore().distrustSone(currentSone, sone.get());
		return createSuccessJsonObject().put("trustValue", webInterface.getCore().getPreferences().getNegativeTrust());
	}

}
