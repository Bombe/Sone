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

import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.number.Numbers;
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
		super("editProfile.html", template, "Page.EditProfile.Title", webInterface);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, Template template) throws RedirectException {
		super.processTemplate(request, template);
		ToadletContext toadletContenxt = request.getToadletContext();
		Sone currentSone = getCurrentSone(toadletContenxt);
		Profile profile = currentSone.getProfile();
		String firstName = profile.getFirstName();
		String middleName = profile.getMiddleName();
		String lastName = profile.getLastName();
		Integer birthDay = profile.getBirthDay();
		Integer birthMonth = profile.getBirthMonth();
		Integer birthYear = profile.getBirthYear();
		if (request.getMethod() == Method.POST) {
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
			if (profile.isModified()) {
				currentSone.setProfile(profile);
			}
			throw new RedirectException("index.html");
		}
		template.set("firstName", firstName);
		template.set("middleName", middleName);
		template.set("lastName", lastName);
		template.set("birthDay", birthDay);
		template.set("birthMonth", birthMonth);
		template.set("birthYear", birthYear);
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
