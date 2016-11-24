/*
 * Sone - DeleteProfileFieldPage.java - Copyright © 2011–2016 David Roden
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
import net.pterodactylus.sone.data.Profile.Field;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

/**
 * Page that lets the user confirm the deletion of a profile field.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DeleteProfileFieldPage extends SoneTemplatePage {

	/**
	 * Creates a new “delete profile field” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public DeleteProfileFieldPage(Template template, WebInterface webInterface) {
		super("deleteProfileField.html", template, "Page.DeleteProfileField.Title", webInterface, true);
	}

	//
	// SONETEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void handleRequest(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		Sone currentSone = getCurrentSone(request.getToadletContext());
		Profile profile = currentSone.getProfile();

		/* get parameters from request. */
		String fieldId = request.getHttpRequest().getParam("field");
		Field field = profile.getFieldById(fieldId);
		if (field == null) {
			throw new RedirectException("invalid.html");
		}

		/* process POST request. */
		if (request.getMethod() == Method.POST) {
			if (request.getHttpRequest().getPartAsStringFailsafe("confirm", 4).equals("true")) {
				profile.removeField(field);
				currentSone.setProfile(profile);
			}
			throw new RedirectException("editProfile.html#profile-fields");
		}

		/* set current values in template. */
		templateContext.set("field", field);
	}

}
