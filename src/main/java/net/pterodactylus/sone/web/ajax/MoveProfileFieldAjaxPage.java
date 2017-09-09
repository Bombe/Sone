/*
 * Sone - MoveProfileFieldAjaxPage.java - Copyright © 2011–2016 David Roden
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

import javax.annotation.Nonnull;

import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Profile.Field;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;

/**
 * AJAX page that lets the user move a profile field up or down.
 *
 * @see Profile#moveFieldUp(Field)
 * @see Profile#moveFieldDown(Field)
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MoveProfileFieldAjaxPage extends LoggedInJsonPage {

	/**
	 * Creates a new “move profile field” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public MoveProfileFieldAjaxPage(WebInterface webInterface) {
		super("moveProfileField.ajax", webInterface);
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	protected JsonReturnObject createJsonObject(@Nonnull Sone currentSone, @Nonnull FreenetRequest request) {
		Profile profile = currentSone.getProfile();
		String fieldId = request.getHttpRequest().getParam("field");
		Field field = profile.getFieldById(fieldId);
		if (field == null) {
			return createErrorJsonObject("invalid-field-id");
		}
		String direction = request.getHttpRequest().getParam("direction");
		try {
			if ("up".equals(direction)) {
				profile.moveFieldUp(field);
			} else if ("down".equals(direction)) {
				profile.moveFieldDown(field);
			} else {
				return createErrorJsonObject("invalid-direction");
			}
		} catch (IllegalArgumentException iae1) {
			return createErrorJsonObject("not-possible");
		}
		currentSone.setProfile(profile);
		webInterface.getCore().touchConfiguration();
		return createSuccessJsonObject();
	}

}
