/*
 * Sone - EditProfileFieldAjaxPage.java - Copyright © 2011 David Roden
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

import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Profile.Field;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.json.JsonObject;

/**
 * AJAX page that lets the user rename a profile field.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class EditProfileFieldAjaxPage extends JsonPage {

	/**
	 * Creates a new “edit profile field” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public EditProfileFieldAjaxPage(WebInterface webInterface) {
		super("editProfileField.ajax", webInterface);
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(Request request) {
		String fieldId = request.getHttpRequest().getParam("field");
		Sone currentSone = getCurrentSone(request.getToadletContext());
		Profile profile = currentSone.getProfile();
		Field field = profile.getFieldById(fieldId);
		if (field == null) {
			return createErrorJsonObject("invalid-field-id");
		}
		String name = request.getHttpRequest().getParam("name", "").trim();
		if (name.length() == 0) {
			return createErrorJsonObject("invalid-parameter-name");
		}
		Field existingField = profile.getFieldByName(name);
		if ((existingField != null) && !existingField.equals(field)) {
			return createErrorJsonObject("duplicate-field-name");
		}
		field.setName(name);
		currentSone.setProfile(profile);
		return createSuccessJsonObject();
	}

}
