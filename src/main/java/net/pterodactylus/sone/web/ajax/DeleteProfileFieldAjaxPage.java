/*
 * Sone - DeleteProfileFieldAjaxPage.java - Copyright © 2011–2013 David Roden
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

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Profile.Field;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * AJAX page that lets the user delete a profile field.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DeleteProfileFieldAjaxPage extends JsonPage {

	/**
	 * Creates a new “delete profile field” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public DeleteProfileFieldAjaxPage(WebInterface webInterface) {
		super("deleteProfileField.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
		String fieldId = request.getHttpRequest().getParam("field");
		Sone currentSone = getCurrentSone(request.getToadletContext());
		Profile profile = currentSone.getProfile();
		Field field = profile.getFieldById(fieldId);
		if (field == null) {
			return createErrorJsonObject("invalid-field-id");
		}
		profile.removeField(field);
		currentSone.setProfile(profile);
		webInterface.getCore().touchConfiguration();
		return createSuccessJsonObject().put("field", new ObjectNode(instance).put("id", new TextNode(field.getId())));
	}

}
