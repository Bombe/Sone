/*
 * Sone - UntrustAjaxPage.java - Copyright © 2011–2012 David Roden
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

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.json.JsonObject;

/**
 * AJAX page that lets the user untrust a Sone.
 *
 * @see Core#untrustSone(Sone, Sone)
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UntrustAjaxPage extends JsonPage {

	/**
	 * Creates a new “untrust Sone” AJAX handler.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public UntrustAjaxPage(WebInterface webInterface) {
		super("untrustSone.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(FreenetRequest request) {
		Sone currentSone = getCurrentSone(request.getToadletContext(), false);
		if (currentSone == null) {
			return createErrorJsonObject("auth-required");
		}
		String soneId = request.getHttpRequest().getParam("sone");
		Sone sone = webInterface.getCore().getSone(soneId, false);
		if (sone == null) {
			return createErrorJsonObject("invalid-sone-id");
		}
		webInterface.getCore().untrustSone(currentSone, sone);
		return createSuccessJsonObject().put("trustValue", (String) null);
	}

}
