/*
 * Sone - EditProfilePage.java - Copyright © 2010 David Roden
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

import java.util.Map;

import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.template.DataProvider;
import net.pterodactylus.util.template.Template;
import freenet.clients.http.ToadletContext;

/**
 * This page lets the user edit her profile.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class EditProfilePage extends SoneTemplatePage {

	/**
	 * Creates a new “edit profile” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public EditProfilePage(Template template, WebInterface webInterface) {
		super("editProfile.html", template, "Page.EditProfile.Title", webInterface, true);
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
		ToadletContext toadletContenxt = request.getToadletContext();
		Sone currentSone = getCurrentSone(toadletContenxt);
		Profile profile = currentSone.getProfile();
		String firstName = profile.getFirstName();
		String middleName = profile.getMiddleName();
		String lastName = profile.getLastName();
		Integer birthDay = profile.getBirthDay();
		Integer birthMonth = profile.getBirthMonth();
		Integer birthYear = profile.getBirthYear();
		Map<String, String> fields = profile.getFields();
		if (request.getMethod() == Method.POST) {
			if (request.getHttpRequest().getPartAsStringFailsafe("save-profile", 4).equals("true")) {
				firstName = request.getHttpRequest().getPartAsStringFailsafe("first-name", 256).trim();
				middleName = request.getHttpRequest().getPartAsStringFailsafe("middle-name", 256).trim();
				lastName = request.getHttpRequest().getPartAsStringFailsafe("last-name", 256).trim();
				birthDay = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("birth-day", 256).trim());
				birthMonth = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("birth-month", 256).trim());
				birthYear = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("birth-year", 256).trim());
				profile.setFirstName(firstName.length() > 0 ? firstName : null);
				profile.setMiddleName(middleName.length() > 0 ? middleName : null);
				profile.setLastName(lastName.length() > 0 ? lastName : null);
				profile.setBirthDay(birthDay).setBirthMonth(birthMonth).setBirthYear(birthYear);
				for (int fieldIndex = 0; fieldIndex < profile.getFieldNames().size(); ++fieldIndex) {
					String value = request.getHttpRequest().getPartAsStringFailsafe("field-" + fieldIndex, 400);
					profile.setField(fieldIndex, value);
				}
				currentSone.setProfile(profile);
				webInterface.getCore().saveSone(currentSone);
				throw new RedirectException("editProfile.html");
			} else if (request.getHttpRequest().getPartAsStringFailsafe("add-field", 4).equals("true")) {
				String fieldName = request.getHttpRequest().getPartAsStringFailsafe("field-name", 256).trim();
				try {
					profile.addField(fieldName);
					currentSone.setProfile(profile);
					fields = profile.getFields();
					webInterface.getCore().saveSone(currentSone);
					throw new RedirectException("editProfile.html#profile-fields");
				} catch (IllegalArgumentException iae1) {
					dataProvider.set("fieldName", fieldName);
					dataProvider.set("duplicateFieldName", true);
				}
			} else {
				int deleteFieldIndex = getFieldIndex(request, "delete-field-");
				if (deleteFieldIndex > -1) {
					throw new RedirectException("deleteProfileField.html?field=" + deleteFieldIndex);
				}
				int moveUpFieldIndex = getFieldIndex(request, "move-up-field-");
				if (moveUpFieldIndex > -1) {
					profile.moveFieldUp(moveUpFieldIndex);
					currentSone.setProfile(profile);
					throw new RedirectException("editProfile.html#profile-fields");
				}
				int moveDownFieldIndex = getFieldIndex(request, "move-down-field-");
				if (moveDownFieldIndex > -1) {
					profile.moveFieldDown(moveDownFieldIndex);
					currentSone.setProfile(profile);
					throw new RedirectException("editProfile.html#profile-fields");
				}
				int editFieldIndex = getFieldIndex(request, "edit-field-");
				if (editFieldIndex > -1) {
					throw new RedirectException("editProfileField.html?field=" + editFieldIndex);
				}
			}
		}
		dataProvider.set("firstName", firstName);
		dataProvider.set("middleName", middleName);
		dataProvider.set("lastName", lastName);
		dataProvider.set("birthDay", birthDay);
		dataProvider.set("birthMonth", birthMonth);
		dataProvider.set("birthYear", birthYear);
		dataProvider.set("fields", fields);
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Searches for a part whose names starts with the given {@code String} and
	 * extracts the number from the located name.
	 *
	 * @param request
	 *            The request to get the parts from
	 * @param partNameStart
	 *            The start of the name of the requested part
	 * @return The parsed number, or {@code -1} if the number could not be
	 *         parsed
	 */
	private int getFieldIndex(Request request, String partNameStart) {
		for (String partName : request.getHttpRequest().getParts()) {
			if (partName.startsWith(partNameStart)) {
				return Numbers.safeParseInteger(partName.substring(partNameStart.length()), -1);
			}
		}
		return -1;
	}
}
