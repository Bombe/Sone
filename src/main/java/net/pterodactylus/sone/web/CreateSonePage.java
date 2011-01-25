/*
 * FreenetSone - CreateSonePage.java - Copyright © 2010 David Roden
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import freenet.clients.http.ToadletContext;

/**
 * The “create Sone” page lets the user create a new Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreateSonePage extends SoneTemplatePage {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(CreateSonePage.class);

	/**
	 * Creates a new “create Sone” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public CreateSonePage(Template template, WebInterface webInterface) {
		super("createSone.html", template, "Page.CreateSone.Title", webInterface, false);
	}

	//
	// STATIC ACCESSORS
	//

	/**
	 * Returns a sorted list of all own identities that do not have the “Sone”
	 * context.
	 *
	 * @param core
	 *            The core
	 * @return The list of own identities without the “Sone” context
	 */
	public static List<OwnIdentity> getOwnIdentitiesWithoutSone(Core core) {
		List<OwnIdentity> identitiesWithoutSone = new ArrayList<OwnIdentity>();
		Set<OwnIdentity> allOwnIdentity = core.getIdentityManager().getAllOwnIdentities();
		for (OwnIdentity ownIdentity : allOwnIdentity) {
			if (!ownIdentity.hasContext("Sone")) {
				identitiesWithoutSone.add(ownIdentity);
			}
		}
		Collections.sort(identitiesWithoutSone, new Comparator<OwnIdentity>() {

			@Override
			public int compare(OwnIdentity leftIdentity, OwnIdentity rightIdentity) {
				return (leftIdentity.getNickname() + "@" + leftIdentity.getId()).compareToIgnoreCase(rightIdentity.getNickname() + "@" + rightIdentity.getId());
			}
		});
		return identitiesWithoutSone;
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		List<OwnIdentity> ownIdentitiesWithoutSone = getOwnIdentitiesWithoutSone(webInterface.getCore());
		templateContext.set("identitiesWithoutSone", ownIdentitiesWithoutSone);
		if (request.getMethod() == Method.POST) {
			String id = request.getHttpRequest().getPartAsStringFailsafe("identity", 44);
			OwnIdentity selectedIdentity = null;
			for (OwnIdentity ownIdentity : ownIdentitiesWithoutSone) {
				if (ownIdentity.getId().equals(id)) {
					selectedIdentity = ownIdentity;
					break;
				}
			}
			if (selectedIdentity == null) {
				templateContext.set("errorNoIdentity", true);
				return;
			}
			/* create Sone. */
			Sone sone = webInterface.getCore().createSone(selectedIdentity);
			if (sone == null) {
				logger.log(Level.SEVERE, "Could not create Sone for OwnIdentity: %s", selectedIdentity);
				/* TODO - go somewhere else */
			}

			/* log in the new Sone. */
			setCurrentSone(request.getToadletContext(), sone);
			throw new RedirectException("index.html");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled(ToadletContext toadletContext) {
		return (getCurrentSone(toadletContext, false) == null) || (webInterface.getCore().getLocalSones().size() == 1);
	}

}
